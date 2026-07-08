package org.hongxi.redis.multi.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Verifies multi-Redis connectivity on startup (connection and server info only).
 * <p>
 * Dynamically discovers all {@link StringRedisTemplate} beans by naming convention
 * ({@code {clusterName}StringRedisTemplate}), so it works across all test scenarios:
 * official-only, clusters-only, and mixed.
 *
 * @author javahongxi
 */
@Order(1)
@Component
public class ConnectionSampleRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ConnectionSampleRunner.class);
    private static final String BEAN_SUFFIX = "StringRedisTemplate";

    private final ApplicationContext applicationContext;

    public ConnectionSampleRunner(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(String... args) {
        log.info("");
        // Discover all StringRedisTemplate beans by naming convention
        Map<String, StringRedisTemplate> templates = discoverTemplates();

        log.info("========== Multi-Redis Connection Verification ==========");
        log.info("Discovered {} cluster(s): {}", templates.size(), templates.keySet());

        for (Map.Entry<String, StringRedisTemplate> entry : templates.entrySet()) {
            verifyConnection(entry.getKey(), entry.getValue());
        }

        log.info("========== Connection verification complete ==========");
    }

    /**
     * Discover all StringRedisTemplate beans by naming convention.
     * Bean name pattern: {clusterName}StringRedisTemplate
     */
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

    private void verifyConnection(String name, StringRedisTemplate template) {
        try {
            LettuceConnectionFactory factory = (LettuceConnectionFactory) template.getConnectionFactory();

            // 1. Show connection factory configuration (expected target)
            if (factory.getClusterConfiguration() != null) {
                RedisClusterConfiguration clusterConfig = factory.getClusterConfiguration();
                log.info("[{}] Config -> CLUSTER nodes={}", name, clusterConfig.getClusterNodes());
            } else {
                RedisStandaloneConfiguration config = factory.getStandaloneConfiguration();
                log.info("[{}] Config -> {}:{}", name, config.getHostName(), config.getPort());
            }

            // 2. Query actual server info via INFO command (proves real connection)
            Properties serverInfo = factory.getConnection().serverCommands().info("server");
            if (serverInfo != null && serverInfo.getProperty("tcp_port") != null) {
                // Standalone mode: keys are plain (e.g. "tcp_port", "redis_version")
                log.info("[{}] Server -> tcp_port={}, redis_version={}", name,
                        serverInfo.getProperty("tcp_port"),
                        serverInfo.getProperty("redis_version"));
            } else if (serverInfo != null) {
                // Cluster mode: keys are node-prefixed (e.g. "127.0.0.1:7001.tcp_port")
                Map<String, String> nodePorts = new LinkedHashMap<>();
                String version = null;
                for (String key : serverInfo.stringPropertyNames()) {
                    if (key.endsWith(".tcp_port")) {
                        String node = key.substring(0, key.length() - ".tcp_port".length());
                        nodePorts.put(node, serverInfo.getProperty(key));
                    }
                    if (key.endsWith(".redis_version") && version == null) {
                        version = serverInfo.getProperty(key);
                    }
                }
                log.info("[{}] Server -> CLUSTER nodes={}, redis_version={}", name, nodePorts, version);
            }
        } catch (Exception e) {
            log.error("[{}] Connection verification FAILED: {}", name, e.getMessage());
        }
    }
}
