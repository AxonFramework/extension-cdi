package org.axonframework.extensions.cdi.test.event;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.extensions.cdi.AxonCDIConfguration;
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


    @Dependent
    static class Config {
        @Produces
        public AxonCDIConfguration axonCDIConfguration () {
            return AxonCDIConfguration.builder()
                    .disableAxonServerConnector(true)
                    .build();
        }
    }
}
