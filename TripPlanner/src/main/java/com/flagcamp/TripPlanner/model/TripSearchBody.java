package com.flagcamp.TripPlanner.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record TripSearchBody(
        @JsonProperty("user_id") Long userId,
        @JsonProperty("country") String country,
        @JsonProperty("city") String city,
        @JsonProperty("state") String state,
        @JsonProperty("start_date") LocalDate startDate,
        @JsonProperty("end_date") LocalDate endDate,
        @JsonProperty("preferences") String preferences,
        @JsonProperty("trip_plan_detail") String tripPlanDetail
) {
}