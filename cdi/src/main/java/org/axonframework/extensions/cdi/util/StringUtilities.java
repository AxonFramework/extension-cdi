package org.axonframework.extensions.cdi.util;

import java.util.Optional;

public class StringUtilities {

    public static String lowerCaseFirstLetter(String string) {
        return string.substring(0, 1).toLowerCase() + string.substring(1);
    }

    public static Optional<String> createOptional(String value) {
        if ("".equals(value)) {
            return Optional.empty();
        }
        
        return Optional.of(value);
    }
}
