package org.axonframework.extensions.cdi.annotations;

import jakarta.enterprise.inject.Stereotype;
import org.axonframework.modelling.command.AnnotationCommandTargetResolver;
import org.axonframework.modelling.command.CommandTargetResolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Stereotype
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Aggregate {

    // TODO: Update javadocs !!!


    /**
     * Sets the name of the bean providing the entire aggregate configuration.
     *
     * If none is provided, the default configuration will be used.
     */
    String configurer() default "";

    /**
     * Sets the name of the bean providing the AggregateRepository bean.
     *
     * If left empty a new repository is created. In that case the
     * name of the repository will be based on the simple name of the aggregate's class.
     *
     * If provided, it overwrites the value provided by the configurer
     */
    String repository() default "";

    /**
     * Sets the name of the bean providing the snapshot trigger definition. If none is provided, no snapshots are
     * created, unless explicitly configured on the referenced repository.
     * <p>
     * Note that the use of {@link #repository()}, or provisioning a
     * {@link org.axonframework.modelling.command.Repository} to the Spring context using the default naming scheme
     * overrides this setting, as a Repository explicitly defines the snapshot trigger definition. The default name
     * corresponds to {@code "[aggregate-name]Repository"}, thus a {@code Trade} Aggregate would by default create/look
     * for a bean named {@code "tradeRepository"}.
     */
    String snapshotTriggerDefinition() default "";

    /**
     * Sets the name of the bean providing the {@link org.axonframework.eventsourcing.snapshotting.SnapshotFilter}. If
     * none is provided, all snapshots will be taken into account unless explicitly configured on the event store.
     */
    String snapshotFilter() default "";

    /**
     * Get the String representation of the aggregate's type. Optional. This defaults to the simple name of the
     * annotated class.
     */
    String type() default "";

    /**
     * Selects the name of the {@link CommandTargetResolver} bean. If left empty,
     * {@link CommandTargetResolver} bean from application context will be used. If
     * the bean is not defined in the application context, {@link AnnotationCommandTargetResolver}
     * will be used.
     */
    String commandTargetResolver() default "";

    /**
     * Sets whether to filter events by Aggregate type. This is used to support installations where multiple
     * Aggregate types can have overlapping Aggregate identifiers. This is only meaningful for event-sourced
     * Aggregates.
     */
    boolean filterEventsByType() default false;

    /**
     * Sets the name of the bean providing the caching. If none is provided, the default cache will be used. If
     * there is no default cache or `ignoreDefaultCache` is specified, the cache explicitly configured on the
     * referenced repository (if any) will be used.
     */
    String cache() default "";

    /**
     * Sets whether to ignore the default cache if no named cache is provided.
     */
    boolean ignoreDefaultCache() default false;

}
