package com.ratelimiter.cache;

import com.ratelimiter.models.RateLimiterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RateLimiterCache {

    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public RateLimiterCache(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public RateLimiterObject upsert(RateLimiterObject rateLimiterObject) {
        redisTemplate.opsForValue().set(rateLimiterObject.getKey(), rateLimiterObject);
        return rateLimiterObject;
    }

    public RateLimiterObject get(String key) {
        return (RateLimiterObject) redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

}
