package com.ratelimiter.aspects;

import com.ratelimiter.annotations.RateLimiting;
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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.ratelimiter.utils.constants.EMPTY_STRING;
import static com.ratelimiter.utils.constants.RATE_LIMIT_EXCEEDED;

/**
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
    @Around("@annotation(com.ratelimiter.annotations.MultiRateLimiting)")
    public Object MultiRateLimiter(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        List<Object> arguments = Arrays.stream(joinPoint.getArgs()).collect(Collectors.toList());
        List<RateLimiting> rateLimitAnnotations = Arrays.asList(method.getAnnotationsByType(RateLimiting.class));

        rateLimitAnnotations = rateLimitAnnotations.stream()
                .sorted(Comparator.comparing(RateLimiting::priority).thenComparing(RateLimiting::keyObjectName))
                .collect(Collectors.toList());

        for (RateLimiting rateLimitAnnotation : rateLimitAnnotations) {
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
    @Around("@annotation(com.ratelimiter.annotations.RateLimiting)")
    public Object RateLimiter(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        List<Object> arguments = Arrays.stream(joinPoint.getArgs()).collect(Collectors.toList());
        RateLimiting rateLimitAnnotation = method.getAnnotation(RateLimiting.class);
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
    private void checkRateLimit(ProceedingJoinPoint joinPoint, List<Object> arguments, RateLimiting rateLimitAnnotation) {

        String keyObjectName = rateLimitAnnotation.keyObjectName();
        String defaultKey = rateLimitAnnotation.defaultKey();
        String beanName = rateLimitAnnotation.providerBeanName();

        RateLimitConfigProvider rateLimitConfigProvider = beanFactory.getBean(beanName, RateLimitConfigProvider.class);

        List<String> rateLimitKeys = StringUtils.hasLength(defaultKey) ? Collections.singletonList(defaultKey) : getRateLimitKeys(joinPoint, arguments, keyObjectName);

        for (String rateLimitKey : rateLimitKeys) {
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
                throw new RateLimitException(HttpStatus.TOO_MANY_REQUESTS.value(),
                        RATE_LIMIT_EXCEEDED + ", " + String.format("Rate for %s is limited to %s requests per %s",
                                rateLimiterObject.getKey(), rateLimiterObject.getRateLimit(), rateLimiterObject.getTimeUnit().toString()));
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
