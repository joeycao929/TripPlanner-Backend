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
        //use string or json to get the body?
        //get the user id from the body
        //create a new tripPlanEntity
        //set the user id as key and tripPlanEntity as value
        //save it in the cache
        //return the tripPlanEntity created by gemini chat service


        //tripPlanService.saveTempData(new TripPlanEntity());//save the temp data to the database
        //return the tripPlanEntity created by gemini chat service
        return null;
    }

    @PostMapping("/tripplan/save")
    public void getTripPlan(@RequestBody TripSearchBody body){
        // save data to database
        // tripPlanService.saveDataToDatabase(request.userId(), request.tripDetail());

    }
}
