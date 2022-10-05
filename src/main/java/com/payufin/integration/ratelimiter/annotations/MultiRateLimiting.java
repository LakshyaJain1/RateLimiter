package com.payufin.integration.ratelimiter.annotations;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Helper annotation which helps to wrap the Multiple Rate Limiting annotations.
 */


@Retention(RetentionPolicy.RUNTIME)
public @interface MultiRateLimiting {

    RateLimiting[] value();

}
