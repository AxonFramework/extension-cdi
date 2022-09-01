package org.axonframework.extensions.cdi.config;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Named;
import org.axonframework.common.caching.Cache;
import org.axonframework.config.AggregateConfigurer;
import org.axonframework.config.Configurer;
import org.axonframework.extensions.cdi.annotations.Aggregate;
import org.axonframework.extensions.cdi.annotations.AxonConfig;
import org.axonframework.extensions.cdi.annotations.AxonDefaultConfig;
import org.axonframework.modelling.command.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class AggregatesRegisterer<T> implements AnnotatedTypeRegisterer<T> {

    private static Logger LOGGER = LoggerFactory.getLogger(AggregatesRegisterer.class);

    Set<AnnotatedType> aggregateAnnotatedTypes = new HashSet<>();

    @Override
    public void add(AnnotatedType<T> annotatedType) {
        aggregateAnnotatedTypes.add(annotatedType);
    }

    @Override
    public void registerAnnotatedTypes(BeanManager beanManager, Configurer configurer) {
        aggregateAnnotatedTypes.forEach( aac -> {
            Class aggregateClass = aac.getJavaClass();
            Aggregate annotation = aac.getAnnotation(Aggregate.class);

            AggregateConfigurer aggregateConfigurer = AggregateConfigurer.defaultConfiguration(aggregateClass);

            String repositoryName = annotation.repository();
            if (repositoryName.isBlank()) {
                // TODO: wire default repository ?!?!
            } else {
                Instance<Repository> repositoryInstance = getConfiguredInstance(
                        beanManager,
                        Repository.class,
                        NamedLiteral.of(repositoryName)
                );

                if (repositoryInstance.isResolvable()) {
                    Repository repository = repositoryInstance.get();
                    aggregateConfigurer.configureRepository(config -> repository);
                    LOGGER.debug("Found `{}` named `{}`", repository, repositoryName);
                } else {
                    LOGGER.warn("Could not find repository named `{}` configured at `{}`", repositoryName, aggregateClass);
                }

            }

            String cacheName = annotation.cache();
            boolean configured = false;
            if (!cacheName.isBlank()) {
                configured = configureCache(beanManager, cacheName, aggregateConfigurer);
            }
            if (!configured && !annotation.ignoreDefaultCache()) {
                configured = configureCache(beanManager, null, aggregateConfigurer);
            }

            annotation.snapshotTriggerDefinition();
            // TODO: wire snapshotTriggerDefinition

            annotation.snapshotFilter();
            // TODO: wire snapshotFilter


            configurer.configureAggregate(aggregateConfigurer);
        });
    }

    private boolean configureCache (BeanManager beanManager, String cacheName, AggregateConfigurer aggregateConfigurer) {
        Instance<Cache> cacheInstance;

        if (cacheName != null) {
            cacheInstance = getConfiguredInstance(
                beanManager,
                Cache.class,
                NamedLiteral.of(cacheName)
            );
        } else {
            cacheInstance = getDefaultInstance(
                    beanManager,
                    Cache.class
            );

        }

        if (cacheInstance.isResolvable()) {
            Cache cache = cacheInstance.get();
            aggregateConfigurer.configureCache(configuration -> cache);
            LOGGER.debug("Found `{}` named `{}`", cache, cacheName);
            return true;
        } else if (cacheName != null) {
            LOGGER.warn("Could not find cache named `{}` configured at `{}`",
                    cacheName,
                    aggregateConfigurer.aggregateType());
        }

        return false;
    }
}