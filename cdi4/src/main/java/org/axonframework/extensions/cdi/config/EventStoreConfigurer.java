package org.axonframework.extensions.cdi.config;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.BeanManager;
import org.axonframework.config.Configurer;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventStoreConfigurer implements ComponentConfigurer {

    private static Logger LOGGER = LoggerFactory.getLogger(EventStoreConfigurer.class);

    @Override
    public void configure(BeanManager beanManager, Configurer configurer) {
        /*
            TODO: support for other Event Stores:
             - JPA
             - JDBC
             - MongoDB
         */

        Instance<EmbeddedEventStore> embeddedEventStoreInstance = getConfiguredInstance(
                beanManager,
                EmbeddedEventStore.class);

        Instance<EventStorageEngine> eventStorageEngineInstance = getConfiguredInstance(
                beanManager,
                EventStorageEngine.class);

        if (embeddedEventStoreInstance.isResolvable()) {
            EmbeddedEventStore embeddedEventStore = embeddedEventStoreInstance.get();
            LOGGER.debug("Found embedded EventStore instance in " + embeddedEventStore.getClass().getName());
            configurer.configureEventStore(configuration -> embeddedEventStore);
        } else if (eventStorageEngineInstance.isResolvable()) {
            EventStorageEngine eventStorageEngine =  eventStorageEngineInstance.get();
            LOGGER.debug("Found event store engine instance in " + eventStorageEngine.getClass().getName());
            configurer.configureEventStore(configuration -> {
                return EmbeddedEventStore.builder()
                        .storageEngine(eventStorageEngine)
                        .build();

            });
        }
    }
}
