package org.axonframework.extensions.cdi.test.aggregate.caching;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.common.caching.Cache;
import org.axonframework.common.caching.WeakReferenceCache;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.extensions.cdi.annotations.Aggregate;
import org.axonframework.extensions.cdi.annotations.AxonConfig;
import org.axonframework.extensions.cdi.annotations.AxonDefaultConfig;
import org.axonframework.extensions.cdi.test.TestUtils;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class AggregateTestComponents {


    public static class Command1 {
        @TargetAggregateIdentifier
        public String id;
    }

    public static class Command12 {
        @TargetAggregateIdentifier
        public String id;
    }

    public static class Command2 {
        @TargetAggregateIdentifier
        public String id;
    }

    public static class Command22 {
        @TargetAggregateIdentifier
        public String id;
    }

    public static class Command3 {
        @TargetAggregateIdentifier
        public String id;
    }

    public static class Command32 {
        @TargetAggregateIdentifier
        public String id;
    }

    public static class TestEvent {
        public String id;
    }

    @Dependent
    public static class Config {

        @Produces
        @AxonConfig
        public EventStorageEngine configureEventStorageEngine () {
            return new InMemoryEventStorageEngine();
        }

        @Produces
        @AxonConfig
        @Named("testAggregateCache")
        public Cache namedCache () {
            return new WeakReferenceCache() {
                public <K, V> V get(K key) {
                    TestUtils.result.set("NamedCache");
                    return super.get(key);
                }
            };
        }

        @Produces
        // TODO: should not have to use separate annotation
        @AxonDefaultConfig
        public Cache defaultCache () {
            return new WeakReferenceCache() {
                public <K, V> V get(K key) {
                    TestUtils.result.set("DefaultCache");
                    return super.get(key);
                }
            };
        }


    }

    @Aggregate (cache = "testAggregateCache")
    public static class Aggregate1 {
        @AggregateIdentifier
        public String id;

        public Aggregate1 () {}

        @CommandHandler
        public Aggregate1 (Command1 command) {
            TestEvent testEvent = new TestEvent();
            testEvent.id = command.id;
            AggregateLifecycle.apply(testEvent);
        }

        @CommandHandler
        public void on (Command12 command) {
            // do nothing
        }

        @EventSourcingHandler
        public void handle (TestEvent event) {
            this.id = event.id;
        }
    }

    @Aggregate
    public static class Aggregate2 {
        @AggregateIdentifier
        public String id;

        public Aggregate2 () {}

        @CommandHandler
        public Aggregate2 (Command2 command) {
            TestEvent testEvent = new TestEvent();
            testEvent.id = command.id;
            AggregateLifecycle.apply(testEvent);
        }

        @CommandHandler
        public void on (Command22 command) {
            // do nothing
        }

        @EventSourcingHandler
        public void handle (TestEvent event) {
            this.id = event.id;
        }

    }

    @Aggregate(ignoreDefaultCache = true)
    public static class Aggregate3 {
        @AggregateIdentifier
        public String id;

        public Aggregate3 () {}

        @CommandHandler
        public Aggregate3 (Command3 command) {
            TestEvent testEvent = new TestEvent();
            testEvent.id = command.id;
            AggregateLifecycle.apply(testEvent);
        }

        @CommandHandler
        public void on (Command32 command) {
            // do nothing
        }

        @EventSourcingHandler
        public void handle (TestEvent event) {
            this.id = event.id;
        }

    }
}
