package org.axonframework.extensions.cdi.test.aggregate.configurer;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.config.AggregateConfigurer;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.eventsourcing.GenericAggregateFactory;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.extensions.cdi.annotations.Aggregate;
import org.axonframework.extensions.cdi.annotations.AxonConfig;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import static org.axonframework.extensions.cdi.test.TestUtils.success;

public class AggregateTestComponents {


    public static class TestCommand {
        @TargetAggregateIdentifier
        public String id;
    }
    public static class TestCommand2 {
        @TargetAggregateIdentifier
        public String id;

        public TestCommand2(String id) {
            this.id = id;
        }
    }

    public static class TestEvent {
    }

    @Dependent
    public static class Config {
        @Produces
        @AxonConfig
        @Named
        public EmbeddedEventStore eventStore () {
            return EmbeddedEventStore.builder()
                        .storageEngine(new InMemoryEventStorageEngine())
                        .build();
        }

        @Produces
        @AxonConfig
        @Named
        public AggregateConfigurer customConfigurer (EventStore eventStore) {
            return AggregateConfigurer
                    .defaultConfiguration(TestAggregate.class)
                    .configureAggregateFactory(configuration -> new MyGenericAggregateFactory(TestAggregate.class));
        }
    }

    public static class MyGenericAggregateFactory extends GenericAggregateFactory<TestAggregate> {

        public MyGenericAggregateFactory(Class<TestAggregate> aggregateType) {
            super(aggregateType);
        }

        @Override
        public TestAggregate doCreateAggregate(String aggregateIdentifier, DomainEventMessage firstEvent) {
            success.set(true);
            return super.doCreateAggregate("aggregateIdentifier", firstEvent);
        }

    }

    @Aggregate (configurer = "customConfigurer")
    public static class TestAggregate {

        @AggregateIdentifier
        public String id;

        public TestAggregate () {
        }

        @CommandHandler
        public TestAggregate (TestCommand command) {
            AggregateLifecycle.apply(new TestEvent());
        }

        @CommandHandler
        public void on (TestCommand2 command) {
        }

        @EventSourcingHandler
        public void handle (TestEvent command) {
            this.id = "new";
        }
    }
}
