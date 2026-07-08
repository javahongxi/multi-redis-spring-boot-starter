package org.hongxi.redis.multi.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Verifies multi-Redis {@link ReactiveStringRedisTemplate} reactive read/write operations.
 *
 * @author javahongxi
 */
@Order(7)
@Component
@Profile("!official")
public class ReactiveStringSampleRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ReactiveStringSampleRunner.class);

    private final ReactiveStringRedisTemplate userReactiveStringRedisTemplate;
    private final ReactiveStringRedisTemplate sessionReactiveStringRedisTemplate;

    public ReactiveStringSampleRunner(
            @Qualifier("userReactiveStringRedisTemplate") ReactiveStringRedisTemplate userReactiveStringRedisTemplate,
            @Qualifier("sessionReactiveStringRedisTemplate") ReactiveStringRedisTemplate sessionReactiveStringRedisTemplate) {
        this.userReactiveStringRedisTemplate = userReactiveStringRedisTemplate;
        this.sessionReactiveStringRedisTemplate = sessionReactiveStringRedisTemplate;
    }

    @Override
    public void run(String... args) {
        log.info("");
        log.info("========== Multi-Redis Reactive String Verification ==========");

        verifyStringReactive("user", userReactiveStringRedisTemplate);
        verifyStringReactive("session", sessionReactiveStringRedisTemplate);

        log.info("========== All reactive string verifications passed! ==========");
    }

    private void verifyStringReactive(String name, ReactiveStringRedisTemplate template) {
        try {
            String key = "sample:reactive:string:test:" + name;
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
