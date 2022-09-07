package com.ratelimiter.models;

/**
 * This Rate Limit Key provider which need to implement by user in order to get the Rate limit key
 * from the user defined object.
 */

public interface RateLimitKeyProvider {

    String getRateLimitKey();

}
