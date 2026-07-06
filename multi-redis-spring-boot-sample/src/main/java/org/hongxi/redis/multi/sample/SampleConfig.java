package org.hongxi.redis.multi.sample;

import org.hongxi.redis.multi.RedisTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Demonstrates how to create multiple RedisTemplate instances
 * for different Redis clusters using the Builder pattern.
 *
 * @author javahongxi
 */
@Configuration
public class SampleConfig {

    @Bean
    public RedisTemplate<String, Object> orderRedisTemplate(RedisTemplateBuilder builder) {
        return builder.cluster("order");
    }

    @Bean
    public RedisTemplate<String, Object> userRedisTemplate(RedisTemplateBuilder builder) {
        return builder.cluster("user");
    }
}
