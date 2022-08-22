package com.ratelimiter.aspects;

import com.ratelimiter.annotations.RateLimiting;
import com.ratelimiter.cache.RateLimiterCache;
import com.ratelimiter.exceptions.RateLimitException;
import com.ratelimiter.models.RateLimiterObject;
import com.ratelimiter.services.RateLimiterService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class MethodAspect {

    @Autowired
    private RateLimiterService rateLimiter;

    @Autowired
    private RateLimiterCache rateLimiterCache;

    @Around("@annotation(com.ratelimiter.annotations.RateLimiting)")
    public Object RateLimiter(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object[] arguments = joinPoint.getArgs();
        String key = "";

        for (Annotation declaredAnnotation : method.getDeclaredAnnotations()) {
            if (declaredAnnotation instanceof RateLimiting) {
                RateLimiting identifier = (RateLimiting) declaredAnnotation;
                key = identifier.key();
                break;
            }
        }

        if (key.isEmpty()) {
            key = arguments[0].toString();
        }

        boolean isRateLimitReached = false;

        RateLimiterObject rateLimiterObject = rateLimiterCache.get(key);
        Boolean isRateLimitActivated = rateLimiterObject.getIsRateLimitActivated();

        if (isRateLimitActivated) {
            Bucket bucket = rateLimiter.resolveBucket(rateLimiterObject);
            ConsumptionProbe consumptionProbe = bucket.tryConsumeAndReturnRemaining(1);
            log.debug("Consumption Probe - Remaining tokens : {}, Nanos to fill : {}", consumptionProbe.getRemainingTokens(),
                    consumptionProbe.getNanosToWaitForRefill());
            isRateLimitReached = !consumptionProbe.isConsumed();
        }

        if (isRateLimitReached) {
            throw new RateLimitException(HttpStatus.TOO_MANY_REQUESTS.value(), HttpStatus.TOO_MANY_REQUESTS.toString());
        }

        Object retValue;
        try {
            retValue = joinPoint.proceed();
        } catch (Exception ex) {
            throw ex;
        }
        return retValue;
    }
}
