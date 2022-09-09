package org.axonframework.extensions.cdi.test.aggregate;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.extensions.cdi.annotations.Aggregate;
import org.axonframework.extensions.cdi.annotations.AxonConfig;
import org.axonframework.extensions.cdi.test.TestUtils;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class AggregateTestComponents {


    public static class TestCommand {
        @TargetAggregateIdentifier
        public String id;
    }

    public static class TestEvent {
    }

    @Dependent
    public static class Config {

        @Produces
        @ApplicationScoped
        public EventStorageEngine eventStorageEngine () {
            return new InMemoryEventStorageEngine();
        }

        @Produces
        @AxonConfig
        public EventStore eventStore (EventStorageEngine eventStorageEngine) {
            return EmbeddedEventStore.builder()
                    .storageEngine(eventStorageEngine)
                    .build();
        }
    }

    @Aggregate
    public static class TestAggregate {

        @AggregateIdentifier
        public String id;

        public TestAggregate () {
        }

        @CommandHandler
        public TestAggregate (TestCommand command) {
            AggregateLifecycle.apply(new TestEvent());
        }

        @EventSourcingHandler
        public void handle (TestEvent command) {
            this.id = "new";
            TestUtils.success.set(true);
        }
    }
}
