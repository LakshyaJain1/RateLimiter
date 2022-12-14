package com.payufin.integration.ratelimiter.configs;

import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.redis.redisson.cas.RedissonBasedProxyManager;
import org.redisson.command.CommandSyncService;
import org.redisson.config.Config;
import org.redisson.config.ConfigSupport;
import org.redisson.connection.ConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Author - lakshya.jain <br>
 * Date - 09/10/2022
 * <p>
 * <p>
 * This redis configuration class is needed by Bucket4j to give
 * functionality of Rate Limiting in distributed system.
 */

@Configuration
public class RateLimiterRedisConfig {

    @Value("${rate-limiter.bucket4j-redisKey.expiry}")
    private int expiry;

    @Value("${rate-limiter.bucket4j-redisKey.expiryTimeUnit}")
    private String expiryTimeUnit;

    @Autowired
    private RedissonAddress redissonAddress;

    @Bean(destroyMethod = "shutdown")
    public ConnectionManager redissonConnectionManager() {
        Config config = new Config();
        config.useReplicatedServers().addNodeAddress(this.redissonAddress.getNodeAddresses().toArray(new String[this.redissonAddress.getNodeAddresses().size()]));
        return ConfigSupport.createConnectionManager(config);
    }

    @Bean("rateLimiterProxyManager")
    public RedissonBasedProxyManager proxyManager() throws IOException {
        CommandSyncService commandSyncService = new CommandSyncService(redissonConnectionManager());
        return new RedissonBasedProxyManager(commandSyncService,
                ClientSideConfig.getDefault(),
                Duration.of(expiry, ChronoUnit.valueOf(expiryTimeUnit.toUpperCase())));
    }

}
