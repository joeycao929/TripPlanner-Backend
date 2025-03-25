package com.flagcamp.TripPlanner.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.flagcamp.TripPlanner.entity.TripEntity;
import com.flagcamp.TripPlanner.model.SaveTripPlanRequest;
import com.flagcamp.TripPlanner.model.TripSearchBody;
import com.flagcamp.TripPlanner.service.GeminiService;
import com.flagcamp.TripPlanner.service.TripPlanService;
import org.springframework.web.bind.annotation.*;


@RestController
public class TripPlanController {
    private final GeminiService chatService;
    private final TripPlanService tripPlanService;

    public TripPlanController(GeminiService chatService, TripPlanService tripPlanService) {
        this.chatService = chatService;
        this.tripPlanService = tripPlanService;
    }

    @PostMapping("/tripplan")
    public TripEntity getTripPlan(@RequestBody TripSearchBody body) {

        // get LLM data
        JsonNode trip = chatService.getTripDetail(body);

        // 将数据暂存到缓存
        tripPlanService.saveTempData(
                                        body.userId(),
                                        body.city(),
                                        body.state(),
                                        body.country(),
                                        body.startDate(),
                                        body.endDate(),
                                        body.preferences(),
                                        trip
                                    );

        // 返回数据
        return new TripEntity(null, body.userId(), body.city(), body.state(), body.country(), body.startDate(),
                body.endDate(), body.preferences(), trip);//这里可能需要返回LLM的数据
    }

    @PostMapping("/tripplan/save")
    public String saveTripPlan(@RequestBody SaveTripPlanRequest request) {
        tripPlanService.saveDataToDatabase(request.userId());
        return "Trip plan saved for user: " + request.userId();
    }
}
