package com.flagcamp.TripPlanner.controller;

import com.flagcamp.TripPlanner.model.TripSearchBody;
import com.flagcamp.TripPlanner.service.GeminiChatService;
import org.springframework.web.bind.annotation.*;

@RestController
public class TripPlanController {
    private final GeminiChatService chatService;

    public TripPlanController(GeminiChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/tripplan")
    public String getTripPlan(String body){
        return null;
    }

    @PostMapping("/tripplan/save")
    public void getTripPlan(@RequestBody TripSearchBody body){
        // save data to database

    }
}
