package org.hongxi.redis.multi.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Sample application demonstrating auto-register mode of multi-redis-spring-boot-starter.
 * <p>
 * <b>Prerequisites:</b> Start 3 standalone (6379/6380/6381) + 2 cluster (cache:7001-7006, session:7011-7016) before running.
 *
 * @author javahongxi
 */
@SpringBootApplication
public class SampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }
}
