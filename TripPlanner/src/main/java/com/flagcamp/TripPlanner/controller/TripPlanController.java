package com.flagcamp.TripPlanner.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flagcamp.TripPlanner.entity.TripEntity;
import com.flagcamp.TripPlanner.model.SaveTripPlanRequest;
import com.flagcamp.TripPlanner.model.TripSearchBody;
import com.flagcamp.TripPlanner.service.GeminiService;
import com.flagcamp.TripPlanner.service.TripPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;


@RestController
public class TripPlanController {
    private static final Logger logger = LoggerFactory.getLogger(TripPlanController.class);
    private final GeminiService chatService;
    private final TripPlanService tripPlanService;
    private final ObjectMapper objectMapper;

    @Autowired
    public TripPlanController(GeminiService chatService, TripPlanService tripPlanService, ObjectMapper objectMapper) {
        this.chatService = chatService;
        this.tripPlanService = tripPlanService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/tripplan")
    public ResponseEntity<Map<String, Object>> getTripPlan(@RequestBody TripSearchBody body) {
        // Get LLM data
        JsonNode tripDetailNode = chatService.getTripDetail(body);
        
        // Format data to meet API requirements
        JsonNode formattedTripDetail = formatTripDetail(tripDetailNode);

        // Save data to cache
        tripPlanService.saveTempData(
                body.userId(),
                body.city(),
                body.state(),
                body.country(),
                body.startDate(),
                body.endDate(),
                body.preferences(),
                formattedTripDetail
        );

        // Build response according to documentation
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> tripPlan = new HashMap<>();
        
        // Basic info
        tripPlan.put("city", body.city());
        tripPlan.put("country", body.country());
        
        // Create dates array
        List<String> dates = new ArrayList<>();
        LocalDate currentDate = body.startDate();
        while (!currentDate.isAfter(body.endDate())) {
            dates.add(currentDate.toString());
            currentDate = currentDate.plusDays(1);
        }
        tripPlan.put("dates", dates);
        
        // Add itinerary details
        if (formattedTripDetail.has("daily_itinerary")) {
            tripPlan.put("daily_itinerary", formattedTripDetail.get("daily_itinerary"));
        }
        
        response.put("trip_plan", tripPlan);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Format trip details to meet API documentation requirements
     */
    private JsonNode formatTripDetail(JsonNode tripDetailNode) {
        try {
            if (tripDetailNode == null || !tripDetailNode.has("daily_itinerary")) {
                return tripDetailNode;
            }
            
            ObjectNode formattedNode = objectMapper.createObjectNode();
            ObjectNode dailyItinerary = formattedNode.putObject("daily_itinerary");
            
            // Process each day's itinerary
            JsonNode originalItinerary = tripDetailNode.get("daily_itinerary");
            Iterator<String> dateFields = originalItinerary.fieldNames();
            
            while (dateFields.hasNext()) {
                String date = dateFields.next();
                JsonNode dayPlaces = originalItinerary.get(date);
                ArrayNode formattedDayPlaces = dailyItinerary.putArray(date);
                
                // Process each place for the day
                for (int i = 0; i < dayPlaces.size(); i++) {
                    JsonNode place = dayPlaces.get(i);
                    ObjectNode formattedPlace = objectMapper.createObjectNode();
                    
                    // Copy required fields
                    copyField(place, formattedPlace, "place_name");
                    copyField(place, formattedPlace, "address");
                    copyField(place, formattedPlace, "latitude");
                    copyField(place, formattedPlace, "longitude");
                    copyField(place, formattedPlace, "image_url");
                    copyField(place, formattedPlace, "rating");
                    copyField(place, formattedPlace, "travel_time_to_next_location");
                    copyField(place, formattedPlace, "suggest_time_to_spend");
                    
                    // Process review field - keep as single string instead of array
                    if (place.has("review")) {
                        // Use existing review field as is
                        formattedPlace.put("review", place.get("review").asText());
                    } else if (place.has("reviews")) {
                        // If there's a reviews array, use the first review or join them
                        JsonNode existingReviews = place.get("reviews");
                        if (existingReviews.isArray() && existingReviews.size() > 0) {
                            // Get the first review from the array
                            formattedPlace.put("review", existingReviews.get(0).asText());
                        } else if (existingReviews.isTextual()) {
                            formattedPlace.put("review", existingReviews.asText());
                        }
                    }
                    
                    formattedDayPlaces.add(formattedPlace);
                }
            }
            
            return formattedNode;
        } catch (Exception e) {
            logger.error("Error formatting trip details: {}", e.getMessage());
            return tripDetailNode; // Return original data on error
        }
    }
    
    /**
     * Copy field from source to target node
     */
    private void copyField(JsonNode source, ObjectNode target, String fieldName) {
        if (source.has(fieldName)) {
            JsonNode value = source.get(fieldName);
            if (value.isTextual()) {
                target.put(fieldName, value.asText());
            } else if (value.isNumber()) {
                if (value.isInt()) {
                    target.put(fieldName, value.asInt());
                } else if (value.isLong()) {
                    target.put(fieldName, value.asLong());
                } else if (value.isDouble()) {
                    target.put(fieldName, value.asDouble());
                } else {
                    target.put(fieldName, value.asText());
                }
            } else if (value.isBoolean()) {
                target.put(fieldName, value.asBoolean());
            } else {
                target.set(fieldName, value);
            }
        }
    }

    @PostMapping("/tripplan/save")
    public ResponseEntity<String> saveTripPlan(@RequestBody SaveTripPlanRequest request) {
        tripPlanService.saveDataToDatabase(request.userId());
        String responseMessage = "Trip plan saved for user: " + request.userId();
        return ResponseEntity.ok(responseMessage);
    }
}
