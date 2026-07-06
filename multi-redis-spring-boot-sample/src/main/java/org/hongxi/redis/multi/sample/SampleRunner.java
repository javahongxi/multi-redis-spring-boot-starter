package org.hongxi.redis.multi.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Verifies multi-Redis connectivity on startup.
 *
 * @author javahongxi
 */
@Component
public class SampleRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SampleRunner.class);

    private final StringRedisTemplate orderStringRedisTemplate;
    private final StringRedisTemplate userStringRedisTemplate;
    private final StringRedisTemplate cacheStringRedisTemplate;
    private final StringRedisTemplate sessionStringRedisTemplate;

    private final RedisTemplate<String, Object> cacheRedisTemplate;

    public SampleRunner(StringRedisTemplate orderStringRedisTemplate,
                        StringRedisTemplate userStringRedisTemplate,
                        StringRedisTemplate cacheStringRedisTemplate,
                        StringRedisTemplate sessionStringRedisTemplate,
                        RedisTemplate<String, Object> cacheRedisTemplate) {
        this.orderStringRedisTemplate = orderStringRedisTemplate;
        this.userStringRedisTemplate = userStringRedisTemplate;
        this.cacheStringRedisTemplate = cacheStringRedisTemplate;
        this.sessionStringRedisTemplate = sessionStringRedisTemplate;
        this.cacheRedisTemplate = cacheRedisTemplate;
    }

    @Override
    public void run(String... args) {
        log.info("========== Multi-Redis Sample Verification ==========");

        // Verify order Redis standalone (port 6379)
        verifyRedis("order", orderStringRedisTemplate);

        // Verify user Redis standalone (port 6380)
        verifyRedis("user", userStringRedisTemplate);

        // Verify cache Redis cluster (port 7001-7003)
        verifyRedis("cache", cacheStringRedisTemplate);

        // Verify session Redis cluster (port 7011-7013)
        verifyRedis("session", sessionStringRedisTemplate);

        log.info("========== All verifications passed! ==========");

        // Verify RedisTemplate
        User user = new User("lily", 20, new Date());
        cacheRedisTemplate.opsForValue().set("user", user);
        log.info("user: {}", cacheRedisTemplate.opsForValue().get("user"));
//        cacheRedisTemplate.delete("user");
    }

    private void verifyRedis(String name, StringRedisTemplate template) {
        try {
            String key = "sample:test:" + name;
            String value = "hello-" + name + "-" + System.currentTimeMillis();

            template.opsForValue().set(key, value);
            Object result = template.opsForValue().get(key);
            template.delete(key);

            if (value.equals(result)) {
                log.info("[{}] Redis OK: set={}, get={}", name, value, result);
            } else {
                log.error("[{}] Redis MISMATCH: set={}, get={}", name, value, result);
            }
        } catch (Exception e) {
            log.error("[{}] Redis FAILED: {}", name, e.getMessage());
        }
    }
}
