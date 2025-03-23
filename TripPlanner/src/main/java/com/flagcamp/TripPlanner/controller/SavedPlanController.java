package com.flagcamp.TripPlanner.controller;


import com.flagcamp.TripPlanner.entity.TripEntity;
import org.apache.catalina.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.ArrayList;
import java.util.List;

@RestController
public class SavedPlanController {

//    @GetMapping("/savedtrips")
//    public List<TripEntity> getTrips(@AuthenticationPrincipal User user) {
//        return new ArrayList<>();
//    }

    @GetMapping("savedtrips/{tripId}")
    public String getTrips(@PathVariable("userId") long userId) {
        return null;
    }
}
