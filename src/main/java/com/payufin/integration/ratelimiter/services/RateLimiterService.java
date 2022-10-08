package com.payufin.integration.ratelimiter.services;

import com.payufin.integration.ratelimiter.models.RateLimiterDto;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.redis.redisson.cas.RedissonBasedProxyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Author - lakshya.jain <br>
 * Date - 09/10/2022
 * <p>
 * <p>
 * Rate Limiter Service help to create the bucket with the given key.
 */

@Service
public class RateLimiterService {

    private final RedissonBasedProxyManager redissonBasedProxyManager;
    private final BucketConfigService bucketConfigService;

    @Autowired
    public RateLimiterService(@Qualifier("rateLimiterProxyManager") RedissonBasedProxyManager redissonBasedProxyManager, BucketConfigService bucketConfigService) {
        this.redissonBasedProxyManager = redissonBasedProxyManager;
        this.bucketConfigService = bucketConfigService;
    }

    public Bucket resolveBucket(RateLimiterDto rateLimiterDto) {
        // Does not always create a new bucket, but instead returns the existing one if it exists.
        return redissonBasedProxyManager
                .builder()
                .build("bucket4j:" + rateLimiterDto.getKey(), bucketConfigService.getConfigSupplierObject(rateLimiterDto));
    }
}
