package org.hongxi.redis.multi;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * {@link FactoryBean} that creates a {@link StringRedisTemplate} backed by
 * a named {@link LettuceConnectionFactory} bean.
 * <p>
 * Used by {@link MultiRedisRegistrar} in auto-register mode to wire
 * each cluster's connection factory to its corresponding string template.
 *
 * @author javahongxi
 * @see MultiRedisRegistrar
 */
public class StringRedisTemplateFactoryBean implements FactoryBean<StringRedisTemplate>, BeanFactoryAware {

    private final String connectionFactoryBeanName;
    private BeanFactory beanFactory;

    public StringRedisTemplateFactoryBean(String connectionFactoryBeanName) {
        this.connectionFactoryBeanName = connectionFactoryBeanName;
    }

    @Override
    public StringRedisTemplate getObject() {
        LettuceConnectionFactory factory = beanFactory.getBean(connectionFactoryBeanName, LettuceConnectionFactory.class);
        return MultiRedisRegistrar.createStringRedisTemplate(factory);
    }

    @Override
    public Class<?> getObjectType() {
        return StringRedisTemplate.class;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
}
