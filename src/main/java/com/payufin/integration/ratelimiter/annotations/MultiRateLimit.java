package com.payufin.integration.ratelimiter.annotations;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Author - lakshya.jain <br>
 * Date - 09/10/2022
 *
 * <p>
 * Helper annotation which helps to wrap the Multiple Rate Limit annotations.
 * </p>
 */


@Retention(RetentionPolicy.RUNTIME)
public @interface MultiRateLimit {

    RateLimit[] value();

}
