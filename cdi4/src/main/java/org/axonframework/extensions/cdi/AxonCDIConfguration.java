package org.axonframework.extensions.cdi;

import jakarta.inject.Inject;
import jakarta.inject.Named;


public class AxonCDIConfguration {

    private boolean disableAxonServerConnector;

    private AxonCDIConfguration(Builder builder) {
        this.disableAxonServerConnector = builder.disableAxonServerConnector;
    }

    public boolean disableAxonServerConnector () {
        return disableAxonServerConnector;
    };


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private boolean disableAxonServerConnector;

        public Builder disableAxonServerConnector (boolean disableAxonServerConnector) {
            this.disableAxonServerConnector = disableAxonServerConnector;
            return this;
        }

        public AxonCDIConfguration build () {
            return new AxonCDIConfguration(this);
        }
    }


}
