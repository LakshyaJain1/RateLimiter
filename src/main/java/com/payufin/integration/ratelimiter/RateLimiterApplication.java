package com.payufin.integration.ratelimiter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages={"com.ratelimiter", "com.lazypay.aspects"})
public class RateLimiterApplication {

    public RateLimiterApplication() {
    }

    public static void main(String[] args) {
        SpringApplication.run(RateLimiterApplication.class, args);
    }

}
