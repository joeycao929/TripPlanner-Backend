package com.flagcamp.TripPlanner.repository;

import com.flagcamp.TripPlanner.entity.TripEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface SavedPlanRepository extends ListCrudRepository<TripEntity, Long> {
    // Show saved trip plans for specific user
    List<TripEntity> getAllByUserId(Long userId);

    // unsave specific trip
    void deleteById(Long id);
}
