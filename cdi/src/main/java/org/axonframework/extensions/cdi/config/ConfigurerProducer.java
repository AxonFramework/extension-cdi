package org.axonframework.extensions.cdi.config;

import org.axonframework.config.Configurer;
import org.axonframework.config.DefaultConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class ConfigurerProducer {

    private static final Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    private enum ConfigurerEnum {
        INSTANCE;

        private final Configurer value;

        ConfigurerEnum() {
            logger.info("instance(): Creating a new DefaultConfigurer...");
            this.value = DefaultConfigurer.defaultConfiguration();
        }

    }

    public static Configurer instance() {
        logger.info("instance(): Here's your Configurer...");

        return ConfigurerEnum.INSTANCE.value;
    }
}
