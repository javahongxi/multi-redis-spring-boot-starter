package org.hongxi.redis.multi;

import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Builder for creating RedisTemplate instances bound to different Redis clusters.
 * <p>Inspired by Spring AI's ChatClient.Builder pattern.</p>
 *
 * @author javahongxi
 */
public class RedisTemplateBuilder {

    private final MultiRedisProperties properties;
    private final Map<String, LettuceConnectionFactory> connectionFactoryCache = new ConcurrentHashMap<>();

    public RedisTemplateBuilder(MultiRedisProperties properties) {
        this.properties = properties;
    }

    /**
     * Select a cluster by name and return a configured {@link RedisTemplate}.
     *
     * @param clusterName the cluster name defined in spring.data.redis.clusters.{name}
     * @return a new RedisTemplate connected to the specified cluster
     */
    public RedisTemplate<String, Object> cluster(String clusterName) {
        LettuceConnectionFactory factory = getConnectionFactory(clusterName);
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(RedisSerializer.string());
        template.setValueSerializer(RedisSerializer.json());
        template.setHashKeySerializer(RedisSerializer.string());
        template.setHashValueSerializer(RedisSerializer.json());
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Select a cluster by name and return a configured {@link StringRedisTemplate}.
     *
     * @param clusterName the cluster name defined in spring.data.redis.clusters.{name}
     * @return a new StringRedisTemplate connected to the specified cluster
     */
    public StringRedisTemplate stringTemplate(String clusterName) {
        LettuceConnectionFactory factory = getConnectionFactory(clusterName);
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(factory);
        template.afterPropertiesSet();
        return template;
    }

    private LettuceConnectionFactory getConnectionFactory(String clusterName) {
        MultiRedisProperties.Cluster cluster = properties.getClusters().get(clusterName);
        if (cluster == null) {
            throw new IllegalArgumentException(
                    "Redis cluster '" + clusterName + "' not found. Available: "
                            + properties.getClusters().keySet());
        }
        return connectionFactoryCache.computeIfAbsent(clusterName, name -> createConnectionFactory(cluster));
    }

    private LettuceConnectionFactory createConnectionFactory(MultiRedisProperties.Cluster cluster) {
        LettuceConnectionFactory factory;
        if (cluster.isClusterMode()) {
            // Redis Cluster mode
            RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(cluster.getCluster().getNodes());
            if (cluster.getCluster().getMaxRedirects() != null) {
                clusterConfig.setMaxRedirects(cluster.getCluster().getMaxRedirects());
            }
            if (cluster.getPassword() != null && !cluster.getPassword().isEmpty()) {
                clusterConfig.setPassword(cluster.getPassword());
            }
            factory = buildClusterConnectionFactory(clusterConfig, cluster);
        } else {
            // Standalone mode
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
            config.setHostName(cluster.getHost());
            config.setPort(cluster.getPort());
            config.setDatabase(cluster.getDatabase());
            if (cluster.getPassword() != null && !cluster.getPassword().isEmpty()) {
                config.setPassword(cluster.getPassword());
            }
            factory = buildStandaloneConnectionFactory(config, cluster);
        }
        factory.afterPropertiesSet();
        return factory;
    }

    private LettuceConnectionFactory buildClusterConnectionFactory(
            RedisClusterConfiguration clusterConfig, MultiRedisProperties.Cluster cluster) {
        ClusterTopologyRefreshOptions topologyRefreshOptions = buildClusterTopologyRefreshOptions(cluster);
        MultiRedisProperties.Pool pool = resolvePoolConfig(cluster);

        if (pool != null) {
            GenericObjectPoolConfig<StatefulConnection<?, ?>> poolConfig = buildPoolConfig(pool);
            LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder =
                    LettucePoolingClientConfiguration.builder().poolConfig(poolConfig);
            if (cluster.getTimeout() != null) {
                builder.commandTimeout(cluster.getTimeout());
            }
            if (topologyRefreshOptions != null) {
                builder.clientOptions(ClusterClientOptions.builder().topologyRefreshOptions(topologyRefreshOptions).build());
            }
            return new LettuceConnectionFactory(clusterConfig, builder.build());
        } else {
            LettuceClientConfiguration.LettuceClientConfigurationBuilder clientBuilder =
                    LettuceClientConfiguration.builder();
            if (cluster.getTimeout() != null) {
                clientBuilder.commandTimeout(cluster.getTimeout());
            }
            if (topologyRefreshOptions != null) {
                clientBuilder.clientOptions(ClusterClientOptions.builder().topologyRefreshOptions(topologyRefreshOptions).build());
            }
            return new LettuceConnectionFactory(clusterConfig, clientBuilder.build());
        }
    }

    private LettuceConnectionFactory buildStandaloneConnectionFactory(
            RedisStandaloneConfiguration config, MultiRedisProperties.Cluster cluster) {
        MultiRedisProperties.Pool pool = resolvePoolConfig(cluster);
        if (pool != null) {
            GenericObjectPoolConfig<StatefulConnection<?, ?>> poolConfig = buildPoolConfig(pool);
            LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder =
                    LettucePoolingClientConfiguration.builder().poolConfig(poolConfig);
            if (cluster.getTimeout() != null) {
                builder.commandTimeout(cluster.getTimeout());
            }
            return new LettuceConnectionFactory(config, builder.build());
        } else {
            LettuceClientConfiguration.LettuceClientConfigurationBuilder clientBuilder =
                    LettuceClientConfiguration.builder();
            if (cluster.getTimeout() != null) {
                clientBuilder.commandTimeout(cluster.getTimeout());
            }
            return new LettuceConnectionFactory(config, clientBuilder.build());
        }
    }

    private GenericObjectPoolConfig<StatefulConnection<?, ?>> buildPoolConfig(MultiRedisProperties.Pool pool) {
        GenericObjectPoolConfig<StatefulConnection<?, ?>> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(pool.getMaxActive());
        poolConfig.setMaxIdle(pool.getMaxIdle());
        poolConfig.setMinIdle(pool.getMinIdle());
        if (pool.getMaxWait() != null) {
            poolConfig.setMaxWait(pool.getMaxWait());
        }
        return poolConfig;
    }

    private ClusterTopologyRefreshOptions buildClusterTopologyRefreshOptions(MultiRedisProperties.Cluster cluster) {
        MultiRedisProperties.Lettuce lettuce = cluster.getLettuce();
        if (lettuce == null || lettuce.getCluster() == null) {
            return null;
        }
        MultiRedisProperties.Refresh refresh = lettuce.getCluster().getRefresh();
        if (refresh == null || (!refresh.isAdaptive() && refresh.getPeriod() == null)) {
            return null;
        }
        ClusterTopologyRefreshOptions.Builder builder = ClusterTopologyRefreshOptions.builder();
        if (refresh.isAdaptive()) {
            builder.enableAllAdaptiveRefreshTriggers();
        }
        if (refresh.getPeriod() != null) {
            builder.enablePeriodicRefresh(refresh.getPeriod());
        }
        return builder.build();
    }

    private MultiRedisProperties.Pool resolvePoolConfig(MultiRedisProperties.Cluster cluster) {
        if (cluster.getLettuce() != null && cluster.getLettuce().getPool() != null) {
            return cluster.getLettuce().getPool();
        }
        return null;
    }
}
