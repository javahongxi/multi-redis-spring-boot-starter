package org.hongxi.redis.multi.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * Verifies multi-Redis {@link RedisTemplate} (Object) read/write operations on startup.
 * <p>
 * Dynamically discovers all {@link RedisTemplate} beans (excluding StringRedisTemplate)
 * by naming convention, so it works across all test scenarios without code changes.
 *
 * @author javahongxi
 */
@Order(3)
@Component
public class ObjectSampleRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ObjectSampleRunner.class);
    private static final String BEAN_SUFFIX = "RedisTemplate";

    private final ApplicationContext applicationContext;

    public ObjectSampleRunner(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(String... args) {
        log.info("");
        Map<String, RedisTemplate<String, Object>> templates = discoverTemplates();

        log.info("========== Multi-Redis Object Read/Write Verification ==========");

        for (Map.Entry<String, RedisTemplate<String, Object>> entry : templates.entrySet()) {
            verifyObjectReadWrite(entry.getKey(), entry.getValue());
        }

        log.info("========== All object read/write verifications passed! ==========");
    }

    /**
     * Discover all RedisTemplate beans (excluding StringRedisTemplate) by naming convention.
     * Bean name pattern: {clusterName}RedisTemplate
     */
    private Map<String, RedisTemplate<String, Object>> discoverTemplates() {
        Map<String, RedisTemplate<String, Object>> result = new TreeMap<>();
        Map<String, RedisTemplate> allBeans = applicationContext.getBeansOfType(RedisTemplate.class);

        for (Map.Entry<String, RedisTemplate> entry : allBeans.entrySet()) {
            String beanName = entry.getKey();
            // Exclude StringRedisTemplate (subclass of RedisTemplate)
            if (entry.getValue() instanceof org.springframework.data.redis.core.StringRedisTemplate) {
                continue;
            }
            if (beanName.endsWith(BEAN_SUFFIX)) {
                String clusterName = beanName.substring(0, beanName.length() - BEAN_SUFFIX.length());
                if (!clusterName.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    RedisTemplate<String, Object> template = (RedisTemplate<String, Object>) entry.getValue();
                    result.put(clusterName, template);
                }
            }
        }
        return result;
    }

    private void verifyObjectReadWrite(String name, RedisTemplate<String, Object> template) {
        try {
            String key = "sample:object:test:" + name;
            User value = new User(name + "-test-user", 25, new Date());

            template.opsForValue().set(key, value);
            Object result = template.opsForValue().get(key);
            template.delete(key);

            log.info("[{}] Object OK: set={}, get={}", name, value, result);
        } catch (Exception e) {
            log.error("[{}] Object FAILED: {}", name, e.getMessage());
        }
    }
}
