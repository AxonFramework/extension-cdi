package org.axonframework.extensions.cdi.config;

import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import org.axonframework.config.Configurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class MessageHandlingComponentsRegisterer<T> implements AnnotatedTypeRegisterer<T> {

    private static Logger LOGGER = LoggerFactory.getLogger(MessageHandlingComponentsRegisterer.class);

    Set<AnnotatedType> messageHandlingComponentsAnnotatedTypes = new HashSet<>();

    @Override
    public void add(AnnotatedType<T> annotatedType) {
        messageHandlingComponentsAnnotatedTypes.add(annotatedType);
    }

    @Override
    public void registerAnnotatedTypes(BeanManager beanManager, Configurer configurer) {
        messageHandlingComponentsAnnotatedTypes.forEach( aac -> {
            Class messageHandlingComponentClass = aac.getJavaClass();

            // TODO: change to use beanManager instead
            Object messageHandlingComponent = CDI.current().select(messageHandlingComponentClass).get();
            LOGGER.debug("Registering " + messageHandlingComponent + " with AxonFramework");
            configurer.registerMessageHandler(conf -> messageHandlingComponent);

        });
    }
}
