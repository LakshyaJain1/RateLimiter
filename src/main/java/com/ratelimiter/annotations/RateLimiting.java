package com.ratelimiter.annotations;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation that can be used to rate limit you API/function calls based on the
 * given key.
 */

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MultiRateLimiting.class)
public @interface RateLimiting {

    String keyObjectName() default "";

    String defaultKey() default "";

    String providerBeanName();

}