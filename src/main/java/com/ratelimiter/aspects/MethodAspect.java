package com.ratelimiter.aspects;

import com.ratelimiter.annotations.RateLimiting;
import com.ratelimiter.cache.RateLimiterCache;
import com.ratelimiter.configs.RateLimitConfigProvider;
import com.ratelimiter.exceptions.RateLimitException;
import com.ratelimiter.models.RateLimitKeyProvider;
import com.ratelimiter.models.RateLimiterObject;
import com.ratelimiter.services.RateLimiterService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.ratelimiter.utils.constants.EMPTY_STRING;

@Slf4j
@Aspect
@Component
public class MethodAspect {

    @Autowired
    private RateLimiterService rateLimiter;

    @Autowired
    private RateLimiterCache rateLimiterCache;

    @Autowired
    BeanFactory beanFactory;

    @Around("@annotation(com.ratelimiter.annotations.RateLimiting)")
    public Object RateLimiter(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        List<Object> arguments = Arrays.stream(joinPoint.getArgs()).collect(Collectors.toList());
        RateLimiting rateLimitAnnotation = getRateLimitAnnotation(method);

        String keyObjectName = rateLimitAnnotation.keyObjectName();
        String defaultKey = rateLimitAnnotation.defaultKey();
        String beanName = rateLimitAnnotation.providerBeanName();

        RateLimitConfigProvider rateLimitConfigProvider = beanFactory.getBean(beanName, RateLimitConfigProvider.class);

        String rateLimitKey = StringUtils.hasLength(defaultKey) ? defaultKey : getRateLimitKey(joinPoint, arguments, keyObjectName);
        boolean isRateLimitReached = false;
        RateLimiterObject rateLimiterObject = rateLimitConfigProvider.getRateLimiterObject(rateLimitKey);

        if (rateLimiterObject.getIsRateLimitActivated()) {
            Bucket bucket = rateLimiter.resolveBucket(rateLimiterObject);
            ConsumptionProbe consumptionProbe = bucket.tryConsumeAndReturnRemaining(1);
            log.debug("Consumption Probe - Remaining tokens : {}, Nanos to fill : {}", consumptionProbe.getRemainingTokens(),
                    consumptionProbe.getNanosToWaitForRefill());
            isRateLimitReached = !consumptionProbe.isConsumed();
        }

        if (isRateLimitReached) {
            throw new RateLimitException(HttpStatus.TOO_MANY_REQUESTS.value(), HttpStatus.TOO_MANY_REQUESTS.toString());
        }

        return returnToFunction(joinPoint);
    }

    private RateLimiting getRateLimitAnnotation(Method method) {
        for (Annotation declaredAnnotation : method.getDeclaredAnnotations()) {
            if (declaredAnnotation instanceof RateLimiting) {
                return (RateLimiting) declaredAnnotation;
            }
        }
        return null;
    }

    private String getRateLimitKey(ProceedingJoinPoint joinPoint, List<Object> arguments, String keyObjectName) {
        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
        List<String> argumentNames = Arrays.stream(codeSignature.getParameterNames()).collect(Collectors.toList());
        Map<String, Object> keyValueMap = IntStream.range(0, argumentNames.size()).boxed().collect(Collectors.toMap(argumentNames::get, arguments::get));
        Object o = keyValueMap.get(keyObjectName);
        if (o instanceof RateLimitKeyProvider) {
            return ((RateLimitKeyProvider) o).getRateLimitKey();
        } else if (o instanceof String) {
            return o.toString();
        }
        return EMPTY_STRING;
    }

    private Object returnToFunction(ProceedingJoinPoint joinPoint) throws Throwable {
        Object retValue;
        try {
            retValue = joinPoint.proceed();
        } catch (Throwable ex) {
            throw ex;
        }
        return retValue;
    }
}
