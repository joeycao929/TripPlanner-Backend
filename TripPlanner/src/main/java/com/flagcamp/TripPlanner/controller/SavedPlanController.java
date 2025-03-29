package com.flagcamp.TripPlanner.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flagcamp.TripPlanner.entity.TripEntity;
import com.flagcamp.TripPlanner.entity.UserEntity;
import com.flagcamp.TripPlanner.service.TripPlanService;
import com.flagcamp.TripPlanner.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;

@RestController
public class SavedPlanController {
    private static final Logger logger = LoggerFactory.getLogger(SavedPlanController.class);
    private static final String FIELD_REVIEW = "review";
    private static final String FIELD_REVIEWS = "reviews";
    private static final String FIELD_DAILY_ITINERARY = "daily_itinerary";
    
    private final TripPlanService tripPlanService;
    private final ObjectMapper objectMapper;
    private final UserService userService;

    @Autowired
    public SavedPlanController(TripPlanService tripPlanService, ObjectMapper objectMapper, UserService userService) {
        this.tripPlanService = tripPlanService;
        this.objectMapper = objectMapper;
        this.userService = userService;
    }

    @GetMapping("/savedtrips")
    public ResponseEntity<Object> getSavedTrips(@AuthenticationPrincipal User user) {
        try {
            UserEntity authUser = userService.getUserByEmail(user.getUsername());
            logger.info("Retrieving saved trip plans for user: {}", authUser.id());
            List<TripEntity> trips = tripPlanService.getSavedTripsByUserId(authUser.id());
            logger.info("Found {} trip plan records", trips.size());
            
            List<Map<String, Object>> result = convertTripsToResponse(trips);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error retrieving trip plans", e);
            return createErrorResponse("Error retrieving trip plans: " + e.getMessage());
        }
    }

    private List<Map<String, Object>> convertTripsToResponse(List<TripEntity> trips) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (TripEntity trip : trips) {
            Map<String, Object> tripMap = new HashMap<>();
            tripMap.put("country", trip.country());
            tripMap.put("state", trip.state());
            tripMap.put("city", trip.city());
            tripMap.put("start_date", trip.startTime());
            tripMap.put("end_date", trip.endTime());
            
            if (trip.preferences() != null) {
                tripMap.put("preferences", trip.preferences());
            }
            
            tripMap.put("trip_id", String.valueOf(trip.id()));
            result.add(tripMap);
        }
        return result;
    }

    @GetMapping("/savedtrips/{trip_id}")
    public ResponseEntity<Object> getSavedTripById(@PathVariable("trip_id") Long tripId, @AuthenticationPrincipal User user) {
        try {
            logger.info("Retrieving trip plan with ID {}", tripId);
            TripEntity trip = tripPlanService.getSavedTripById(tripId);
            
            if (trip == null) {
                logger.info("Trip plan with ID {} not found", tripId);
                return ResponseEntity.notFound().build();
            }

            UserEntity authUser = userService.getUserByEmail(user.getUsername());
            if (!trip.userId().equals(authUser.id())) {
                logger.warn("User {} attempted to access trip {} belonging to user {}", authUser.id(), tripId, trip.userId());
                return ResponseEntity.status(403).build();
            }
            
            logger.info("Successfully found trip plan with ID {}", tripId);
            Map<String, Object> response = buildTripResponse(trip);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving trip plan with ID {}", tripId, e);
            return createErrorResponse("Error retrieving trip plan: " + e.getMessage());
        }
    }

    private Map<String, Object> buildTripResponse(TripEntity trip) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> tripPlan = new HashMap<>();
        
        tripPlan.put("city", trip.city());
        tripPlan.put("country", trip.country());
        tripPlan.put("dates", generateDateRange(trip.startTime(), trip.endTime()));
        
        JsonNode tripDetailNode = trip.tripPlanDetail();
        if (tripDetailNode != null) {
            tripPlan.put(FIELD_DAILY_ITINERARY, formatDailyItinerary(tripDetailNode));
        }
        
        response.put("trip_plan", tripPlan);
        return response;
    }

    private List<String> generateDateRange(LocalDate startDate, LocalDate endDate) {
        List<String> dates = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            dates.add(currentDate.toString());
            currentDate = currentDate.plusDays(1);
        }
        return dates;
    }

    private ResponseEntity<Object> createErrorResponse(String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        return ResponseEntity.status(500).body(errorResponse);
    }
    
    private JsonNode formatDailyItinerary(JsonNode tripDetailNode) {
        try {
            JsonNode dailyItineraryNode = tripDetailNode.has(FIELD_DAILY_ITINERARY) 
                ? tripDetailNode.get(FIELD_DAILY_ITINERARY) 
                : tripDetailNode;
            
            if (dailyItineraryNode == null || dailyItineraryNode.isEmpty()) {
                return objectMapper.createObjectNode();
            }
            
            ObjectNode formattedItinerary = objectMapper.createObjectNode();
            Iterator<String> dateFields = dailyItineraryNode.fieldNames();
            
            while (dateFields.hasNext()) {
                String date = dateFields.next();
                JsonNode dayPlaces = dailyItineraryNode.get(date);
                if (!dayPlaces.isArray()) {
                    continue;
                }
                
                ArrayNode formattedDayPlaces = formattedItinerary.putArray(date);
                formatDayPlaces(dayPlaces, formattedDayPlaces);
            }
            
            return formattedItinerary;
        } catch (Exception e) {
            logger.error("Error formatting daily itinerary: {}", e.getMessage());
            return objectMapper.createObjectNode();
        }
    }

    private void formatDayPlaces(JsonNode dayPlaces, ArrayNode formattedDayPlaces) {
        for (JsonNode place : dayPlaces) {
            if (!place.isObject()) {
                continue;
            }
            
            ObjectNode formattedPlace = objectMapper.createObjectNode();
            copyRequiredFields(place, formattedPlace);
            processReviewField(place, formattedPlace);
            formattedDayPlaces.add(formattedPlace);
        }
    }

    private void copyRequiredFields(JsonNode source, ObjectNode target) {
        List<String> requiredFields = Arrays.asList(
            "place_name", "address", "latitude", "longitude",
            "image_url", "rating", "travel_time_to_next_location",
            "suggest_time_to_spend"
        );
        
        for (String field : requiredFields) {
            copyField(source, target, field);
        }
    }

    private void processReviewField(JsonNode place, ObjectNode formattedPlace) {
        if (place.has(FIELD_REVIEW)) {
            formattedPlace.put(FIELD_REVIEW, place.get(FIELD_REVIEW).asText());
        } else if (place.has(FIELD_REVIEWS)) {
            JsonNode existingReviews = place.get(FIELD_REVIEWS);
            if (existingReviews.isArray() && existingReviews.size() > 0) {
                formattedPlace.put(FIELD_REVIEW, existingReviews.get(0).asText());
            } else if (existingReviews.isTextual()) {
                formattedPlace.put(FIELD_REVIEW, existingReviews.asText());
            }
        }
    }
    
    private void copyField(JsonNode source, ObjectNode target, String fieldName) {
        if (!source.has(fieldName)) {
            return;
        }

        JsonNode value = source.get(fieldName);
        if (value.isTextual()) {
            target.put(fieldName, value.asText());
        } else if (value.isNumber()) {
            copyNumberField(target, fieldName, value);
        } else if (value.isBoolean()) {
            target.put(fieldName, value.asBoolean());
        } else {
            target.set(fieldName, value);
        }
    }

    private void copyNumberField(ObjectNode target, String fieldName, JsonNode value) {
        if (value.isInt()) {
            target.put(fieldName, value.asInt());
        } else if (value.isLong()) {
            target.put(fieldName, value.asLong());
        } else if (value.isDouble()) {
            target.put(fieldName, value.asDouble());
        } else {
            target.put(fieldName, value.asText());
        }
    }
}
