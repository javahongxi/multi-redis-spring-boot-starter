package org.hongxi.redis.multi.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Verifies multi-Redis {@link ReactiveRedisTemplate} (Object) reactive read/write operations.
 *
 * @author javahongxi
 */
@Order(6)
@Component
@Profile("!official")
public class ReactiveObjectSampleRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ReactiveObjectSampleRunner.class);

    private final ReactiveRedisTemplate<String, Object> orderReactiveRedisTemplate;
    private final ReactiveRedisTemplate<String, Object> cacheReactiveRedisTemplate;

    public ReactiveObjectSampleRunner(
            @Qualifier("orderReactiveRedisTemplate") ReactiveRedisTemplate<String, Object> orderReactiveRedisTemplate,
            @Qualifier("cacheReactiveRedisTemplate") ReactiveRedisTemplate<String, Object> cacheReactiveRedisTemplate) {
        this.orderReactiveRedisTemplate = orderReactiveRedisTemplate;
        this.cacheReactiveRedisTemplate = cacheReactiveRedisTemplate;
    }

    @Override
    public void run(String... args) {
        log.info("");
        log.info("========== Multi-Redis Reactive Object Verification ==========");

        verifyObjectReactive("order", orderReactiveRedisTemplate);
        verifyObjectReactive("cache", cacheReactiveRedisTemplate);

        log.info("========== All reactive object verifications passed! ==========");
    }

    private void verifyObjectReactive(String name, ReactiveRedisTemplate<String, Object> template) {
        try {
            String key = "sample:reactive:object:test:" + name;
            User value = new User(name + "-reactive-user", 25, new Date());

            template.opsForValue().set(key, value).block();
            Object result = template.opsForValue().get(key).block();
            template.delete(key).block();

            log.info("[reactive:{}] Object OK: set={}, get={}", name, value, result);
        } catch (Exception e) {
            log.error("[reactive:{}] Object FAILED: {}", name, e.getMessage());
        }
    }
}
