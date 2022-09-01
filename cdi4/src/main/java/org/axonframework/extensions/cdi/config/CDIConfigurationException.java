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
        return ambiguousInstance(type, instance, null);
    }

    public static CDIConfigurationException ambiguousInstance (Class type, Instance<?> instance, String injectionPoint) {

        String message;
        Object[] params;
        String delimiter = "\n\t --> ";

        if (injectionPoint == null) {
            message = "\nAmbiguous configuration for type:" +
                    "\n\t `%s` " +
                    "\nMultiple matching beans found: %s%s";
            params = new Object[] {
                    type.getCanonicalName(),
                    delimiter,
                    instance.handlesStream()
                            .map(handle -> handle.getBean().toString())
                            .collect(Collectors.joining(delimiter))

            };
        } else {
            message = "\nAmbiguous configuration for type:" +
                    "\n\t `%s` " +
                    "\nInjection point: " +
                    "\n\t `%s` " +
                    "\nMultiple matching beans found: %s%s";
            params = new Object[] {
                    type.getCanonicalName(),
                    injectionPoint,
                    delimiter,
                    instance.handlesStream()
                            .map(handle -> handle.getBean().toString())
                            .collect(Collectors.joining(delimiter))

            };
        }

        return new CDIConfigurationException(String.format(message, params));
    }
}
