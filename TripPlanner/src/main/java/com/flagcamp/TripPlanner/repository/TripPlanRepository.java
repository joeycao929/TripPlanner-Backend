package com.flagcamp.TripPlanner.repository;

import com.flagcamp.TripPlanner.entity.TripEntity;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface TripPlanRepository extends ListCrudRepository<TripEntity, Long> {
    // Get all saved trip plans for a specific user
    List<TripEntity> getAllByUserId(Long userId);
}
