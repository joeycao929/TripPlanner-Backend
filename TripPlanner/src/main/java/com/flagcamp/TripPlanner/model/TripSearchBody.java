package com.flagcamp.TripPlanner.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TripSearchBody(
        @JsonProperty("country") String country,
        @JsonProperty("city") String city,
        @JsonProperty("state") String state,
        @JsonProperty("start_date") String startDate,
        @JsonProperty("end_date") String endDate,
        @JsonProperty("preferences") String preferences
) {
}
