package org.hongxi.redis.multi;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

/**
 * {@link FactoryBean} that creates a {@link ReactiveRedisTemplate} backed by
 * a named {@link LettuceConnectionFactory} bean.
 * <p>
 * Used by {@link MultiRedisRegistrar} in auto-register mode to wire
 * each cluster's connection factory to its corresponding reactive template.
 *
 * @author javahongxi
 * @see MultiRedisRegistrar
 */
public class ReactiveRedisTemplateFactoryBean implements FactoryBean<ReactiveRedisTemplate<String, Object>>, BeanFactoryAware {

    private final String connectionFactoryBeanName;
    private final MultiRedisRegistrar.ClusterConfig config;
    private BeanFactory beanFactory;

    public ReactiveRedisTemplateFactoryBean(String connectionFactoryBeanName, MultiRedisRegistrar.ClusterConfig config) {
        this.connectionFactoryBeanName = connectionFactoryBeanName;
        this.config = config;
    }

    @Override
    public ReactiveRedisTemplate<String, Object> getObject() {
        LettuceConnectionFactory factory = beanFactory.getBean(connectionFactoryBeanName, LettuceConnectionFactory.class);
        return MultiRedisRegistrar.createReactiveRedisTemplate(factory, config);
    }

    @Override
    public Class<?> getObjectType() {
        return ReactiveRedisTemplate.class;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
}
