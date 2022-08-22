package com.ratelimiter.services;

import com.ratelimiter.models.RateLimiterObject;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.redis.redisson.cas.RedissonBasedProxyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {

    private final RedissonBasedProxyManager redissonBasedProxyManager;
    private final BucketConfigService bucketConfigService;

    @Autowired
    public RateLimiterService(@Qualifier("rateLimiterProxyManager") RedissonBasedProxyManager redissonBasedProxyManager, BucketConfigService bucketConfigService) {
        this.redissonBasedProxyManager = redissonBasedProxyManager;
        this.bucketConfigService = bucketConfigService;
    }

    public Bucket resolveBucket(RateLimiterObject rateLimiterObject) {
        // Does not always create a new bucket, but instead returns the existing one if it exists.
        return redissonBasedProxyManager
                .builder()
                .build("bucket4j:" + rateLimiterObject.getKey(), bucketConfigService.getConfigSupplierObject(rateLimiterObject));
    }
}
