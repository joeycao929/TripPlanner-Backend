package com.flagcamp.TripPlanner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flagcamp.TripPlanner.repository.TripPlanRepository;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import com.flagcamp.TripPlanner.entity.TripEntity;

import java.time.LocalDate;

@Service
public class TripPlanService {

    private final CacheManager cacheManager;
    private final TripPlanRepository tripPlanRepository;

    @Autowired
    public TripPlanService(CacheManager cacheManager, TripPlanRepository tripPlanRepository, ObjectMapper objectMapper) {
        this.cacheManager = cacheManager;
        this.tripPlanRepository = tripPlanRepository;
    }

    // 将数据暂存到缓存
    public void saveTempData(long userId, String city, String state, String country,
                             LocalDate startTime, LocalDate endTime, String preferences, JsonNode tripDetail) {
        Cache<String, TripEntity> cache = getCache();

        TripEntity tripPlan = new TripEntity(null, userId, city, state, country, startTime, endTime, preferences, tripDetail);
        cache.put(String.valueOf(userId), tripPlan);  // 缓存中存入TripPlanEntity
    }

    // 从缓存中获取数据
    public TripEntity getTempData(long userId) {
        Cache<String, TripEntity> cache = getCache();
        return cache.getIfPresent(String.valueOf(userId));  // 获取缓存中的数据
    }

    // 将数据从缓存中取出并存入数据库
    public void saveDataToDatabase(long userId) {
        TripEntity tripPlan = getTempData(userId);

        if (tripPlan != null) {
//             使用record的不可变特性，更新tripDetail并创建新的TripPlanEntity实例
            TripEntity updatedTripPlan = new TripEntity(
                    tripPlan.id(),  // 保持原有ID
                    tripPlan.userId(),
                    tripPlan.city(),
                    tripPlan.state(),
                    tripPlan.country(),
                    tripPlan.startTime(),
                    tripPlan.endTime(),
                    tripPlan.preferences(),
                    tripPlan.tripPlanDetail()
            );
            tripPlanRepository.save(updatedTripPlan); // 存入数据库
            getCache().invalidate(String.valueOf(userId)); // 清除缓存
        } else {
            throw new RuntimeException("No data found in cache for user: " + userId);
        }
    }

    // 获取缓存实例
    private Cache<String, TripEntity> getCache() {
        org.springframework.cache.Cache cache = cacheManager.getCache("tripPlanCache");
        return (Cache<String, TripEntity>) cache.getNativeCache();
    }

}

