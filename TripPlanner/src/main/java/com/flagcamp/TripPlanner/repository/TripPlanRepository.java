package com.flagcamp.TripPlanner.repository;

import com.flagcamp.TripPlanner.entity.TripEntity;
import org.springframework.data.repository.CrudRepository;

public interface TripPlanRepository extends CrudRepository<TripEntity, Long> {
    //save the new plan created by gemini chat

}
