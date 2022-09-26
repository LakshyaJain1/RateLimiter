package com.ratelimiter.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.temporal.ChronoUnit;

/**
 * Generic Rate limiter object which we are using in this library.
 */

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RateLimiterDto {

    String key;
    boolean isRateLimitActivated;
    int rateLimit;
    ChronoUnit timeUnit;
}
