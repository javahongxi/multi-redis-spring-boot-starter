package org.hongxi.redis.multi.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;

/**
 * Verifies multi-Redis read/write operations on startup.
 * <p>
 * Dynamically discovers all {@link StringRedisTemplate} beans by naming convention,
 * so it works across all test scenarios without code changes.
 *
 * @author javahongxi
 */
@Order(2)
@Component
public class StringSampleRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StringSampleRunner.class);
    private static final String BEAN_SUFFIX = "StringRedisTemplate";

    private final ApplicationContext applicationContext;

    public StringSampleRunner(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(String... args) {
        log.info("");
        Map<String, StringRedisTemplate> templates = discoverTemplates();

        log.info("========== Multi-Redis Read/Write Verification ==========");

        for (Map.Entry<String, StringRedisTemplate> entry : templates.entrySet()) {
            verifyStringReadWrite(entry.getKey(), entry.getValue());
        }

        log.info("========== All read/write verifications passed! ==========");
    }

    private Map<String, StringRedisTemplate> discoverTemplates() {
        Map<String, StringRedisTemplate> result = new TreeMap<>();
        Map<String, StringRedisTemplate> allBeans = applicationContext.getBeansOfType(StringRedisTemplate.class);

        for (Map.Entry<String, StringRedisTemplate> entry : allBeans.entrySet()) {
            String beanName = entry.getKey();
            if (beanName.endsWith(BEAN_SUFFIX)) {
                String clusterName = beanName.substring(0, beanName.length() - BEAN_SUFFIX.length());
                if (!clusterName.isEmpty()) {
                    result.put(clusterName, entry.getValue());
                }
            }
        }
        return result;
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
