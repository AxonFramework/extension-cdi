package org.axonframework.extensions.cdi.test.event;

import jakarta.enterprise.context.ApplicationScoped;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.extensions.cdi.test.TestUtils;

import java.io.Serializable;

public class EventProcessingTestComponents {

    public static class Event {}

    @ApplicationScoped
    public static class Handler implements Serializable {

        @EventHandler
        public void handle(Event query) {
            TestUtils.success.set(true);
        }
    }
}
