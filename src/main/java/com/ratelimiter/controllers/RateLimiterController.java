package com.ratelimiter.controllers;

import com.ratelimiter.cache.RateLimiterCache;
import com.ratelimiter.models.RateLimiterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rateLimiter")
public class RateLimiterController {

    @Autowired
    RateLimiterCache cache;

    @RequestMapping(value = "/upsertRateLimiterObject", method = RequestMethod.POST)
    public RateLimiterObject upsertRateLimiter(@RequestBody RateLimiterObject rateLimiterObject) {
        return cache.upsert(rateLimiterObject);
    }

    @RequestMapping(value = "/deleteRateLimiterObject", method = RequestMethod.POST)
    public void deleteRateLimiterObject(@RequestParam(value = "key") String key) {
        cache.delete(key);
    }

}
