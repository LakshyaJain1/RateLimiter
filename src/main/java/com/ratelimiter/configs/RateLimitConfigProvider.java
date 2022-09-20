package com.ratelimiter.configs;

import com.ratelimiter.exceptions.RateLimitException;
import com.ratelimiter.models.RateLimiterObject;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import com.ratelimiter.aspects.MethodAspect;

import static com.ratelimiter.utils.constants.KEY_NOT_FOUND;

/**
 * This configuration class need to be extended by the user and give the implementation of the
 * abstract methods in order to use Rate Limiting annotation.
 *
 * @param <T> This is generic object which user need to create in order to retrive
 *            object from the source.
 */

@Configuration
public abstract class RateLimitConfigProvider<T> {

    /**
     * User needs to implement this function in order to get the Rate Limiter Object
     * from their Cache.
     *
     * @param key Rate Limit key
     * @return Rate Limiter object
     */
    public abstract T getRateLimiterObjectFromCache(String key);

    /**
     * User needs to implement this function in order to get the Rate Limiter Object
     * from their source.
     *
     * @param key Rate Limit key
     * @return Rate Limiter object from source
     */
    public abstract T getRateLimiterObjectFromSource(String key);

    /**
     * User needs to implement this function in order to transform their source Rate Limiter
     * Object to Rate Limiter Object.
     *
     * @param rateLimiterObjectFromSource Rate Limiter object from source
     * @return returns Rate Limiter Object
     */
    public abstract RateLimiterObject transformSourceObjectToRateLimiterObject(T rateLimiterObjectFromSource);

    /**
     * This is generic function which we are using in {@link MethodAspect} class to get
     * Rate Limiter Object.
     *
     * @param key Rate Limiter key
     * @return Rate Limiter Object
     */
    public final RateLimiterObject getRateLimiterObject(String key) {
        RateLimiterObject rateLimiterObject;
        T rateLimiterObjectFromCache = getRateLimiterObjectFromCache(key);
        rateLimiterObject = rateLimiterObjectFromCache != null ? transformSourceObjectToRateLimiterObject(rateLimiterObjectFromCache) : null;
        if (rateLimiterObject == null) {
            T rateLimiterObjectFromSource;
            try {
                rateLimiterObjectFromSource = getRateLimiterObjectFromSource(key);
            } catch (Exception ex) {
                throw new RateLimitException(HttpStatus.BAD_REQUEST.value(), KEY_NOT_FOUND);
            }
            rateLimiterObject = transformSourceObjectToRateLimiterObject(rateLimiterObjectFromSource);
        }
        return rateLimiterObject;
    }

}
