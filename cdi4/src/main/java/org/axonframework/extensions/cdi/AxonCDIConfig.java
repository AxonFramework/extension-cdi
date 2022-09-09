package org.axonframework.extensions.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class AxonCDIConfig {

    @Inject
    @ConfigProperty (name = "axonserver.connector.enabled", defaultValue = "true")
    private boolean axonServerConnectorEnabled;

    @Inject
    @ConfigProperty (name = "something", defaultValue = "some value")
    private String something;


    public boolean isAxonServerConnectorEnabled() {
        return axonServerConnectorEnabled;
    }

    public String getSomething() {
        return something;
    }
}
