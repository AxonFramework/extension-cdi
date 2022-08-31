package org.axonframework.extensions.cdi.test.aggregate.statestored;

import jakarta.inject.Inject;
import org.axonframework.commandhandling.gateway.CommandGateway;
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
public class AggregateTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AggregateTest.class);

    @Inject
    CommandGateway commandGateway;

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive archive = ArchiveTemplates.javaArchive();
        archive.addAsResource("persistence.xml", "META-INF/persistence.xml");
        archive.addPackage(AggregateTest.class.getPackage());
        LOGGER.debug("Making archive with following content:\n" + archive.toString(Formatters.VERBOSE));
        return archive;
    }

    @Test
    public void test() {
        success.remove();
        commandGateway.sendAndWait(new AggregateTestComponents.TestCommand());
        assertNotNull(success.get());
        assertTrue(success.get());
    }
}
