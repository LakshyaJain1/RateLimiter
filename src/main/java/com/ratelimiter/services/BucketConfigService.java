package com.ratelimiter.services;

import com.ratelimiter.models.RateLimiterObject;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

/**
 * Configuration class to get the Bucket configuration used by Bucket4j.
 */

@Service
public class BucketConfigService {

    public Supplier<BucketConfiguration> getConfigSupplierObject(RateLimiterObject rateLimiterObject) {
        Refill refill = Refill.intervally(rateLimiterObject.getRateLimit(),
                Duration.of(1, rateLimiterObject.getTimeUnit()));
        Bandwidth limit = Bandwidth.classic(rateLimiterObject.getRateLimit(), refill);
        return () -> (BucketConfiguration.builder()
                .addLimit(limit)
                .build());
    }

}
