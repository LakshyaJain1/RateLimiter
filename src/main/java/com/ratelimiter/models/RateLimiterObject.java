package com.ratelimiter.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RateLimiterObject {

    String key;
    Boolean isRateLimitActivated;
    int rateLimitPerMinute;

}
