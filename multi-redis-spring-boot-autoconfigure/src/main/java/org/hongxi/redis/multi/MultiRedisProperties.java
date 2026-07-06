package org.hongxi.redis.multi;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuration properties for multiple Redis clusters.
 *
 * @author javahongxi
 */
@ConfigurationProperties(prefix = "spring.redis")
public class MultiRedisProperties {

    private Map<String, Cluster> clusters = new LinkedHashMap<>();

    public Map<String, Cluster> getClusters() {
        return clusters;
    }

    public void setClusters(Map<String, Cluster> clusters) {
        this.clusters = clusters;
    }

    public static class Cluster {

        private String host = "localhost";
        private int port = 6379;
        private String password;
        private int database = 0;
        private Duration timeout;

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }

        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public int getDatabase() { return database; }
        public void setDatabase(int database) { this.database = database; }

        public Duration getTimeout() { return timeout; }
        public void setTimeout(Duration timeout) { this.timeout = timeout; }
    }
}
