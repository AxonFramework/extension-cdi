package org.axonframework.extensions.cdi.test.aggregate.statestored;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.*;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.common.jpa.SimpleEntityManagerProvider;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.extensions.cdi.AxonCDIConfguration;
import org.axonframework.extensions.cdi.annotations.Aggregate;
import org.axonframework.extensions.cdi.annotations.AxonConfig;
import org.axonframework.extensions.cdi.test.TestUtils;
import org.axonframework.modelling.command.*;

import java.util.HashMap;

public class AggregateTestComponents {

    public static class TestCommand {
        @TargetAggregateIdentifier
        public String id;
    }

    public static class TestEvent {
    }

    @ApplicationScoped
    public static class Config {

        @Produces
        public AxonCDIConfguration axonCDIConfguration () {
            return AxonCDIConfguration.builder()
                    .disableAxonServerConnector(true)
                    .build();
        }

        @Inject
        EntityManagerFactory entityManagerFactory;

        @Inject
        EntityManager entityManager;

        @Produces
        @ApplicationScoped
        public EntityManagerFactory produceEntityManagerFactory() {
            return Persistence.createEntityManagerFactory("default", new HashMap<>());
        }

        @Produces
        @RequestScoped
        public EntityManager produceEntityManager() {
            return entityManagerFactory.createEntityManager();
        }


        @Produces
        @AxonConfig
        @Named("repo1")
        public Repository configureRepository1 (EventBus eventBus) {
            return GenericJpaRepository.builder(TestAggregate.class)
                    .entityManagerProvider(new SimpleEntityManagerProvider(entityManager))
                    .eventBus(eventBus)
                    .build();
        }

        @Produces
        @AxonConfig
        @Named("repo2")
        public Repository configureRepository2 (EventBus eventBus) {
            return GenericJpaRepository.builder(TestAggregate.class)
                    .entityManagerProvider(new SimpleEntityManagerProvider(entityManager))
                    .eventBus(eventBus)
                    .build();
        }

    }

    @Aggregate (repository = "repo1")
    @Entity
    public static class TestAggregate {

        @AggregateIdentifier
        @Id
        public String id;

        public TestAggregate () {
            // required no-arg constructor
        }

        @CommandHandler
        public TestAggregate (TestCommand command) {
            AggregateLifecycle.apply(new TestEvent());
        }

        @EventHandler
        public void handle (TestEvent command) {
            this.id = "new";
            TestUtils.success.set(true);
        }
    }
}
