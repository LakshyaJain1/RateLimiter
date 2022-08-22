package com.ratelimiter.services;

import com.ratelimiter.models.RateLimiterObject;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

@Service
public class BucketConfigService {

    public Supplier<BucketConfiguration> getConfigSupplierObject(RateLimiterObject rateLimiterObject) {
        Refill refill = Refill.intervally(rateLimiterObject.getRateLimitPerMinute(), Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(rateLimiterObject.getRateLimitPerMinute(), refill);
        return () -> (BucketConfiguration.builder()
                .addLimit(limit)
                .build());
    }

}
