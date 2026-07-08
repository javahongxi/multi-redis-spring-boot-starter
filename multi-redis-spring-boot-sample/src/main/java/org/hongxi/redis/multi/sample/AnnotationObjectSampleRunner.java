package org.hongxi.redis.multi.sample;

import org.hongxi.redis.multi.annotation.ConditionalOnAutoRegisterDisabled;
import org.hongxi.redis.multi.annotation.RedisCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Demonstrates {@code @RedisCluster} annotation injection with {@link RedisTemplate}.
 * <p>
 * Each field is automatically injected with the RedisTemplate for the specified cluster.
 * Only active when {@code spring.data.redis.auto-register=false} (Builder mode).
 *
 * @author javahongxi
 * @see org.hongxi.redis.multi.annotation.RedisCluster
 */
@Order(4)
@Component
@Profile("!official")
@ConditionalOnAutoRegisterDisabled
public class AnnotationObjectSampleRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AnnotationObjectSampleRunner.class);

    @RedisCluster("order")
    private RedisTemplate<String, Object> orderRedisTemplate;

    @RedisCluster("cache")
    private RedisTemplate<String, Object> cacheRedisTemplate;

    @Override
    public void run(String... args) {
        log.info("");
        log.info("========== @RedisCluster Object Mode Demo ==========");

        // order: standalone with User object
        User orderUser = new User("order-test", 25, new Date());
        orderRedisTemplate.opsForValue().set("annotation:order:test", orderUser);
        Object orderValue = orderRedisTemplate.opsForValue().get("annotation:order:test");
        log.info("[order] @RedisCluster Object demo: {}", orderValue);
        orderRedisTemplate.delete("annotation:order:test");

        // cache: cluster mode with User object
        User cacheUser = new User("cache-test", 30, new Date());
        cacheRedisTemplate.opsForValue().set("annotation:cache:test", cacheUser);
        Object cacheValue = cacheRedisTemplate.opsForValue().get("annotation:cache:test");
        log.info("[cache] @RedisCluster Object demo: {}", cacheValue);
        cacheRedisTemplate.delete("annotation:cache:test");

        log.info("========== @RedisCluster Object Mode Demo Complete ==========");
    }
}
