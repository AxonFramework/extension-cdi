package org.axonframework.extensions.cdi.test.aggregate.caching;

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

import static org.axonframework.extensions.cdi.test.TestUtils.result;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ArquillianExtension.class)
public class AggregateTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AggregateTest.class);

    @Deployment
    public static JavaArchive createDeployment () {
        JavaArchive archive = ArchiveTemplates.javaArchive();
        archive.addPackage(AggregateTest.class.getPackage());
        LOGGER.debug(archive.toString(Formatters.VERBOSE));
        return archive;
    }

    @Inject
    CommandGateway commandGateway;

    @Test
    public void testNamedCache() {
        AggregateTestComponents.Command1 c1 = new AggregateTestComponents.Command1();
        c1.id = "ID";
        AggregateTestComponents.Command12 c2 = new AggregateTestComponents.Command12();
        c2.id = c1.id;

        result.remove();
        commandGateway.sendAndWait(c1);
        commandGateway.sendAndWait(c2);
        assertNotNull(result.get());
        assertEquals("NamedCache", result.get());
    }

    @Test
    public void testDefaultCache() {
        AggregateTestComponents.Command2 c1 = new AggregateTestComponents.Command2();
        c1.id = "ID";
        AggregateTestComponents.Command22 c2 = new AggregateTestComponents.Command22();
        c2.id = c1.id;

        result.remove();
        commandGateway.sendAndWait(c1);
        commandGateway.sendAndWait(c2);
        assertNotNull(result.get());
        assertEquals("DefaultCache", result.get());
    }

    @Test
    public void testNoCache() {
        AggregateTestComponents.Command3 c1 = new AggregateTestComponents.Command3();
        c1.id = "ID";
        AggregateTestComponents.Command32 c2 = new AggregateTestComponents.Command32();
        c2.id = c1.id;

        result.remove();
        commandGateway.sendAndWait(c1);
        commandGateway.sendAndWait(c2);
        assertNull(result.get());
    }
}
