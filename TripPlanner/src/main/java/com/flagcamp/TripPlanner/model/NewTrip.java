package com.flagcamp.TripPlanner.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;

public record NewTrip(
        Long userId,
        String country,
        String city,
        LocalDate startTime,  // 修改为 LocalDate
        LocalDate endTime,    // 修改为 LocalDate
        String preferences,
        String tripPlanDetail  // 保持为 JsonNode
) {
}
