package com.payufin.integration.ratelimiter.configs;

import com.payufin.integration.ratelimiter.exceptions.RateLimitException;
import com.payufin.integration.ratelimiter.models.RateLimiterDto;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import com.payufin.integration.ratelimiter.aspects.MethodAspect;

import static com.payufin.integration.ratelimiter.utils.constants.KEY_NOT_FOUND;

/**
 * This configuration class need to be extended by the user and give the implementation of the
 * abstract methods in order to use Rate Limiting annotation.
 *
 * @param <T> This is generic object which user need to create in order to retrieve
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
    public abstract T getRateLimiterEntityFromCache(String key);

    /**
     * User needs to implement this function in order to save the Rate Limiter Object
     * to their Cache.
     *
     * @param key Rate Limit key
     */
    public abstract void saveRateLimiterEntityToCache(String key, T rateLimitEntity);

    /**
     * User needs to implement this function in order to delete the Rate Limiter Object
     * from their Cache.
     *
     * @param key Rate Limit key
     */
    public abstract void deleteRateLimiterEntityFromCache(String key);

    /**
     * User needs to implement this function in order to get the Rate Limiter Object
     * from their source.
     *
     * @param key Rate Limit key
     * @return Rate Limiter object from source
     */
    public abstract T getRateLimiterEntityFromSource(String key);

    /**
     * User needs to implement this function in order to insert the Rate Limiter Object
     * to their source.
     *
     * @param rateLimitEntity Rate Entity
     * @return Rate Limiter Entity from source
     */
    public abstract T insertRateLimiterEntityToSource(T rateLimitEntity);

    /**
     * User needs to implement this function in order to update the Rate Limiter Object
     * to their source.
     *
     * @param rateLimitEntity Rate Entity
     * @return Rate Limiter Entity from source
     */
    public abstract T updateRateLimiterEntityToSource(T rateLimitEntity);

    /**
     * User needs to implement this function in order to delete the Rate Limiter Object
     * to their source.
     *
     * @param key Rate Entity
     * @return 1 if deleted successfully
     */
    public abstract String deleteRateLimiterEntityFromSource(String key);

    /**
     * User needs to implement this function in order to transform their source Rate Limiter
     * Object to Rate Limiter Object.
     *
     * @param rateLimiterEntity Rate Limiter object from source
     * @return returns Rate Limiter Object
     */
    public abstract RateLimiterDto transformRateLimiterEntityToRateLimiterDto(T rateLimiterEntity);

    /**
     * User needs to implement this function in order to transform Rate Limiter
     * dto to Rate Limiter entity.
     *
     * @param rateLimiterDto Rate Limiter object from source
     * @return returns Rate Limiter entity
     */
    public abstract T transformRateLimiterDtoToRateLimiterEntity(RateLimiterDto rateLimiterDto);

    /**
     * This is generic function which we are using in {@link MethodAspect} class to get
     * Rate Limiter Object.
     *
     * @param key Rate Limiter key
     * @return Rate Limiter Object
     */
    public final RateLimiterDto getRateLimiterDto(String key) {
        RateLimiterDto rateLimiterDto;
        T rateLimiterEntityFromCache = getRateLimiterEntityFromCache(key);
        rateLimiterDto = rateLimiterEntityFromCache != null ? transformRateLimiterEntityToRateLimiterDto(rateLimiterEntityFromCache) : null;
        if (rateLimiterDto == null) {
            T rateLimiterEntityFromSource;
            try {
                rateLimiterEntityFromSource = getRateLimiterEntityFromSource(key);
            } catch (Exception ex) {
                throw new RateLimitException(HttpStatus.BAD_REQUEST.value(), KEY_NOT_FOUND);
            }
            rateLimiterDto = transformRateLimiterEntityToRateLimiterDto(rateLimiterEntityFromSource);
        }
        return rateLimiterDto;
    }

}
