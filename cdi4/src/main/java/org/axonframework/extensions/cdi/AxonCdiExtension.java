package org.axonframework.extensions.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.*;
import jakarta.enterprise.util.AnnotationLiteral;
import org.axonframework.axonserver.connector.ServerConnectorConfigurerModule;
import org.axonframework.axonserver.connector.TargetContextResolver;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.config.*;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.extensions.cdi.annotations.Aggregate;
import org.axonframework.extensions.cdi.annotations.AxonConfig;
import org.axonframework.extensions.cdi.annotations.AxonInternal;
import org.axonframework.extensions.cdi.config.*;
import org.axonframework.messaging.annotation.MessageHandler;
import org.axonframework.serialization.xml.XStreamSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Main CDI extension class responsible for collecting CDI beans and setting up
 * Axon configuration.
 */
public class AxonCdiExtension implements Extension {

    private static Logger LOGGER = LoggerFactory.getLogger(AxonCdiExtension.class);

    private static final AnnotationLiteral<AxonConfig> AXON_CONFIG_ANNOTATION = new AnnotationLiteral<AxonConfig>() {};

    static {
        LOGGER.info("Axon CDI Extension loaded");
    }

    private List<ComponentConfigurer> componentConfigurers = new LinkedList<>();

    private AggregatesRegisterer aggregatesRegisterer = new AggregatesRegisterer();
    private MessageHandlingComponentsRegisterer messageHandlingComponentsRegisterer =
            new MessageHandlingComponentsRegisterer();


    public AxonCdiExtension () {
        componentConfigurers.add(new EventStoreConfigurer());
        componentConfigurers.add(new TransactionManagerConfigurer());

    }

    public void beforeBeanDiscovery (@Observes final BeforeBeanDiscovery bbdEvent) {
        LOGGER.debug("beforeBeanDiscovery");
    }

    <T> void observeMessageHandlingComponents(@Observes
                       @WithAnnotations(MessageHandler.class) final ProcessAnnotatedType<T> event) {

        if (event.getAnnotatedType().isAnnotationPresent(Aggregate.class)) {
            LOGGER.debug("found aggregate in : " + event.getAnnotatedType().getJavaClass());
            aggregatesRegisterer.add(event.getAnnotatedType());
        } else {
            LOGGER.debug("found message handler in : " + event.getAnnotatedType().getJavaClass());
            messageHandlingComponentsRegisterer.add(event.getAnnotatedType());
        }

    }

    void afterBeanDiscovery(@Observes final AfterBeanDiscovery afterBeanDiscovery,
                            final BeanManager beanManager) {
        LOGGER.debug("Configuring Axon Framework");

        afterBeanDiscovery.addBean()
                .types(Configuration.class)
                .qualifiers(new AnnotationLiteral<Default>() {}, new AnnotationLiteral<Any>() {})
                .scope(ApplicationScoped.class)
                .name(Configuration.class.getName())
                .beanClass(Configuration.class)
                .createWith(context -> {

                    Set<Class> disabledModules = new HashSet<>();

                    Configurer configurer = DefaultConfigurer.defaultConfiguration(false);

                    AxonCDIConfig axonCDIConfig = beanManager.createInstance().select(AxonCDIConfig.class).get();

                    ServiceLoader<ConfigurerModule> configurerModuleLoader = ServiceLoader.load(ConfigurerModule.class, configurer.getClass().getClassLoader());
                    List<OptionalConfigurerModule> configurerModules = new LinkedList<>();
                    configurerModuleLoader.forEach(configurerModule -> {
                        configurerModules.add(new OptionalConfigurerModule(configurerModule, axonCDIConfig));
                    });
                    configurerModules.stream()
                            .filter(OptionalConfigurerModule::isEnabled)
                            .map(OptionalConfigurerModule::getModule)
                            .sorted(Comparator.comparingInt(ConfigurerModule::order))
                            .forEach((cm) -> {
                                cm.configureModule(configurer);
                            });

                    componentConfigurers.forEach(c -> c.configure(beanManager, configurer));
                    aggregatesRegisterer.registerAnnotatedTypes(beanManager, configurer);
                    messageHandlingComponentsRegisterer.registerAnnotatedTypes(beanManager, configurer);


                    /*  --------------------------------------------------
                        Temporary dirty hacks
                        TODO: move those to respective proper places

                     */

                    configurer.configureSerializer(c -> XStreamSerializer.defaultSerializer());

                    /*  -------------------------------------------------- */

                    Configuration configuration = configurer.buildConfiguration();
                    return configuration;
                });

    }


    void afterDeploymentValidation(@Observes final AfterDeploymentValidation afterDeploymentValidation,
                                   final BeanManager beanManager) {
        LOGGER.debug("Starting Axon Framework");

        Configuration configuration = beanManager.createInstance()
                .select(Configuration.class,
                        new AnnotationLiteral<Default>() {},
                        new AnnotationLiteral<Any>() {}
                        ).get();

        configuration.onStart(Integer.MIN_VALUE, () -> {
            LOGGER.debug("Axon Framework Started!");
//            printConfiguration();
        });
        configuration.onShutdown(Integer.MIN_VALUE, () -> LOGGER.debug("Axon Framework Stopped!"));
        configuration.start();
    }


    void beforeShutdown(@Observes @Destroyed(ApplicationScoped.class) final Object event) {
        Configuration configuration = CDI.current().select(Configuration.class).get();
        if (configuration != null) {
            configuration.shutdown();
        }
    }

    private static void printConfiguration() {

        Configuration configuration = CDI.current().select(Configuration.class).get();

        Map<String, String> components = new HashMap<>();
        components.put("Command Bus", nameOf(configuration.commandBus()));
        components.put("Command Gateway", nameOf(configuration.commandGateway()));
        components.put("Event Bus", nameOf(configuration.eventBus()));
        components.put("Event Gateway", nameOf(configuration.eventGateway()));
        components.put("Event Store", nameOf(configuration.eventStore()));
        components.put("Query Bus", nameOf(configuration.queryBus()));
        components.put("Query Gateway", nameOf(configuration.queryGateway()));
        components.put("Event Scheduler", nameOf(configuration.eventScheduler()));
        components.put("Event Serializer", nameOf(configuration.eventSerializer()));
        components.put("Message Serializer", nameOf(configuration.messageSerializer()));
        components.put("Serializer", nameOf(configuration.serializer()));
        components.put("Token Store", nameOf(configuration.getComponent(TokenStore.class)));
        components.put("Event Storage Engine", nameOf(configuration.getComponent(EventStorageEngine.class)));
        components.put("Target Context Resolver", nameOf(configuration.getComponent(TargetContextResolver.class)));

        String formatComponent = "| %-25s | %-105s |%n";
        String formatModule = "| %-133s |%n";

        System.out.println("+---------------------------------------------------------------------------------------------------------------------------------------+");
        System.out.println("| ### Axon Framework configuration ###                                                                                                  |");
        System.out.println("+---------------------------+-----------------------------------------------------------------------------------------------------------+");
        System.out.println("| \uD83D\uDD2E Component              | \uD83E\uDDD9️ Implementation                                                                                        |");
        System.out.println("+---------------------------+-----------------------------------------------------------------------------------------------------------+");

        components.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> System.out.format(formatComponent, entry.getKey(), entry.getValue()));

        System.out.println("+---------------------------+-----------------------------------------------------------------------------------------------------------+");
        System.out.println("| 🧩 enabled modules                                                                                                                    |");
        System.out.println("+---------------------------------------------------------------------------------------------------------------------------------------+");

        configuration.getModules().forEach(module -> {
            System.out.format(formatModule, " " + module);
        });

        System.out.println("+---------------------------------------------------------------------------------------------------------------------------------------+");

    }

    private static String nameOf (Object o) {
        return Objects.toString(o);
    }
 }
