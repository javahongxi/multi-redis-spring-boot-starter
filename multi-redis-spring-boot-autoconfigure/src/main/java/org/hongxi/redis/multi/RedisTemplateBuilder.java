package org.hongxi.redis.multi;

import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Builder for creating RedisTemplate instances bound to different Redis clusters.
 * <p>Inspired by Spring AI's ChatClient.Builder pattern.</p>
 *
 * @author javahongxi
 */
public class RedisTemplateBuilder {

    private final MultiRedisProperties properties;

    public RedisTemplateBuilder(MultiRedisProperties properties) {
        this.properties = properties;
    }

    /**
     * Select a cluster by name and return a configured RedisTemplate.
     *
     * @param clusterName the cluster name defined in spring.redis.clusters.{name}
     * @return a new RedisTemplate connected to the specified cluster
     */
    public RedisTemplate<String, Object> cluster(String clusterName) {
        MultiRedisProperties.Cluster cluster = properties.getClusters().get(clusterName);
        if (cluster == null) {
            throw new IllegalArgumentException(
                    "Redis cluster '" + clusterName + "' not found. Available: "
                            + properties.getClusters().keySet());
        }
        return build(cluster);
    }

    private RedisTemplate<String, Object> build(MultiRedisProperties.Cluster cluster) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(cluster.getHost());
        config.setPort(cluster.getPort());
        config.setDatabase(cluster.getDatabase());
        if (cluster.getPassword() != null && !cluster.getPassword().isEmpty()) {
            config.setPassword(cluster.getPassword());
        }

        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.afterPropertiesSet();

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
