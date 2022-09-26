package com.ratelimiter.services;

import com.ratelimiter.models.RateLimiterDto;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Configuration class to get the Bucket configuration used by Bucket4j.
 */

@Service
public class BucketConfigService {

    public Supplier<BucketConfiguration> getConfigSupplierObject(RateLimiterDto rateLimiterDto) {
        Refill refill = Refill.intervally(rateLimiterDto.getRateLimit(),
                Duration.of(1, rateLimiterDto.getTimeUnit()));
        Bandwidth limit = Bandwidth.classic(rateLimiterDto.getRateLimit(), refill);
        return () -> (BucketConfiguration.builder()
                .addLimit(limit)
                .build());
    }

}
