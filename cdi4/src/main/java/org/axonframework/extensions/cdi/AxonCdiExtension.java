package org.axonframework.extensions.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.*;
import jakarta.enterprise.util.AnnotationLiteral;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.extensions.cdi.annotations.Aggregate;
import org.axonframework.extensions.cdi.annotations.AxonConfig;
import org.axonframework.extensions.cdi.config.*;
import org.axonframework.messaging.annotation.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Main CDI extension class responsible for collecting CDI beans and setting up
 * Axon configuration.
 */
public class AxonCdiExtension implements Extension {

    private static Logger LOGGER = LoggerFactory.getLogger(AxonCdiExtension.class);

    private static final AnnotationLiteral<AxonConfig> AXON_CONFIG_ANNOTATION = new AnnotationLiteral<AxonConfig>() {};

    static {
        LOGGER.info("Axon CDI Extension loaded");
    }

    private List<ComponentConfigurer> componentConfigurers = new LinkedList<>();

    private AggregatesRegisterer aggregatesRegisterer = new AggregatesRegisterer();
    private MessageHandlingComponentsRegisterer messageHandlingComponentsRegisterer =
            new MessageHandlingComponentsRegisterer();


    public AxonCdiExtension () {
        componentConfigurers.add(new EventStoreConfigurer());
        componentConfigurers.add(new TransactionManagerConfigurer());

    }

    public void beforeBeanDiscovery (@Observes final BeforeBeanDiscovery bbdEvent) {
        LOGGER.debug("beforeBeanDiscovery");
    }

    <T> void observeMessageHandlingComponents(@Observes
                       @WithAnnotations(MessageHandler.class) final ProcessAnnotatedType<T> event) {

        if (event.getAnnotatedType().isAnnotationPresent(Aggregate.class)) {
            LOGGER.debug("found aggregate in : " + event.getAnnotatedType().getJavaClass());
            aggregatesRegisterer.add(event.getAnnotatedType());
        } else {
            LOGGER.debug("found message handler in : " + event.getAnnotatedType().getJavaClass());
            messageHandlingComponentsRegisterer.add(event.getAnnotatedType());
        }

    }

    void afterBeanDiscovery(@Observes final AfterBeanDiscovery afterBeanDiscovery,
                            final BeanManager beanManager) {
        LOGGER.debug("Configuring Axon Framework");

        afterBeanDiscovery.addBean()
                .types(Configuration.class)
                .qualifiers(new AnnotationLiteral<Default>() {}, new AnnotationLiteral<Any>() {})
                .scope(ApplicationScoped.class)
                .name(Configuration.class.getName())
                .beanClass(Configuration.class)
                .createWith(context -> {
                    Configurer configurer = DefaultConfigurer.defaultConfiguration();

                    componentConfigurers.forEach(c -> c.configure(beanManager, configurer));
                    aggregatesRegisterer.registerAnnotatedTypes(beanManager, configurer);
                    messageHandlingComponentsRegisterer.registerAnnotatedTypes(beanManager, configurer);

                    Configuration configuration = configurer.buildConfiguration();
                    return configuration;
                });

    }


    void afterDeploymentValidation(@Observes final AfterDeploymentValidation afterDeploymentValidation,
                                   final BeanManager beanManager) {
        LOGGER.debug("Starting Axon Framework");

        Configuration configuration = beanManager.createInstance()
                .select(Configuration.class,
                        new AnnotationLiteral<Default>() {},
                        new AnnotationLiteral<Any>() {}
                        ).get();

        configuration.onStart(Integer.MAX_VALUE, () -> LOGGER.debug("Axon Framework Started!"));
        configuration.onShutdown(Integer.MIN_VALUE, () -> LOGGER.debug("Axon Framework Stopped!"));
        configuration.start();
    }


    void beforeShutdown(@Observes @Destroyed(ApplicationScoped.class) final Object event) {
        Configuration configuration = CDI.current().select(Configuration.class).get();
        if (configuration != null) {
            configuration.shutdown();
        }
    }

}
