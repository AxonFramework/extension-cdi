package org.axonframework.extensions.cdi.config;

import jakarta.enterprise.inject.spi.BeanManager;
import org.axonframework.config.Configurer;

public interface ComponentConfigurer extends BeanInstantiatingConfigurer {

    void configure (BeanManager beanManager, Configurer configurer);

}
