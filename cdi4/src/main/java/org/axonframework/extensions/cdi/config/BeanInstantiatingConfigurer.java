package org.axonframework.extensions.cdi.config;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.util.AnnotationLiteral;
import org.axonframework.extensions.cdi.annotations.AxonConfig;
import org.axonframework.extensions.cdi.annotations.AxonDefaultConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

public interface BeanInstantiatingConfigurer {

    static Logger LOGGER = LoggerFactory.getLogger(BeanInstantiatingConfigurer.class);


    static final AnnotationLiteral<AxonDefaultConfig> AXON_DEFAULT_CONFIG_ANNOTATION =
            new AnnotationLiteral<AxonDefaultConfig>() {};
    static final AnnotationLiteral<AxonConfig> AXON_CONFIG_ANNOTATION =
            new AnnotationLiteral<AxonConfig>() {};

    default <T> Instance<T> getDefaultInstance (BeanManager beanManager, Class<T> beanClass) {
        return beanManager.createInstance().select(beanClass, AXON_DEFAULT_CONFIG_ANNOTATION);
    }

    default <T> Instance<T> getDefaultInstance
            (BeanManager beanManager, Class<T> beanClass, AnnotationLiteral... annotationLiterals) {

        return beanManager.createInstance().select(
                beanClass,
                merge(AXON_DEFAULT_CONFIG_ANNOTATION, annotationLiterals));
    }

    default <T> Instance<T> getConfiguredInstance (BeanManager beanManager, Class<T> beanClass) {
        return beanManager.createInstance().select(beanClass, AXON_CONFIG_ANNOTATION);
    }

    default <T> Instance<T> getConfiguredInstance
            (BeanManager beanManager, Class<T> beanClass, AnnotationLiteral... annotationLiterals) {

        return beanManager.createInstance().select(
                beanClass,
                merge(AXON_CONFIG_ANNOTATION, annotationLiterals));
    }

    default <T> Instance.Handle<T> getConfiguredOrDefaultInstance
            (BeanManager beanManager, Class<T> beanClass, AnnotationLiteral... annotationLiterals) {
        return getConfiguredOrDefaultInstance(false, beanManager, beanClass, annotationLiterals);
    }

    private <T> Instance.Handle<T> getConfiguredOrDefaultInstance
            (boolean isDefault, BeanManager beanManager, Class<T> beanClass, AnnotationLiteral... annotationLiterals) {

        Instance<T> instance;

        if (isDefault) {
            instance = getDefaultInstance(beanManager, beanClass, annotationLiterals);
        } else {
            instance = getConfiguredInstance(beanManager, beanClass, annotationLiterals);
        }

        if (instance.isResolvable()) {
            LOGGER.debug("Found instance of `{}` in bean: {}", beanClass.getSimpleName(), instance.getHandle().getBean());
            return instance.getHandle();
        }

        if (instance.isAmbiguous()) {
            throw  CDIConfigurationException.ambiguousInstance(beanClass, instance);
        }

        if (!isDefault && instance.isUnsatisfied()) {
            LOGGER.debug("No `{}` bean configured. Attempting to get the default.", beanClass.getSimpleName());
            return getConfiguredOrDefaultInstance(true, beanManager, beanClass, annotationLiterals);
        }

        LOGGER.warn("No `{}` bean found!", beanClass.getSimpleName());
        return null;

    }

    private AnnotationLiteral[] merge (AnnotationLiteral first, AnnotationLiteral... rest) {
        AnnotationLiteral[] merged = new AnnotationLiteral[rest.length + 1];
        merged[0] = first;
        System.arraycopy(rest, 0, merged, 1, rest.length);
        return merged;
    }
}
