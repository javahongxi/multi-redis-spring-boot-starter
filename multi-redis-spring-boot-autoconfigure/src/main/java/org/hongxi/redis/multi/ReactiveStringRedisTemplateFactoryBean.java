package org.hongxi.redis.multi;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

/**
 * {@link FactoryBean} that creates a {@link ReactiveStringRedisTemplate} backed by
 * a named {@link LettuceConnectionFactory} bean.
 * <p>
 * Used by {@link MultiRedisRegistrar} in auto-register mode to wire
 * each cluster's connection factory to its corresponding reactive string template.
 *
 * @author javahongxi
 * @see MultiRedisRegistrar
 */
public class ReactiveStringRedisTemplateFactoryBean implements FactoryBean<ReactiveStringRedisTemplate>, BeanFactoryAware {

    private final String connectionFactoryBeanName;
    private BeanFactory beanFactory;

    public ReactiveStringRedisTemplateFactoryBean(String connectionFactoryBeanName) {
        this.connectionFactoryBeanName = connectionFactoryBeanName;
    }

    @Override
    public ReactiveStringRedisTemplate getObject() {
        LettuceConnectionFactory factory = beanFactory.getBean(connectionFactoryBeanName, LettuceConnectionFactory.class);
        return MultiRedisRegistrar.createReactiveStringRedisTemplate(factory);
    }

    @Override
    public Class<?> getObjectType() {
        return ReactiveStringRedisTemplate.class;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
}
