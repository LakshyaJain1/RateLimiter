package com.payufin.integration.ratelimiter.models;

import java.util.List;

/**
 * This Rate Limit Key provider which need to implement by user in order to get the Rate limit key
 * from the user defined object.
 */

public interface RateLimitKeyProvider {

    List<String> getRateLimitKeys();

}
