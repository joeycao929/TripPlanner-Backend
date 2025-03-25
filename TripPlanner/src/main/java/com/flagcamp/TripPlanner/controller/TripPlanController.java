package com.flagcamp.TripPlanner.controller;

import com.flagcamp.TripPlanner.entity.TripEntity;
import com.flagcamp.TripPlanner.model.NewTrip;
import com.flagcamp.TripPlanner.model.SaveTripPlanRequest;
import com.flagcamp.TripPlanner.model.TripSearchBody;
import com.flagcamp.TripPlanner.service.GeminiChatService;
import com.flagcamp.TripPlanner.service.TripPlanService;
import org.springframework.web.bind.annotation.*;

@RestController
public class TripPlanController {
    private final GeminiChatService chatService;
    private final TripPlanService tripPlanService;

    public TripPlanController(GeminiChatService chatService, TripPlanService tripPlanService) {
        this.chatService = chatService;
        this.tripPlanService = tripPlanService;
    }

    @PostMapping("/tripplan")
    public TripEntity getTripPlan(@RequestBody NewTrip body) {
        /*String city = "Sample City"; // 模拟数据
        String country = "Sample Country"; // 模拟数据
        String tripDetail = "Sample Trip Detail"; // 模拟数据
        String startDate = "2023-01-01"; // 模拟数据
        String endDate = "2023-01-10"; // 模拟数据*/

        // 将数据暂存到缓存
        tripPlanService.saveTempData(body.userId(), body.city(), body.country(), body.startTime(),
                body.endTime(), body.preferences(),body.tripPlanDetail());

        // 返回数据

        return new TripEntity(null, body.userId(), body.city(), body.country(), body.startTime(),
                body.endTime(), body.preferences(),body.tripPlanDetail());//这里可能需要返回LLM的数据
    }

    @PostMapping("/tripplan/save")
    public String saveTripPlan(@RequestBody SaveTripPlanRequest request) {
        tripPlanService.saveDataToDatabase(request.userId());
        return "Trip plan saved for user: " + request.userId();
    }
}
