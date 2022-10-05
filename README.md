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
   <groupId>com.payufin.integration.rateLimiter</groupId>
   <artifactId>payufin.integration.rateLimiter</artifactId>
   <version>1.0-SNAPSHOT</version>
</dependency>


<repositories>
<repository>
   <id>libs-release-local</id>
   <name>libs-release-local</name>
   <url>https://jfrog-artifactory.lazypay.in:443/artifactory/libs-release-local/</url>
</repository>
<repository>
   <id>libs-snapshot-local</id>
   <name>libs-snapshot-local</name>
   <url>https://jfrog-artifactory.lazypay.in:443/artifactory/libs-snapshot-local/</url>
</repository>
</repositories>
``` 

2. User need to provide an implementation of the configuration class [**RateLimitConfigProvider**](src/main/java/com/payufin/configs/RateLimitConfigProvider.java)

3. If user want to use some attribute present in an object as your Rate Limit key, then you have to extend that class with [**RateLimitKeyProvider**](src/main/java/com/payufin/models/RateLimitKeyProvider.java).
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

5. You can hit below curl to insert the RateLimitKey inside your DB
```
curl --location --request POST 'http://localhost:8080/rateLimiter/insertRateLimiterEntity' \
--header 'Content-Type: application/json' \
--data-raw '{
    "key": "temp1",
    "active": true,
    "maxLimit": 5,
    "timeUnit" : "MINUTES"
}'
```

6. Now you can add [@RateLimiting](src/main/java/com/payufin/annotations/RateLimiting.java) annotation before you API/function in order to limit its rate.

