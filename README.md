# Java rate-limiting library based on [Bucket4j](https://github.com/bucket4j/bucket4j)

This library provides functionality of annotation based rate limiting in your java projects.
You can use this library in distributed application via Redis. It also provides priority based
rate limiting as per the value given in rate limiting annotaion.

### Pre-Requisites to use this library
- Java 8
- Spring Boot Application
- Redis

### How to integrate it in your Java Spring Application

We have provided some hooks in order to integrate it in your application, you need to provide implementation or value to those hooks in order to make it work.

1. Dependencies to be added in your pom.xml file
```xml
<dependency>
    <groupId>com.ratelimiter</groupId>
    <artifactId>RateLimiter</artifactId>
    <version>1.0.0</version>
    <scope>system</scope>
    <systemPath>${basedir}/src/main/resources/libs/RateLimiter-1.0.0-SNAPSHOT.jar</systemPath>
</dependency>

<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>7.4.0</version>
</dependency>

<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-redis</artifactId>
    <version>7.4.0</version>
</dependency>

<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson</artifactId>
    <version>3.13.4</version>
</dependency>

<dependency>
    <groupId>org.apache.httpcomponents</groupId>
    <artifactId>httpclient</artifactId>
</dependency>
``` 

2. User need to provide an implementation of the configuration class [**RateLimitConfigProvider**](src/main/java/com/ratelimiter/configs/RateLimitConfigProvider.java), here you need to implement 3 methods:
   - getRateLimiterObjectFromCache
   - getRateLimiterObjectFromSource
   - transformSourceObjectToRateLimiterObject

    
Example:
```java
@Configuration
public class RateLimitConfig1 extends RateLimitConfigProvider<RateLimiterEntity> {

    @Autowired
    RateLimiterRepository rateLimiterRepository;

    @Autowired
    CacheService cacheService;

    public RateLimiterEntity getRateLimiterObjectFromCache(String s) {
        return cacheService.getObject(s, RateLimiterEntity.class);
    }

    public RateLimiterEntity getRateLimiterObjectFromSource(String rateLimitKey) {
        RateLimiterEntity rateLimiterEntity = rateLimiterRepository.findByRateLimitKey(rateLimitKey);
        cacheService.saveObject(rateLimitKey, rateLimiterEntity);
        return rateLimiterEntity;
    }

    public RateLimiterObject transformSourceObjectToRateLimiterObject(RateLimiterEntity rateLimiterEntity) {
        return RateLimiterObject.builder()
                .key(rateLimiterEntity.getRateLimitKey())
                .isRateLimitActivated(rateLimiterEntity.getIsRateLimitActivated())
                .rateLimit(rateLimiterEntity.getRateLimit())
                .timeUnit(ChronoUnit.valueOf(rateLimiterEntity.getRateLimitTimeUnit().toUpperCase()))
                .build();
    }

}
```

3. If user want to use some attribute present in an object as your Rate Limit key, then you have to extend that class with [**RateLimitKeyProvider**](src/main/java/com/ratelimiter/models/RateLimitKeyProvider.java).
Example:
```java
public class UserInfo implements RateLimitKeyProvider {
    private String firstName;
    private String lastName;
    private String panNumber;
    private String optionalAddress;
    private String postalCode;
    private UserDob dateOfBirth;
    private String phoneNum;
    private String email;
    private String gender;
    private AddressTypeEnum optionalAddressType;

    @Override
    public List<String> getRateLimitKeys() {
        return Arrays.asList(getPanNumber(), getPhoneNum());
    }
}

```
Here now you can have your pan number as your rate limit key.

4. User need to provide expiry time of bucket4j key present in Redis, this is configurable via application.yaml file. This is a time after which new bucket will get created.
Value of expiryTimeUnit should belong to only from - `SECONDS`, `MINUTES`, `HOURS`, `DAYS`
```yaml
ratelimiter:
  redisKey:
    expiry: '5'
    expiryTimeUnit: MINUTES
```
5. Now you can add [@RateLimiting](src/main/java/com/ratelimiter/annotations/RateLimiting.java) annotation before you API/function in order to limit its rate.






