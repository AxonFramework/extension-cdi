package org.axonframework.extensions.cdi;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.config.Configuration;
import org.axonframework.config.EventProcessingModule;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.extensions.cdi.stereotype.Aggregate;
import org.axonframework.modelling.command.Repository;
import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;
import org.junit.Test;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Id;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;


public class AutoWiredEventSourcedAggregateTest {

    @Rule
    public WeldInitiator weld = WeldInitiator
            .from(WeldInitiator.createWeld().enableDiscovery())
            .inject(this)
            .build();

    @Inject
    private Repository<Context.MyAggregate> myAggregateRepository;

    @Inject
    private Configuration configuration;

    @Test
    public void testAggregateIsWiredUsingStateStorage() {
        assertTrue(/*EventSourcing*/Repository.class.isAssignableFrom(myAggregateRepository.getClass()));
        assertEquals(EventSourcingRepository.class, configuration.repository(Context.MyAggregate.class).getClass());
    }

    @Singleton
    public static class Context {

        @Produces
        public EventStorageEngine eventStorageEngine() {
            return new InMemoryEventStorageEngine();
        }

        @Produces
        public EntityManagerProvider entityManagerProvider() {
            return mock(EntityManagerProvider.class);
        }

        @Produces
        public EventProcessingModule eventProcessingConfiguration() {
            return new EventProcessingModule();
        }

        @Aggregate
        public static class MyAggregate {

            @Id
            private String id;

            @CommandHandler
            public void handle(Long command) {
                apply(command);
            }

            @CommandHandler
            public void handle(String command) {
            }

            @EventSourcingHandler
            public void on(Long event) {
                this.id = Long.toString(event);
            }

            @EventSourcingHandler
            public void on(String event) {
                fail("Event Handler on aggregate shouldn't be invoked");
            }
        }
    }

    public static class SomeEvent {

        private final String id;

        public SomeEvent(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }


}
