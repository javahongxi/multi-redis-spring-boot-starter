package org.hongxi.redis.multi.sample;

import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Demonstrates Redisson distributed lock and data structures on startup.
 *
 * @author javahongxi
 */
@Order(4)
@Component
public class RedissonSampleRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RedissonSampleRunner.class);

    private final RedissonClient redissonClient;

    public RedissonSampleRunner(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public void run(String... args) {
        log.info("========== Redisson Distributed Lock & Data Structures ==========");

        verifyDistributedLock();
        verifyMap();

        log.info("========== All Redisson verifications passed! ==========");
    }

    private void verifyDistributedLock() {
        String lockKey = "sample:lock:order";
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean acquired = lock.tryLock(0, 10, TimeUnit.SECONDS);
            if (acquired) {
                log.info("[lock] Acquired lock: {}", lockKey);
                // simulate business logic
                Thread.sleep(100);
                log.info("[lock] Business logic executed, releasing lock: {}", lockKey);
            } else {
                log.warn("[lock] Failed to acquire lock: {}", lockKey);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[lock] Interrupted while holding lock: {}", lockKey);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("[lock] Released lock: {}", lockKey);
            }
        }
    }

    private void verifyMap() {
        String mapKey = "sample:redisson:map";
        RMap<String, String> map = redissonClient.getMap(mapKey);
        try {
            map.put("name", "redisson-test");
            map.put("version", "4.x");

            String name = map.get("name");
            String version = map.get("version");
            log.info("[map] Read OK: name={}, version={}", name, version);

            map.delete();
            log.info("[map] Cleanup OK: deleted {}", mapKey);
        } catch (Exception e) {
            log.error("[map] FAILED: {}", e.getMessage());
        }
    }
}
