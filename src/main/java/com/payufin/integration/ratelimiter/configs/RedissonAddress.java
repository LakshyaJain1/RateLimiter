package com.payufin.integration.ratelimiter.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Author - lakshya.jain <br>
 * Date - 09/10/2022
 */

@Configuration
@ConfigurationProperties(prefix = "redisson")
@Data
public class RedissonAddress {

    private List<String> nodeAddresses = new ArrayList<>();

    public List<String> getNodeAddresses() {
        return this.nodeAddresses;
    }

}
