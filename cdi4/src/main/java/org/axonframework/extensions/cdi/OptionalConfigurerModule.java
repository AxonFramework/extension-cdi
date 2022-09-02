package org.axonframework.extensions.cdi;

import org.axonframework.config.ConfigurerModule;

public class OptionalConfigurerModule {

    ConfigurerModule module;

    boolean enabled;

    public OptionalConfigurerModule(ConfigurerModule module, boolean enabled) {
        this.module = module;
        this.enabled = enabled;
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
