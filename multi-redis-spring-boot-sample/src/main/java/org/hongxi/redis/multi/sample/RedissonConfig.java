package org.hongxi.redis.multi.sample;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson configuration for distributed lock support.
 * Connects to the order Redis cluster (localhost:6379) by default.
 *
 * @author javahongxi
 */
@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.clusters.order.host:localhost}")
    private String host;

    @Value("${spring.data.redis.clusters.order.port:6379}")
    private int port;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                .setConnectionMinimumIdleSize(4)
                .setConnectionPoolSize(16);
        return Redisson.create(config);
    }
}
