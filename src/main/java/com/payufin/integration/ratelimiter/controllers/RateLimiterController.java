package com.payufin.integration.ratelimiter.controllers;

import com.payufin.integration.ratelimiter.configs.RateLimitConfigProvider;
import com.payufin.integration.ratelimiter.models.RateLimiterDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Author - lakshya.jain <br>
 * Date - 09/10/2022
 */

@RestController
@RequestMapping("/rateLimiter")
public class RateLimiterController<T> {

    @Autowired
    RateLimitConfigProvider<T> rateLimitConfigProvider;

    @PostMapping(value = "/insertRateLimiterEntity")
    public ResponseEntity<T> insertRateLimiterEntity(@RequestBody RateLimiterDto rateLimiterDto) {
        T rateLimiterEntity = rateLimitConfigProvider.transformRateLimiterDtoToRateLimiterEntity(rateLimiterDto);
        return new ResponseEntity<>(rateLimitConfigProvider.insertRateLimiterEntityToSource(rateLimiterEntity), HttpStatus.CREATED);
    }

    @PutMapping(value = "/updateRateLimiterEntity")
    public ResponseEntity<T> updateRateLimiterEntity(@RequestBody RateLimiterDto rateLimiterDto) {
        T rateLimiterEntity = rateLimitConfigProvider.transformRateLimiterDtoToRateLimiterEntity(rateLimiterDto);
        return new ResponseEntity<>(rateLimitConfigProvider.updateRateLimiterEntityToSource(rateLimiterEntity), HttpStatus.OK);
    }

    @GetMapping(value = "/getRateLimiterEntity")
    public ResponseEntity<T> getRateLimiterEntity(@RequestParam String rateLimitKey) {
        return new ResponseEntity<>(rateLimitConfigProvider.getRateLimiterEntityFromSource(rateLimitKey), HttpStatus.OK);
    }

    @DeleteMapping(value = "/deleteRateLimiterEntity")
    public ResponseEntity<String> deleteRateLimiterEntity(@RequestParam String rateLimitKey) {
        return new ResponseEntity<>(rateLimitConfigProvider.deleteRateLimiterEntityFromSource(rateLimitKey), HttpStatus.OK);
    }

}
