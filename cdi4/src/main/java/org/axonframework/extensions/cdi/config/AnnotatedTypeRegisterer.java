package org.axonframework.extensions.cdi.config;

import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import org.axonframework.config.Configurer;

public interface AnnotatedTypeRegisterer<T> extends BeanInstantiatingConfigurer {

    void add (AnnotatedType<T> annotatedType);

    void registerAnnotatedTypes (BeanManager beanManager, Configurer configurer);
}
