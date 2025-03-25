package com.flagcamp.TripPlanner.entity;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Table("trips")
public record TripEntity(
        @Id Long id,
        Long userId,
        String country,
        String city,
        LocalDate startTime,
        LocalDate endTime,
        String preferences,
        String tripPlanDetail
) {
}
