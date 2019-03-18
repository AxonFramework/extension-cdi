package org.axonframework.extensions.cdi;

import org.slf4j.Logger;

import javax.enterprise.context.*;
import javax.inject.Singleton;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

public enum BeanScope {
    UNKNOWN(Class.class),
    SINGLETON(Singleton.class),                     // There can be only one!
    APPLICATION_SCOPED(ApplicationScoped.class),    // The bean is expected to live until the application ends
    SESSION_SCOPED(SessionScoped.class),            // The bean is expected to live until the (web) session expires
    CONVERSATION_SCOPED(ConversationScoped.class),  // The bean is expected to live for the length of the current "conversation"
    REQUEST_SCOPED(RequestScoped.class),            // The bean is expected to live until the current (web) request has been replied to
    DEPENDENT(Dependent.class);                     // There will be one bean for every context wherein the bean is requested

    private static final Logger logger = getLogger(lookup().lookupClass());

    private Class<?> annotation;

    BeanScope(Class<?> annotation) {
        this.annotation = annotation;
    }

    public Class<?> getAnnotation() {
        return annotation;
    }

    public static BeanScope fromAnnotation(Class<?> annotation) {
        if (annotation.equals(Singleton.class)) {
            return SINGLETON;
        }
        else if (annotation.equals(ApplicationScoped.class)) {
            return APPLICATION_SCOPED;
        }
        else if (annotation.equals(SessionScoped.class)) {
            return SESSION_SCOPED;
        }
        else if (annotation.equals(ConversationScoped.class)) {
            return CONVERSATION_SCOPED;
        }
        else if (annotation.equals(RequestScoped.class)) {
            return REQUEST_SCOPED;
        }
        else if (annotation.equals(Dependent.class)) {
            return DEPENDENT;
        }
        else {
            logger.warn("fromAnnotation(): Unknown Bean scope annotation \"{}\".", annotation.getName());
        }
        return UNKNOWN;
    }
}
