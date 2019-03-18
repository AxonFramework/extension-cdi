package org.axonframework.extensions.cdi.config;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(value = {TYPE, METHOD, PARAMETER, FIELD})
    @Retention(value = RUNTIME)
    @Documented
    @Qualifier
public @interface IfNoneDefined {
    Class<?> value() default Class.class;
}
