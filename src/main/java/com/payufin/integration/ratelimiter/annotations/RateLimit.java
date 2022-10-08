package com.payufin.integration.ratelimiter.annotations;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Author - lakshya.jain <br>
 * Date - 09/10/2022
 * <p>
 * Annotation that can be used to rate limit your API/function calls based on the
 * given key.
 * </p>
 */

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MultiRateLimit.class)
public @interface RateLimit {

    String keyObjectName() default "";

    String defaultKey() default "";

    String providerBeanName();

    int priority() default Integer.MAX_VALUE;

}
