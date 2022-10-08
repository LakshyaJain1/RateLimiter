package com.payufin.integration.ratelimiter.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.temporal.ChronoUnit;

/**
 * Author - lakshya.jain <br>
 * Date - 09/10/2022
 * <p>
 * <p>
 * Generic Rate limiter object which we are using in this library.
 */

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RateLimiterDto {

    String key;
    boolean active;
    int maxLimit;
    ChronoUnit timeUnit;
}
