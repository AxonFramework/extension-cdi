package org.axonframework.extensions.cdi;

import org.axonframework.axonserver.connector.ServerConnectorConfigurerModule;
import org.axonframework.config.ConfigurerModule;

public class OptionalConfigurerModule {

    ConfigurerModule module;

    boolean enabled;

    public OptionalConfigurerModule(ConfigurerModule module, AxonCDIConfig axonCDIConfig) {
        this.module = module;
        this.enabled = true;

        if (ServerConnectorConfigurerModule.class.equals(module.getClass())) {
            this.enabled = axonCDIConfig.isAxonServerConnectorEnabled();
        }
    }

    public ConfigurerModule getModule() {
        return module;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
