package org.axonframework.extensions.cdi.test.aggregate.configurer;

import jakarta.inject.Inject;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.Configuration;
import org.axonframework.extensions.cdi.test.ArchiveTemplates;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.formatter.Formatters;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.axonframework.extensions.cdi.test.TestUtils.success;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ArquillianExtension.class)
public class AggregateWithConfigurerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AggregateWithConfigurerTest.class);

    @Deployment
    public static JavaArchive createDeployment () {
        JavaArchive archive = ArchiveTemplates.javaArchive();
        archive.addPackage(AggregateWithConfigurerTest.class.getPackage());
        LOGGER.debug(archive.toString(Formatters.VERBOSE));
        return archive;
    }

    @Inject
    CommandGateway commandGateway;

    @Inject
    Configuration configuration;

    @Test
    public void test() {
        success.remove();
        String id = commandGateway.sendAndWait(new AggregateTestComponents.TestCommand());
        commandGateway.sendAndWait(new AggregateTestComponents.TestCommand2(id));
        assertNotNull(success.get());
        assertTrue(success.get());
    }
}
