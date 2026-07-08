package org.hongxi.redis.multi.sample;

import org.hongxi.redis.multi.annotation.ConditionalOnAutoRegisterDisabled;
import org.hongxi.redis.multi.annotation.RedisCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Demonstrates {@code @RedisCluster} annotation injection with {@link StringRedisTemplate}.
 * <p>
 * Each field is automatically injected with the StringRedisTemplate for the specified cluster.
 * Only active when {@code spring.data.redis.auto-register=false} (Builder mode).
 *
 * @author javahongxi
 * @see org.hongxi.redis.multi.annotation.RedisCluster
 */
@Order(5)
@Component
@Profile("!official")
@ConditionalOnAutoRegisterDisabled
public class AnnotationStringSampleRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AnnotationStringSampleRunner.class);

    @RedisCluster("user")
    private StringRedisTemplate userStringRedisTemplate;

    @RedisCluster("session")
    private StringRedisTemplate sessionStringRedisTemplate;

    @Override
    public void run(String... args) {
        log.info("");
        log.info("========== @RedisCluster String Mode Demo ==========");

        // user: standalone with simple string
        userStringRedisTemplate.opsForValue().set("annotation:user:test", "Hello from @RedisCluster!");
        String userValue = userStringRedisTemplate.opsForValue().get("annotation:user:test");
        log.info("[user] @RedisCluster String demo: {}", userValue);
        userStringRedisTemplate.delete("annotation:user:test");

        // session: cluster mode with simple string
        sessionStringRedisTemplate.opsForValue().set("annotation:session:test", "Session cluster demo!");
        String sessionValue = sessionStringRedisTemplate.opsForValue().get("annotation:session:test");
        log.info("[session] @RedisCluster String demo: {}", sessionValue);
        sessionStringRedisTemplate.delete("annotation:session:test");

        log.info("========== @RedisCluster String Mode Demo Complete ==========");
    }
}
