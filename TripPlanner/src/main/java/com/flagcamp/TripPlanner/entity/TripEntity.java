package com.flagcamp.TripPlanner.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("trips")
public record TripEntity(
        @Id Long id,
        Long userId,
        String country,
        String city,
        String startDate,
        String endDate,
        String preferences,
        String tripPlanDetail
) {
}
