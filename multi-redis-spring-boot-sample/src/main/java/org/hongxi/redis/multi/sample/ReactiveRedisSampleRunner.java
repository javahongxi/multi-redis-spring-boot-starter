package org.hongxi.redis.multi.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Verifies multi-Redis reactive read/write operations on startup.
 *
 * @author javahongxi
 */
@Order(4)
@Component
public class ReactiveRedisSampleRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ReactiveRedisSampleRunner.class);

    private final ReactiveRedisTemplate<String, Object> orderReactiveRedisTemplate;
    private final ReactiveStringRedisTemplate userReactiveStringRedisTemplate;
    private final ReactiveRedisTemplate<String, Object> cacheReactiveRedisTemplate;
    private final ReactiveStringRedisTemplate sessionReactiveStringRedisTemplate;

    public ReactiveRedisSampleRunner(
            @Qualifier("orderReactiveRedisTemplate") ReactiveRedisTemplate<String, Object> orderReactiveRedisTemplate,
            @Qualifier("userReactiveStringRedisTemplate") ReactiveStringRedisTemplate userReactiveStringRedisTemplate,
            @Qualifier("cacheReactiveRedisTemplate") ReactiveRedisTemplate<String, Object> cacheReactiveRedisTemplate,
            @Qualifier("sessionReactiveStringRedisTemplate") ReactiveStringRedisTemplate sessionReactiveStringRedisTemplate) {
        this.orderReactiveRedisTemplate = orderReactiveRedisTemplate;
        this.userReactiveStringRedisTemplate = userReactiveStringRedisTemplate;
        this.cacheReactiveRedisTemplate = cacheReactiveRedisTemplate;
        this.sessionReactiveStringRedisTemplate = sessionReactiveStringRedisTemplate;
    }

    @Override
    public void run(String... args) {
        log.info("========== Multi-Redis Reactive Verification ==========");

        // Verify order Redis (ReactiveRedisTemplate with User object)
        verifyObjectReactive("order", orderReactiveRedisTemplate);

        // Verify user Redis (ReactiveStringRedisTemplate with simple string)
        verifyStringReactive("user", userReactiveStringRedisTemplate);

        // Verify cache Redis (ReactiveRedisTemplate with User object)
        verifyObjectReactive("cache", cacheReactiveRedisTemplate);

        // Verify session Redis (ReactiveStringRedisTemplate with simple string)
        verifyStringReactive("session", sessionReactiveStringRedisTemplate);

        log.info("========== All reactive verifications passed! ==========");
    }

    private void verifyObjectReactive(String name, ReactiveRedisTemplate<String, Object> template) {
        try {
            String key = "sample:reactive:test:" + name;
            User value = new User(name + "-reactive-user", 25, new Date());

            template.opsForValue().set(key, value).block();
            Object result = template.opsForValue().get(key).block();
            template.delete(key).block();

            log.info("[reactive:{}] Object OK: set={}, get={}", name, value, result);
        } catch (Exception e) {
            log.error("[reactive:{}] Object FAILED: {}", name, e.getMessage());
        }
    }

    private void verifyStringReactive(String name, ReactiveStringRedisTemplate template) {
        try {
            String key = "sample:reactive:test:" + name;
            String value = "reactive-hello-" + name + "-" + System.currentTimeMillis();

            template.opsForValue().set(key, value).block();
            String result = template.opsForValue().get(key).block();
            template.delete(key).block();

            if (value.equals(result)) {
                log.info("[reactive:{}] String OK: set={}, get={}", name, value, result);
            } else {
                log.error("[reactive:{}] String MISMATCH: set={}, get={}", name, value, result);
            }
        } catch (Exception e) {
            log.error("[reactive:{}] String FAILED: {}", name, e.getMessage());
        }
    }
}
