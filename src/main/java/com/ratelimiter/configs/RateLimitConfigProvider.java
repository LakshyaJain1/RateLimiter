package com.ratelimiter.configs;

import com.ratelimiter.exceptions.RateLimitException;
import com.ratelimiter.models.RateLimiterObject;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import static com.ratelimiter.utils.constants.KEY_NOT_FOUND;

/**
 * This configuration class need to be extended by the user and give the implementation of the
 * abstract methods in order to use Rate Limiting annotation.
 *
 * @param <T> This is generic object which user need to create in order to retrive
 *           object from the DB.
 */

@Configuration
public abstract class RateLimitConfigProvider<T> {

    public abstract RateLimiterObject getRateLimiterObjectFromCache(String key);

    public abstract T getRateLimiterObjectFromDb(String key);

    public abstract RateLimiterObject transformDBObjectToRateLimiterObject(T o);

    public final RateLimiterObject getRateLimiterObject(String key) {
        RateLimiterObject rateLimiterObject;
        rateLimiterObject = getRateLimiterObjectFromCache(key);
        if (rateLimiterObject == null) {
            T rateLimiterObjectFromDb;
            try {
                rateLimiterObjectFromDb = getRateLimiterObjectFromDb(key);
            } catch (Exception ex) {
                throw new RateLimitException(HttpStatus.BAD_REQUEST.value(), KEY_NOT_FOUND);
            }
            rateLimiterObject = transformDBObjectToRateLimiterObject(rateLimiterObjectFromDb);
        }
        return rateLimiterObject;
    }

}
