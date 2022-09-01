package org.axonframework.extensions.cdi.config;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.util.AnnotationLiteral;

import java.util.Arrays;
import java.util.stream.Collectors;

public class CDIConfigurationException extends RuntimeException {


    private static final String listDelimiter = "\n\t --> ";

    public CDIConfigurationException(String message) {
        super(message);
    }

    public CDIConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CDIConfigurationException(Throwable cause) {
        super(cause);
    }


    public static CDIConfigurationException notFound (Class<?> type) {
        return new CDIConfigurationException(String.format(
                "Couldn't find bean of type %s ",
                type
        ));
    }

    public static CDIConfigurationException notFound (Class<?> type, Class<?> injectionPointClass, AnnotationLiteral<?>... annotationLiterals) {
        return new CDIConfigurationException(String.format(
                        "Couldn't find bean of type %s with qualifiers: " +
                        "\n\t%s" +
                        "\nInjection point:" +
                        "\n\t%s",
                type,
                Arrays.stream(annotationLiterals).sorted()
                        .map(AnnotationLiteral::toString)
                        .collect(Collectors.joining(listDelimiter)),
                injectionPointClass
        ));
    }

    public static CDIConfigurationException ambiguousInstance (Class<?> type, Instance<?> instance) {
        return ambiguousInstance(type, instance, null);
    }

    public static CDIConfigurationException ambiguousInstance (Class<?> type, Instance<?> instance, String injectionPoint) {

        String message;
        Object[] params;

        if (injectionPoint == null) {
            message = "\nAmbiguous configuration for type:" +
                    "\n\t `%s` " +
                    "\nMultiple matching beans found: %s%s";
            params = new Object[] {
                    type.getCanonicalName(),
                    listDelimiter,
                    instance.handlesStream()
                            .map(handle -> handle.getBean().toString())
                            .collect(Collectors.joining(listDelimiter))

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
                    listDelimiter,
                    instance.handlesStream()
                            .map(handle -> handle.getBean().toString())
                            .collect(Collectors.joining(listDelimiter))

            };
        }

        return new CDIConfigurationException(String.format(message, params));
    }
}
