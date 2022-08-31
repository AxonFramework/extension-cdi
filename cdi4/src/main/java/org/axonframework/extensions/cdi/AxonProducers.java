package org.axonframework.extensions.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Typed;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.Configuration;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.eventhandling.scheduling.EventScheduler;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.extensions.cdi.annotations.AxonInternal;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.axonframework.serialization.Serializer;

@Dependent
@AxonInternal
public class AxonProducers {

    @Default
    @Typed(CommandBus.class)
    @ApplicationScoped
    @Produces
    public CommandBus commandBus(Configuration configuration) {
        return configuration.commandBus();
    }

    @Default
    @Typed(CommandGateway.class)
    @ApplicationScoped
    @Produces
    public CommandGateway commandGateway(Configuration configuration) {
        return configuration.commandGateway();
    }

    @Default
    @Typed(QueryBus.class)
    @ApplicationScoped
    @Produces
    public QueryBus queryBus(Configuration configuration) {
        return configuration.queryBus();
    }

    @Default
    @Typed(QueryGateway.class)
    @ApplicationScoped
    @Produces
    public QueryGateway queryGateway(Configuration configuration) {
        return configuration.queryGateway();
    }

    @Default
    @Typed(QueryUpdateEmitter.class)
    @ApplicationScoped
    @Produces
    public QueryUpdateEmitter queryUpdateEmitter(Configuration configuration) {
        return configuration.queryUpdateEmitter();
    }

    @Default
    @Typed(EventBus.class)
    @ApplicationScoped
    @Produces
    public EventBus eventBus(Configuration configuration) {
        return configuration.eventBus();
    }

    @Default
    @Typed(EventGateway.class)
    @ApplicationScoped
    @Produces
    public EventGateway eventGateway(Configuration configuration) {
        return configuration.eventGateway();
    }

    @Default
    @Typed(EventStore.class)
    @ApplicationScoped
    @Produces
    public EventStore eventStore(Configuration configuration) {
        return configuration.eventStore();
    }

    @Default
    @Typed(Serializer.class)
    @ApplicationScoped
    @Produces
    public Serializer eventSerializer(Configuration configuration) {
        return configuration.eventSerializer();
    }

    @Default
    @Typed(DeadlineManager.class)
    @ApplicationScoped
    @Produces
    public DeadlineManager deadlineManager(Configuration configuration) {
        return configuration.deadlineManager();
    }

    @Default
    @Typed(EventScheduler.class)
    @ApplicationScoped
    @Produces
    public EventScheduler eventScheduler(Configuration configuration) {
        return configuration.eventScheduler();
    }

}
