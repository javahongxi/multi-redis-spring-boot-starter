package org.hongxi.redis.multi;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Condition that matches when auto-register mode is effectively disabled.
 * <p>
 * Auto-register is considered disabled when:
 * <ul>
 *   <li>{@code spring.data.redis.auto-register} is explicitly set to {@code false}, OR</li>
 *   <li>{@code spring.data.redis.auto-register} is not set AND no Redis configuration is detected
 *       (neither official format nor multi-cluster format)</li>
 * </ul>
 * <p>
 * This condition is used to activate Builder mode (Mode 1) where users manually define
 * RedisTemplate beans using {@link RedisTemplateBuilder}.
 *
 * @author javahongxi
 */
public class OnAutoRegisterDisabledCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String autoRegisterProp = context.getEnvironment().getProperty("spring.data.redis.auto-register");
        
        if (autoRegisterProp != null) {
            // User explicitly set the property
            boolean autoRegister = Boolean.parseBoolean(autoRegisterProp);
            if (autoRegister) {
                return ConditionOutcome.noMatch("auto-register is explicitly enabled");
            } else {
                return ConditionOutcome.match("auto-register is explicitly disabled");
            }
        }
        
        // Property not set, check if any Redis config is detected
        boolean hasOfficialFormat = hasOfficialFormatConfig(context);
        boolean hasMultiCluster = hasMultiClusterConfig(context);
        
        if (hasOfficialFormat || hasMultiCluster) {
            return ConditionOutcome.noMatch("Redis configuration detected, auto-register will be auto-enabled");
        }
        
        return ConditionOutcome.match("No Redis configuration detected, Builder mode is active");
    }
    
    private boolean hasOfficialFormatConfig(ConditionContext context) {
        String host = context.getEnvironment().getProperty("spring.data.redis.host");
        if (host != null) {
            return true;
        }
        String clusterNodes = context.getEnvironment().getProperty("spring.data.redis.cluster.nodes");
        if (clusterNodes != null && !clusterNodes.isEmpty()) {
            return true;
        }
        String url = context.getEnvironment().getProperty("spring.data.redis.url");
        return url != null && !url.isEmpty();
    }
    
    private boolean hasMultiClusterConfig(ConditionContext context) {
        if (context.getEnvironment() instanceof ConfigurableEnvironment ce) {
            for (PropertySource<?> ps : ce.getPropertySources()) {
                if (ps instanceof EnumerablePropertySource<?> eps) {
                    for (String name : eps.getPropertyNames()) {
                        if (name.startsWith("spring.data.redis.clusters.")) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
