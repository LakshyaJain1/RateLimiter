package com.ratelimiter.configs;

import com.ratelimiter.exceptions.RateLimitException;
import com.ratelimiter.models.RateLimiterObject;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static com.ratelimiter.utils.constants.KEY_NOT_FOUND;

@Configuration
public abstract class RateLimitConfigProvider<T> {

    public abstract RateLimiterObject getRateLimiterObjectFromCache(String key);

    public abstract T getRateLimiterObjectFromDb(String key);

    public abstract RateLimiterObject getRateLimiterObjectFromDbObject(T o);

    public final RateLimiterObject getRateLimiterObject(String key) {
        RateLimiterObject rateLimiterObject;
        rateLimiterObject = getRateLimiterObjectFromCache(key);
        if (rateLimiterObject == null) {
            T rateLimiterObjectFromDb;
            try {
                rateLimiterObjectFromDb = getRateLimiterObjectFromDb(key);
            } catch (NullPointerException ex) {
                throw new RateLimitException(HttpStatus.BAD_REQUEST.value(), KEY_NOT_FOUND);
            }
            rateLimiterObject = getRateLimiterObjectFromDbObject(rateLimiterObjectFromDb);
        }
        return rateLimiterObject;
    }

}
