package org.axonframework.extensions.cdi.messagehandling;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.extensions.cdi.BeanScope;
import org.axonframework.extensions.cdi.stereotype.Aggregate;
import org.axonframework.queryhandling.QueryHandler;
import org.slf4j.Logger;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Milan Savic
 */
public class MessageHandlingBeanDefinition {

    private static final Logger logger = getLogger(lookup().lookupClass());

    private final Bean<?> bean;
    private final BeanScope scope;
    private boolean eventHandler = false;
    private boolean eventSourcingHandler = false;
    private boolean queryHandler = false;
    private boolean commandHandler = false;

    private MessageHandlingBeanDefinition(Bean<?> bean) {
        this.bean = bean;
        this.scope = BeanScope.fromAnnotation(bean.getScope());
    }

    public static Optional<MessageHandlingBeanDefinition> inspect(Bean<?> bean, Annotated annotated) {
        if (!(annotated instanceof AnnotatedType)) {
            return Optional.empty();
        }
        if (annotated.isAnnotationPresent(Aggregate.class)) {
            logger.info("inspect({}, {}): Ignoring Aggregates...", bean.getBeanClass().getName(), annotated.getClass().getName());

            return Optional.empty();
        }

        MessageHandlingBeanDefinition result = new MessageHandlingBeanDefinition(bean);

        ((AnnotatedType<?>) annotated).getMethods().forEach(m -> {
            if (m.isAnnotationPresent(QueryHandler.class)) {
                logger.info("inspect({}, ...): Found an @QueryHandler annotation.", bean.getBeanClass().getName());

                result.queryHandler = true;
            } else if (m.isAnnotationPresent(EventSourcingHandler.class)) {
                logger.info("inspect({}, ...): Found an @EventSourcingHandler annotation.", bean.getBeanClass().getName());

                result.eventSourcingHandler = true;
            } else if (m.isAnnotationPresent(EventHandler.class)) {
                logger.info("inspect({}, ...): Found an @EventHandler annotation.", bean.getBeanClass().getName());

                result.eventHandler = true;
            } else if (m.isAnnotationPresent(CommandHandler.class)) {
                logger.info("inspect({}, ...): Found an @CommandHandler annotation.", bean.getBeanClass().getName());

                result.commandHandler = true;
            }
        });
        if (result.isMessageHandler()) {
            logger.info("inspect(): Class {} has Scope {}.", bean.getBeanClass().getName(), result.getScope());
        }
        return result.isMessageHandler() ? Optional.of(result) : Optional.empty();
    }

    public Bean<?> getBean() {
        return bean;
    }

    public BeanScope getScope() {
        return scope;
    }

    public boolean isEventHandler() {
        return eventHandler;
    }

    public boolean isEventSourcingHandler() {
        return eventSourcingHandler;
    }

    public boolean isQueryHandler() {
        return queryHandler;
    }

    public boolean isCommandHandler() {
        return commandHandler;
    }

    public boolean isMessageHandler() {
        return eventHandler || eventSourcingHandler || queryHandler || commandHandler;
    }

    @Override
    public String toString() {
        return "MessageHandlingBeanDefinition with bean=" + bean
                + ", eventHandler=" + eventHandler + ", queryHandler="
                + queryHandler + ", commandHandler=" + commandHandler;
    }
}
