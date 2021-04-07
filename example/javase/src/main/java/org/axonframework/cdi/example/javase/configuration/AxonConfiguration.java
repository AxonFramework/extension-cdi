package org.axonframework.cdi.example.javase.configuration;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;

@ApplicationScoped
public class AxonConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(
            MethodHandles.lookup().lookupClass().getName());

    /**
     * In a Java SE environment, we are using an in-memory store.
     *
     * @return Event storage engine.
     */
    @Produces
    @ApplicationScoped
    public EventStorageEngine eventStorageEngine() {
        logger.info("AXON: Configuring in-memory store!");
        return new InMemoryEventStorageEngine();
    }
}
