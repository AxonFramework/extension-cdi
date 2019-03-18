package org.axonframework.extensions.cdi;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.common.lock.LockFactory;
import org.axonframework.common.lock.NullLockFactory;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.*;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.extensions.cdi.config.ConfigurerProducer;
import org.axonframework.extensions.cdi.config.IfNoneDefined;
import org.axonframework.extensions.cdi.messagehandling.MessageHandlingBeanDefinition;
import org.axonframework.extensions.cdi.stereotype.Aggregate;
import org.axonframework.extensions.cdi.util.CdiUtilities;
import org.axonframework.extensions.cdi.util.SupplierBasedLifeCycle;
import org.axonframework.modelling.command.CommandTargetResolver;
import org.axonframework.modelling.command.GenericJpaRepository;
import org.axonframework.modelling.command.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.*;
import javax.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.axonframework.extensions.cdi.util.CdiUtilities.*;
import static org.axonframework.extensions.cdi.util.StringUtilities.lowerCaseFirstLetter;


/**
 * @author Bert Laverman
 */
@Singleton
public class AxonCdiExtension implements Extension {

    private static final Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    private final List<AggregateDefinition> aggregates = new ArrayList<>();
    private final Map<String, Producer<Repository>> aggregateRepositoryProducerMap = new HashMap<>();
    private final Map<String, Producer<SnapshotTriggerDefinition>> snapshotTriggerDefinitionProducerMap = new HashMap<>();
    private final Map<String, Producer<CommandTargetResolver>> commandTargetResolverProducerMap = new HashMap<>();

    private final List<MessageHandlingBeanDefinition> messageHandlers = new ArrayList<>();

    /**
     * Produce a bean, using the given {@link BeanManager} and {@link Producer}.
     *
     * @param beanManager the CDI BeanManager to provide the context.
     * @param producer the (registered) producer for this bean class.
     * @param <T> The class of the bean.
     * @return the requested instance.
     */
    private <T> T produce(BeanManager beanManager, Producer<T> producer) {
        return producer.produce(beanManager.createCreationalContext(null));
    }

    private Map<Class<?>, AnnotatedType<?>> defaultBeans = new HashMap<>();
    private Map<Type, Producer<?>> defaultProducers = new HashMap<>();
    private Map<Type, Class<?>> defaultProducerBeanClasses = new HashMap<>();
    private Set<Type> nonDefaultProducers = new HashSet<>();

    /**
     * Collect bean classes marked with the {code @IfNoneDefined} annotation.
     *
     * @param pat the {@link ProcessAnnotatedType} event.
     * @param <T> the class of the bean.
     */
    <T> void processDefaultBeans(@Observes final ProcessAnnotatedType<T> pat) {
        AnnotatedType<T> at = pat.getAnnotatedType();
        IfNoneDefined noneDefined = at.getAnnotation(IfNoneDefined.class);

        if (noneDefined != null) {
            logger.info("processAnnotatedType(): Found default bean. Vetoing it for now.");
            defaultBeans.put(noneDefined.value(), at);
            pat.veto();
        }
    }

    /**
     * Collect the producers marked with the {@code @IfNoneDefined} qualifier.
     *
     * @param processProducer the {@link ProcessProducer} event.
     * @param <T> the bean class of the bean that declares the producer.
     * @param <X> the return type of the producer.
     */
    <T, X> void processDefaultProducers(@Observes final ProcessProducer<T, X> processProducer) {
        AnnotatedMember<T> member = processProducer.getAnnotatedMember();
        if (member instanceof AnnotatedMethod) {
            final AnnotatedMethod<?> am = (AnnotatedMethod)member;
            final Type returnType = am.getBaseType();

            final IfNoneDefined noneDefined = am.getAnnotation(IfNoneDefined.class);

            if (noneDefined != null) {
                logger.info("processAnnotatedType(): Found default producer for {}. \"{}::{}\".",
                        returnType,
                        am.getDeclaringType().getJavaClass().getName(),
                        am.getJavaMember().getName());
                defaultProducers.put(returnType, processProducer.getProducer());
                defaultProducerBeanClasses.put(returnType, am.getJavaMember().getReturnType());
            }
            else {
                nonDefaultProducers.add(returnType);
            }
        }
//        else if (member instanceof AnnotatedField) {
//            logger.info("processDefaultProducers(): Ignoring @Produces on a {}.", member.getClass().getName());
//        }
//        else {
//            logger.info("processDefaultProducers(): Ignoring @Produces on a {}.", member.getClass().getName());
//        }
    }

    /**
     * Add beans from all {@code @IfNoneDefined} marked producers, if none is already avilable.
     *
     * @param afterBeanDiscovery the {@link AfterBeanDiscovery} event.
     * @param mgr the {@link BeanManager}.
     */
    private void addMissingProducers(final AfterBeanDiscovery afterBeanDiscovery, final BeanManager mgr) {
        logger.info("addMissingProducers(): Checking for missing producers.");
        for (Type tp: defaultProducers.keySet()) {
            if (!nonDefaultProducers.contains(tp)) {
                if (defaultProducers.containsKey(tp)) {
                    final Class<?> beanClass = defaultProducerBeanClasses.get(tp);
                    logger.info("addMissingProducers(): Enabling our default producer for \"{}\" (beanClass \"{}\".", tp, beanClass);
                    afterBeanDiscovery.addBean(new BeanBuilder<>(mgr)
                            .beanClass(beanClass)
                            .types(tp)
                            .scope(ApplicationScoped.class)
                            .qualifiers(AnnotationInstanceProvider.of(Default.class))
                            .beanLifecycle(new SupplierBasedLifeCycle<>(() -> produce(mgr, defaultProducers.get(tp))))
                            .create());
                }
                else {
                    logger.warn("addMissingProducers(): We seem to lack a producer for \"{}\".", tp);
                }
            }
        }
        logger.info("addMissingProducers(): Done");
    }

    private <T> Bean<T> defineBean(BeanManager beanManager, AnnotatedType<T> annotatedType) {
        BeanAttributes<T> beanAttributes = beanManager.createBeanAttributes(annotatedType);
        InjectionTargetFactory<T> injectionTargetFactory = beanManager.getInjectionTargetFactory(annotatedType);
        return beanManager.createBean(beanAttributes, annotatedType.getJavaClass(), injectionTargetFactory);
    }

    private void addMissingBeans(final AfterBeanDiscovery afterBeanDiscovery, final BeanManager mgr) {
        logger.info("addMissingBeans(): Checking for missing beans.");

        defaultBeans.entrySet().stream()
                .filter(e -> mgr.getBeans(e.getKey()).isEmpty())
                .map(e -> defineBean(mgr, e.getValue()))
                .forEach(afterBeanDiscovery::addBean);

        logger.info("addMissingBeans(): Done");
    }

    /**
     * Collect all classes annotated with {@link Aggregate}.
     *
     * @param processAnnotatedType the CDI wrapper for the class.
     * @param <T> the class itself.
     */
    <T> void processAggregate(@Observes @WithAnnotations({Aggregate.class})
                              final ProcessAnnotatedType<T> processAnnotatedType) {
        // TODO Aggregate classes may need to be vetoed so that CDI does not
        // actually try to manage them.

        final Class<T> clazz = processAnnotatedType.getAnnotatedType().getJavaClass();

        logger.info("Found aggregate: {}.", clazz);

        aggregates.add(new AggregateDefinition(clazz));
    }

    /**
     * Collect {@link Repository} producers.
     *
     * @param processProducer the CDI wrapper for the producer.
     * @param <T> the producer's class.
     */
    <T> void processAggregateRepositoryProducer(
            @Observes final ProcessProducer<T, Repository> processProducer) {
        logger.info("Found producer for repository: {}.", processProducer.getProducer());

        AnnotatedMember<T> member = processProducer.getAnnotatedMember();
        if (member instanceof AnnotatedMethod) {
            logger.info("processAggregateRepositoryProducer(): Checking @Produces on a method.");
            AnnotatedMethod<?> am = (AnnotatedMethod)member;
            final Type type = am.getBaseType();
            logger.info("processAggregateRepositoryProducer(): The baseType is {}. ({})", type.toString(), type.getClass().getName());
            if (type instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) type;
                logger.info("processAggregateRepositoryProducer(): It is a ParameterizedType.");
                for (Type tp: pType.getActualTypeArguments()) {
                    logger.info("processAggregateRepositoryProducer(): - {}", tp.toString());
                }
            }
        }
        else if (member instanceof AnnotatedField) {
            logger.info("processAggregateRepositoryProducer(): Ignoring @Produces on a {}.", member.getClass().getName());
        }
        else {
            logger.info("processAggregateRepositoryProducer(): Ignoring @Produces on a {}.", member.getClass().getName());
        }
        String repositoryName = CdiUtilities.extractBeanName(processProducer.getAnnotatedMember());

        this.aggregateRepositoryProducerMap.put(repositoryName, processProducer.getProducer());
    }

    /**
     * Collect {@link SnapshotTriggerDefinition} producers.
     *
     * @param processProducer the CDI wrapper for the producer.
     * @param <T> the producer's class.
     */
    <T> void processSnapshotTriggerDefinitionProducer(
            @Observes final ProcessProducer<T, SnapshotTriggerDefinition> processProducer) {
        logger.info("Found producer for snapshot trigger definition: {}.",
                processProducer.getProducer());

        String triggerDefinitionName = CdiUtilities.extractBeanName(processProducer.getAnnotatedMember());

        this.snapshotTriggerDefinitionProducerMap.put(triggerDefinitionName, processProducer.getProducer());
    }

    /**
     * Collect {@link CommandTargetResolver} producers.
     *
     * @param processProducer the CDI wrapper for the producer.
     * @param <T> the producer's class.
     */
    <T> void processCommandTargetResolverProducer(
            @Observes final ProcessProducer<T, CommandTargetResolver> processProducer) {
        logger.info("Found producer for command target resolver: {}.", processProducer.getProducer());

        String resolverName = CdiUtilities.extractBeanName(processProducer.getAnnotatedMember());

        this.commandTargetResolverProducerMap.put(resolverName, processProducer.getProducer());
    }

    /**
     * Collect message handlers for later processing.
     * @param processBean the CDI wrapper for the bean.
     * @param <T> the class of the bean.
     */
    <T> void processBean(@Observes final ProcessBean<T> processBean) {
        MessageHandlingBeanDefinition.inspect(processBean.getBean(), processBean.getAnnotated())
                .ifPresent(bean -> {
                    logger.debug("Found {}.", bean);
                    messageHandlers.add(bean);
                });
    }

    @SuppressWarnings("unchecked")
    private void registerAggregates(final AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager, Configurer configurer) {
        aggregates.forEach(aggregateDefinition -> {
            logger.info("registerAggregates(): Registering aggregate: {}.", aggregateDefinition.aggregateType().getSimpleName());

            AggregateConfigurer<?> aggregateConfigurer
                    = AggregateConfigurer.defaultConfiguration(aggregateDefinition.aggregateType());

            if (aggregateDefinition.repository().isPresent()) {

                final String repoBeanName = aggregateDefinition.repository().get();
                logger.info("registerAggregates(): This aggregate has a repository specified by name (\"{}\").", repoBeanName);

                aggregateConfigurer.configureRepository(c -> produce(beanManager, aggregateRepositoryProducerMap
                                .get(repoBeanName)));
            } else {
                logger.info("registerAggregates(): This aggregate has a no repository specified.");

                final String repoName = aggregateDefinition.repositoryName();
                if (aggregateRepositoryProducerMap.containsKey(repoName)) {
                    logger.info("registerAggregates(): We have a producer for \"{}\".", repoName);

                    aggregateConfigurer.configureRepository(
                            c -> produce(beanManager, aggregateRepositoryProducerMap.get(repoName)));
                } else {
                    logger.info("registerAggregates(): This aggregate has a no repository and no producer specified.");

                    String repositoryName = lowerCaseFirstLetter(aggregateDefinition.aggregateType().getSimpleName()) + "Repository";
                    String factoryName = lowerCaseFirstLetter(aggregateDefinition.aggregateType().getName()) + "AggregateFactory";
                    logger.info("registerAggregates(): Repository beanName = \"{}\", factory name = \"{}\".", repoName, factoryName);

                    Optional<Repository> repo = getInstance(beanManager, repositoryName, Repository.class);
                    if (!repo.isPresent()) {
                        logger.info("registerAggregates(): We have no registered bean for \"{}\".", repositoryName);

                        final ParameterizedType repoType = TypeUtils.parameterize(Repository.class, aggregateDefinition.aggregateType());
                        logger.info("registerAggregates(): Adding a bean with type {}", repoType);

                        afterBeanDiscovery.addBean(new BeanBuilder<>(beanManager)
                                .beanClass(Repository.class)
                                .types(repoType)
                                .qualifiers(AnnotationInstanceProvider.of(Default.class))
                                .scope(ApplicationScoped.class)
                                .beanLifecycle(new SupplierBasedLifeCycle<>(() -> aggregateConfigurer.repository()))
                                .create());
                    }
                    else {
                        logger.info("registerAggregates(): We have a registered bean named \"{}\".", repositoryName);
                    }

                    // TODO: 8/29/2018 check how to do in CDI world: aggregate factory
                    aggregateDefinition.snapshotTriggerDefinition().ifPresent(triggerDefinition -> aggregateConfigurer
                            .configureSnapshotTrigger(
                                    c -> produce(beanManager, snapshotTriggerDefinitionProducerMap
                                            .get(triggerDefinition))));
                    if (aggregateDefinition.isJpaAggregate()) {
                        aggregateConfigurer.configureRepository(
                                c -> (Repository) GenericJpaRepository.builder(aggregateDefinition.aggregateType())
                                        // TODO: 8/29/2018 what to do about default EntityManagerProvider (check spring impl)
                                        .entityManagerProvider(c.getComponent(EntityManagerProvider.class))
                                        .eventBus(c.eventBus())
                                        .repositoryProvider(c::repository)
                                        .lockFactory(c.getComponent(LockFactory.class, () -> NullLockFactory.INSTANCE))
                                        .parameterResolverFactory(c.parameterResolverFactory())
                                        .handlerDefinition(c.handlerDefinition(aggregateDefinition.aggregateType()))
                                        .build());
                    }
                }
            }

            if (aggregateDefinition.commandTargetResolver().isPresent()) {
                aggregateConfigurer.configureCommandTargetResolver(
                        c -> produce(beanManager,
                                commandTargetResolverProducerMap.get(aggregateDefinition.commandTargetResolver().get())));
            } else {
                commandTargetResolverProducerMap.keySet()
                        .stream()
                        .filter(resolver -> aggregates.stream()
                                .filter(a -> a.commandTargetResolver().isPresent())
                                .map(a -> a.commandTargetResolver().get())
                                .noneMatch(resolver::equals))
                        .findFirst() // TODO: 8/29/2018 what if there are more "default" resolvers
                        .ifPresent(resolver -> aggregateConfigurer.configureCommandTargetResolver(
                                c -> produce(beanManager,
                                        commandTargetResolverProducerMap.get(resolver))));
            }

            configurer.configureAggregate(aggregateConfigurer);
        });
    }

    private void registerMessageHandlers(final BeanManager beanManager, final Configurer configurer) {
        final EventProcessingConfigurer eventProcessingConfigurer = configurer.eventProcessing();

        messageHandlers.stream().filter(MessageHandlingBeanDefinition::isMessageHandler).forEach(handler -> {
            switch (handler.getScope()) {
                case SINGLETON:
                case APPLICATION_SCOPED: {
                    final Function<Configuration, Object> handlerBuilder = c -> getInstance(handler.getBean().getBeanClass());
                    if (handler.isEventSourcingHandler()) {
                        logger.warn("registerMessageHandlers(): Odd, an @EventSourcingHandler that is not an Aggregate? Ignoring for now.");
                    }
                    else if (handler.isEventHandler()) {
                        logger.info("registerMessageHandlers(): Registering \"{}\" as event handler.", handler.getBean().getBeanClass().getName());

                        eventProcessingConfigurer.registerEventHandler(handlerBuilder);
                    }
                    if (handler.isCommandHandler()) {
                        logger.info("registerMessageHandlers(): Registering \"{}\" as command handler.", handler.getBean().getBeanClass().getName());

                        configurer.registerCommandHandler(handlerBuilder);
                    }
                    if (handler.isQueryHandler()) {
                        logger.info("registerMessageHandlers(): Registering \"{}\" as query handler.", handler.getBean().getBeanClass().getName());

                        configurer.registerQueryHandler(handlerBuilder);
                    }
                    break;
                }
                case SESSION_SCOPED:
                case CONVERSATION_SCOPED:
                case REQUEST_SCOPED:
                case DEPENDENT:
                case UNKNOWN: {
                    logger.error("registerMessageHandlers(): Only dealing with @ApplicationScoped and @Singleton for now. Ignoring \"{}\".", handler.getBean().getBeanClass().getName());
                }
            }
        });
    }

    private void registerModules(final BeanManager mgr, Configurer configurer) {
        registerConfigurerModules(mgr, configurer);
        registerModuleConfigurations(mgr, configurer);
    }

    private void registerConfigurerModules(final BeanManager mgr, Configurer configurer) {
        Set<Bean<?>> configurerModules = mgr.getBeans(ConfigurerModule.class);
        for (Bean<?> configurerModuleBean : configurerModules) {
            ConfigurerModule configurerModule = (ConfigurerModule) configurerModuleBean.create(mgr.createCreationalContext(null));
            configurerModule.configureModule(configurer);
        }
    }

    private void registerModuleConfigurations(final BeanManager mgr, Configurer configurer) {
        Set<Bean<?>> moduleConfigurations = mgr.getBeans(ModuleConfiguration.class);
        for (Bean<?> moduleConfigurationBean : moduleConfigurations) {
            configurer.registerModule(new LazyRetrievedModuleConfiguration(
                    () -> (ModuleConfiguration) moduleConfigurationBean.create(mgr.createCreationalContext(null)),
                    moduleConfigurationBean.getBeanClass())
            );
        }
    }

    private Configuration buildConfiguration(Configurer configurer) {
        logger.info("Starting Axon configuration.");

        return configurer.buildConfiguration();
    }

    private void afterTypeDiscovery(@Observes final AfterTypeDiscovery afterTypeDiscovery) {
        logger.info("******* AfterTypeDiscovery");
    }

    private void beforeBeanDiscovery(@Observes final BeforeBeanDiscovery beforeBeanDiscovery) {
        logger.info("******* BeforeBeanDiscovery");
    }

    private void afterBeanDiscovery(@Observes final AfterBeanDiscovery afterBeanDiscovery,
                            final BeanManager beanManager) {
        logger.info("******* AfterBeanDiscovery");

        final Configurer configurer = ConfigurerProducer.instance();

        registerModules(beanManager, configurer);

        registerAggregates(afterBeanDiscovery, beanManager, configurer);
        registerMessageHandlers(beanManager, configurer);

        addMissingProducers(afterBeanDiscovery, beanManager);
        addMissingBeans(afterBeanDiscovery, beanManager);

        resolveBean(beanManager, EventStorageEngine.class)
                .ifPresent(bean -> configurer.configureEmbeddedEventStore(c -> getBean(beanManager, bean, EventStorageEngine.class).get()));
        resolveBean(beanManager, TransactionManager.class)
                .ifPresent(bean -> configurer.configureTransactionManager(c -> getBean(beanManager, bean, TransactionManager.class).get()));
        resolveBean(beanManager, EventBus.class)
                .ifPresent(bean -> configurer.configureEventBus(c -> getBean(beanManager, bean, EventBus.class).get()));

        afterBeanDiscovery.addBean(new BeanBuilder<>(beanManager)
                .beanClass(Configuration.class)
                .types(Configuration.class)
                .qualifiers(AnnotationInstanceProvider.of(Default.class))
                .scope(ApplicationScoped.class)
                .beanLifecycle(new SupplierBasedLifeCycle<>(() -> buildConfiguration(configurer)))
                .create());
    }

    private void afterDeploymentValidation(
            @Observes final AfterDeploymentValidation afterDeploymentValidation,
            final BeanManager beanManager) {
        logger.info("******* AfterDeploymentValidation");
    }

    private void init(@Observes @Initialized(ApplicationScoped.class) Object initialized) {
        logger.info("init(): ApplicationScoped is @Initialized!");
    }

    private void destroy(@Observes @Destroyed(ApplicationScoped.class) final Object destroyed) {
        logger.info("init(): ApplicationScoped is @Destroyed!");
    }

    private void beforeShutdown(@Observes BeforeShutdown event, BeanManager beanManager) {
        logger.info("beforeShutdown()");
    }

    private static class LazyRetrievedModuleConfiguration implements ModuleConfiguration {

        private final Supplier<ModuleConfiguration> delegateSupplier;
        private final Class<?> moduleType;
        private ModuleConfiguration delegate;

        LazyRetrievedModuleConfiguration(Supplier<ModuleConfiguration> delegateSupplier, Class<?> moduleType) {
            this.delegateSupplier = delegateSupplier;
            this.moduleType = moduleType;
        }

        @Override
        public void initialize(Configuration config) {
            getDelegate().initialize(config);
        }

        @Override
        public void start() {
            getDelegate().start();
        }

        @Override
        public void shutdown() {
            getDelegate().shutdown();
        }

        @Override
        public int phase() {
            return getDelegate().phase();
        }

        @Override
        public ModuleConfiguration unwrap() {
            return getDelegate();
        }

        @Override
        public boolean isType(Class<?> type) {
            return type.isAssignableFrom(moduleType);
        }

        private ModuleConfiguration getDelegate() {
            if (delegate == null) {
                delegate = delegateSupplier.get();
            }
            return delegate;
        }
    }

}
