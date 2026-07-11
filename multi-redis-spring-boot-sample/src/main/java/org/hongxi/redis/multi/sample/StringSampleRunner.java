package org.hongxi.redis.multi.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Verifies multi-Redis read/write operations on startup.
 *
 * @author javahongxi
 */
@Component
public class StringSampleRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StringSampleRunner.class);

    private final StringRedisTemplate defaultStringRedisTemplate;
    private final StringRedisTemplate orderStringRedisTemplate;
    private final StringRedisTemplate userStringRedisTemplate;
    private final StringRedisTemplate cacheStringRedisTemplate;
    private final StringRedisTemplate sessionStringRedisTemplate;

    public StringSampleRunner(StringRedisTemplate defaultStringRedisTemplate,
                              StringRedisTemplate orderStringRedisTemplate,
                              StringRedisTemplate userStringRedisTemplate,
                              StringRedisTemplate cacheStringRedisTemplate,
                              StringRedisTemplate sessionStringRedisTemplate) {
        this.defaultStringRedisTemplate = defaultStringRedisTemplate;
        this.orderStringRedisTemplate = orderStringRedisTemplate;
        this.userStringRedisTemplate = userStringRedisTemplate;
        this.cacheStringRedisTemplate = cacheStringRedisTemplate;
        this.sessionStringRedisTemplate = sessionStringRedisTemplate;
    }

    @Override
    public void run(String... args) {
        log.info("");
        log.info("========== Multi-Redis Read/Write Verification ==========");

        verifyStringReadWrite("default", defaultStringRedisTemplate);
        verifyStringReadWrite("order", orderStringRedisTemplate);
        verifyStringReadWrite("user", userStringRedisTemplate);
        verifyStringReadWrite("cache", cacheStringRedisTemplate);
        verifyStringReadWrite("session", sessionStringRedisTemplate);

        log.info("========== All read/write verifications passed! ==========");
    }

    private void verifyStringReadWrite(String name, StringRedisTemplate template) {
        try {
            String key = "sample:test:" + name;
            String value = "hello-" + name + "-" + System.currentTimeMillis();

            template.opsForValue().set(key, value);
            String result = template.opsForValue().get(key);
            template.delete(key);

            if (value.equals(result)) {
                log.info("[{}] Read/Write OK: set={}, get={}", name, value, result);
            } else {
                log.error("[{}] Read/Write MISMATCH: set={}, get={}", name, value, result);
            }
        } catch (Exception e) {
            log.error("[{}] Read/Write FAILED: {}", name, e.getMessage());
        }
    }
}
