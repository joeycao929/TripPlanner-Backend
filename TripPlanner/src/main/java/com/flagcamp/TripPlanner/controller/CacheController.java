package com.flagcamp.TripPlanner.controller;

import com.flagcamp.TripPlanner.entity.TripEntity;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@RestController
@RequestMapping("/cache")
public class CacheController {

    private final CacheManager cacheManager;

    public CacheController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @GetMapping("/")
    public Map<String, Object> getAllCacheData() {
        Map<String, Object> result = new HashMap<>();
        org.springframework.cache.Cache springCache = cacheManager.getCache("tripPlanCache");
        
        if (springCache != null) {
            Cache<Object, Object> nativeCache = (Cache<Object, Object>) springCache.getNativeCache();
            if (nativeCache != null) {
                // Get all keys
                Set<Object> keys = nativeCache.asMap().keySet();
                
                // For each key, retrieve its value and store in result map
                for (Object key : keys) {
                    Object value = nativeCache.getIfPresent(key);
                    if (value != null) {
                        result.put(key.toString(), value);
                    }
                }
            }
        }
        
        return result;
    }

    @GetMapping("/{cacheName}/{key}")
    public Object getCacheValue(@PathVariable String cacheName, @PathVariable String key) {
        org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            org.springframework.cache.Cache.ValueWrapper value = cache.get(key);
            return value != null ? value.get() : "Cache miss";
        }
        return "Cache not found";
    }
}

