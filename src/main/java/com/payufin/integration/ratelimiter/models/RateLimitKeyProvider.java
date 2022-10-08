package com.payufin.integration.ratelimiter.models;

import java.util.List;

/**
 * Author - lakshya.jain <br>
 * Date - 09/10/2022
 * <p>
 * <p>
 * This Rate Limit Key provider which need to implement by user in order to get the Rate limit key
 * from the user defined object.
 */

public interface RateLimitKeyProvider {

    List<String> getRateLimitKeys();

}
