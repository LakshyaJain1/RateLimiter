package com.payufin.integration.ratelimiter.aspects;

import com.payufin.integration.ratelimiter.annotations.RateLimit;
import com.payufin.integration.ratelimiter.configs.RateLimitConfigProvider;
import com.payufin.integration.ratelimiter.exceptions.RateLimitException;
import com.payufin.integration.ratelimiter.models.RateLimitKeyProvider;
import com.payufin.integration.ratelimiter.models.RateLimiterDto;
import com.payufin.integration.ratelimiter.services.RateLimiterService;
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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.payufin.integration.ratelimiter.utils.constants.EMPTY_STRING;
import static com.payufin.integration.ratelimiter.utils.constants.RATE_LIMIT_EXCEEDED;

/**
 * Author - lakshya.jain <br>
 * Date - 09/10/2022
 * <p>
 * <p>
 * This is Aspect class where we intercept the RateLimiting annotation and check
 * whether Limit is exceeded or not.
 */

@Slf4j
@Aspect
@Component
public class MethodAspect {

    @Autowired
    private RateLimiterService rateLimiter;

    @Autowired
    BeanFactory beanFactory;

    /**
     * MultiRateLimiter is called when you use multiple RateLimiting annotation
     * over a function.
     *
     * @param joinPoint joinPoint exposes the proceed(..) method in order to support around advice
     * @return returns to the Annotated function
     * @throws Throwable throws exception when rate limit exceeded
     */
    @Around("@annotation(com.payufin.integration.ratelimiter.annotations.MultiRateLimit)")
    public Object MultiRateLimiter(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        List<Object> arguments = Arrays.stream(joinPoint.getArgs()).collect(Collectors.toList());
        List<RateLimit> rateLimitAnnotations = Arrays.asList(method.getAnnotationsByType(RateLimit.class));

        rateLimitAnnotations = rateLimitAnnotations.stream()
                .sorted(Comparator.comparing(RateLimit::priority).thenComparing(RateLimit::keyObjectName))
                .collect(Collectors.toList());

        for (RateLimit rateLimitAnnotation : rateLimitAnnotations) {
            checkRateLimit(joinPoint, arguments, rateLimitAnnotation);
        }

        return returnToFunction(joinPoint);
    }

    /**
     * RateLimiter is called when you use single RateLimiting annotation
     * over a function.
     *
     * @param joinPoint joinPoint exposes the proceed(..) method in order to support around advice
     * @return returns to the Annotated function
     * @throws Throwable throws exception when rate limit exceeded
     */
    @Around("@annotation(com.payufin.integration.ratelimiter.annotations.RateLimit)")
    public Object RateLimiter(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        List<Object> arguments = Arrays.stream(joinPoint.getArgs()).collect(Collectors.toList());
        RateLimit rateLimitAnnotation = method.getAnnotation(RateLimit.class);
        checkRateLimit(joinPoint, arguments, rateLimitAnnotation);
        return returnToFunction(joinPoint);
    }

    /**
     * This is generic function to check the Rate Limit w.r.t the given RateLimit Annotation.
     *
     * @param joinPoint           joinPoint exposes the proceed(..) method in order to support around advice
     * @param arguments           List of arguments given in the function
     * @param rateLimitAnnotation RateLimit Annotation present over a function
     */
    private void checkRateLimit(ProceedingJoinPoint joinPoint, List<Object> arguments, RateLimit rateLimitAnnotation) {

        String keyObjectName = rateLimitAnnotation.keyObjectName();
        String defaultKey = rateLimitAnnotation.defaultKey();
        String beanName = rateLimitAnnotation.providerBeanName();

        RateLimitConfigProvider rateLimitConfigProvider = beanFactory.getBean(beanName, RateLimitConfigProvider.class);

        List<String> rateLimitKeys = StringUtils.hasLength(defaultKey) ? Collections.singletonList(defaultKey) : getRateLimitKeys(joinPoint, arguments, keyObjectName);

        for (String rateLimitKey : rateLimitKeys) {
            boolean isRateLimitReached = false;
            RateLimiterDto rateLimiterDto = rateLimitConfigProvider.getRateLimiterDto(rateLimitKey);

            if (rateLimiterDto == null) {
                continue;
            }

            if (rateLimiterDto.isActive()) {
                Bucket bucket = rateLimiter.resolveBucket(rateLimiterDto);
                ConsumptionProbe consumptionProbe = bucket.tryConsumeAndReturnRemaining(1);
                log.debug("Consumption Probe - Remaining tokens : {}, Nanos to fill : {}", consumptionProbe.getRemainingTokens(),
                        consumptionProbe.getNanosToWaitForRefill());
                isRateLimitReached = !consumptionProbe.isConsumed();
            }

            if (isRateLimitReached) {
                throw new RateLimitException(HttpStatus.TOO_MANY_REQUESTS.value(),
                        RATE_LIMIT_EXCEEDED + ", " + String.format("Rate for %s is limited to %s requests per %s",
                                rateLimiterDto.getKey(), rateLimiterDto.getMaxLimit(), rateLimiterDto.getTimeUnit().toString()));
            }
        }
    }

    /**
     * @param joinPoint     joinPoint exposes the proceed(..) method in order to support around advice
     * @param arguments     List of arguments given in the function
     * @param keyObjectName Name of object present in the arguments of function from which
     *                      Rate Limiter can get the Rate Limit Key
     * @return return Rate Limit Key
     */
    private List<String> getRateLimitKeys(ProceedingJoinPoint joinPoint, List<Object> arguments, String keyObjectName) {
        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
        List<String> argumentNames = Arrays.stream(codeSignature.getParameterNames()).collect(Collectors.toList());
        Map<String, Object> keyValueMap = IntStream.range(0, argumentNames.size()).boxed().collect(Collectors.toMap(argumentNames::get, arguments::get));
        Object o = keyValueMap.get(keyObjectName);
        if (o instanceof RateLimitKeyProvider) {
            return ((RateLimitKeyProvider) o).getRateLimitKeys();
        } else if (o instanceof String) {
            return Collections.singletonList(o.toString());
        }
        return Collections.singletonList(EMPTY_STRING);
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
