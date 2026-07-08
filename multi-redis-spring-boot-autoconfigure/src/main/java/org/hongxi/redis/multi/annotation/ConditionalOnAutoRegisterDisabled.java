package org.hongxi.redis.multi.annotation;

import org.hongxi.redis.multi.OnAutoRegisterDisabledCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Condition that matches when auto-register mode is effectively disabled.
 * <p>
 * This is used to activate Builder mode (Mode 1) where users manually define
 * RedisTemplate beans using {@code RedisTemplateBuilder}.
 * <p>
 * Auto-register is considered disabled when:
 * <ul>
 *   <li>{@code spring.data.redis.auto-register} is explicitly set to {@code false}, OR</li>
 *   <li>{@code spring.data.redis.auto-register} is not set AND no Redis configuration is detected</li>
 * </ul>
 *
 * @author javahongxi
 * @see OnAutoRegisterDisabledCondition
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnAutoRegisterDisabledCondition.class)
public @interface ConditionalOnAutoRegisterDisabled {
}
