package com.flagcamp.TripPlanner.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flagcamp.TripPlanner.entity.TripEntity;
import com.flagcamp.TripPlanner.service.TripPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

@RestController
public class SavedPlanController {

    private static final Logger logger = LoggerFactory.getLogger(SavedPlanController.class);
    private final TripPlanService tripPlanService;
    private final ObjectMapper objectMapper;

    @Autowired
    public SavedPlanController(TripPlanService tripPlanService, ObjectMapper objectMapper) {
        this.tripPlanService = tripPlanService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/savedtrips")
    public ResponseEntity<Object> getSavedTrips() {
        try {
            logger.info("Retrieving all saved trip plans");
            List<TripEntity> trips = tripPlanService.getAllSavedTrips();
            logger.info("Found {} trip plan records", trips.size());
            
            // Convert TripEntity to required format
            List<Map<String, Object>> result = new ArrayList<>();
            for (TripEntity trip : trips) {
                Map<String, Object> tripMap = new HashMap<>();
                tripMap.put("country", trip.country());
                tripMap.put("state", trip.state());
                tripMap.put("city", trip.city());
                tripMap.put("start_date", trip.startTime());
                tripMap.put("end_date", trip.endTime());
                
                // Handle preferences with emoji support
                String preferences = trip.preferences();
                if (preferences != null) {
                    // Use preferences directly - Java String/JSON supports Unicode/emoji
                    tripMap.put("preferences", preferences);
                }
                
                tripMap.put("trip_id", String.valueOf(trip.id()));
                result.add(tripMap);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error retrieving trip plans", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error retrieving trip plans: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/savedtrips/{trip_id}")
    public ResponseEntity<Object> getSavedTripById(@PathVariable("trip_id") Long tripId) {
        try {
            logger.info("Retrieving trip plan with ID {}", tripId);
            TripEntity trip = tripPlanService.getSavedTripById(tripId);
            if (trip != null) {
                logger.info("Successfully found trip plan with ID {}", tripId);
                
                // Build response according to documentation
                Map<String, Object> response = new HashMap<>();
                Map<String, Object> tripPlan = new HashMap<>();
                
                // Basic info
                tripPlan.put("city", trip.city());
                tripPlan.put("country", trip.country());
                
                // Create dates array
                List<String> dates = new ArrayList<>();
                LocalDate currentDate = trip.startTime();
                while (!currentDate.isAfter(trip.endTime())) {
                    dates.add(currentDate.toString());
                    currentDate = currentDate.plusDays(1);
                }
                tripPlan.put("dates", dates);
                
                // Add itinerary details with proper formatting
                JsonNode tripDetailNode = trip.tripPlanDetail();
                if (tripDetailNode != null) {
                    tripPlan.put("daily_itinerary", formatDailyItinerary(tripDetailNode));
                }
                
                response.put("trip_plan", tripPlan);
                
                return ResponseEntity.ok(response);
            } else {
                logger.info("Trip plan with ID {} not found", tripId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error retrieving trip plan with ID {}", tripId, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error retrieving trip plan: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Format daily itinerary to match API documentation requirements
     */
    private JsonNode formatDailyItinerary(JsonNode tripDetailNode) {
        try {
            // Check if data has daily_itinerary field
            JsonNode dailyItineraryNode = null;
            if (tripDetailNode.has("daily_itinerary")) {
                dailyItineraryNode = tripDetailNode.get("daily_itinerary");
            } else {
                // Handle case when original data isn't nested in daily_itinerary
                dailyItineraryNode = tripDetailNode;
            }
            
            if (dailyItineraryNode == null || dailyItineraryNode.isEmpty()) {
                return objectMapper.createObjectNode();
            }
            
            ObjectNode formattedItinerary = objectMapper.createObjectNode();
            
            // Process each day's itinerary
            Iterator<String> dateFields = dailyItineraryNode.fieldNames();
            while (dateFields.hasNext()) {
                String date = dateFields.next();
                JsonNode dayPlaces = dailyItineraryNode.get(date);
                if (!dayPlaces.isArray()) {
                    continue;
                }
                
                ArrayNode formattedDayPlaces = formattedItinerary.putArray(date);
                
                // Process each place for the day
                for (int i = 0; i < dayPlaces.size(); i++) {
                    JsonNode place = dayPlaces.get(i);
                    if (!place.isObject()) {
                        continue;
                    }
                    
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
            
            return formattedItinerary;
        } catch (Exception e) {
            logger.error("Error formatting daily itinerary: {}", e.getMessage());
            return objectMapper.createObjectNode(); // Return empty object on error
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
}
