package org.axonframework.extensions.cdi.config;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Bean;

import java.util.stream.Collectors;

public class CDIConfigurationException extends RuntimeException {

    public CDIConfigurationException(String message) {
        super(message);
    }

    public CDIConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CDIConfigurationException(Throwable cause) {
        super(cause);
    }

    public static CDIConfigurationException ambiguousInstance (Class type, Instance<?> instance) {
        String delimiter = "\n\t -->";
        return new CDIConfigurationException(
                String.format("Ambiguous configuration for \n\t `%s` \n Multiple matching beans found: %s %s",
                        type.getCanonicalName(),
                        delimiter,
                        instance.handlesStream()
                                .map(handle -> handle.getBean().toString())
                                .collect(Collectors.joining(delimiter))
                )
        );
    }
}
