package org.hongxi.redis.multi;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for multiple Redis clusters support.
 *
 * @author javahongxi
 */
@AutoConfiguration
@EnableConfigurationProperties(MultiRedisProperties.class)
public class MultiRedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RedisTemplateBuilder redisTemplateBuilder(MultiRedisProperties properties) {
        return new RedisTemplateBuilder(properties);
    }
}
