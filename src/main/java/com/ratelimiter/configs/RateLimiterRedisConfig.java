package com.ratelimiter.configs;

import com.lazypay.aspects.lazyPayLogging.configs.RedissonParamConfig;
import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.redis.redisson.cas.RedissonBasedProxyManager;
import org.redisson.command.CommandSyncService;
import org.redisson.config.Config;
import org.redisson.config.ConfigSupport;
import org.redisson.connection.ConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;

@Configuration
public class RateLimiterRedisConfig {

    @Autowired
    private RedissonParamConfig redissonParamConfig;

    @Bean(destroyMethod = "shutdown")
    public ConnectionManager redissonConnectionManager() {
        Config config = new Config();
        config.useReplicatedServers().addNodeAddress(this.redissonParamConfig.getNodeAddresses().toArray(new String[this.redissonParamConfig.getNodeAddresses().size()]));
        return ConfigSupport.createConnectionManager(config);
    }

    @Bean("rateLimiterProxyManager")
    public RedissonBasedProxyManager proxyManager() throws IOException {
        CommandSyncService commandSyncService = new CommandSyncService(redissonConnectionManager());
        return new RedissonBasedProxyManager(commandSyncService,
                ClientSideConfig.getDefault(),
                Duration.ofMinutes(10));
    }

}
