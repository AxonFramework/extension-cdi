package org.axonframework.extensions.cdi.test.paramresolver;

import jakarta.inject.Inject;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.conflictresolution.NoConflictResolver;
import org.axonframework.extensions.cdi.test.ArchiveTemplates;
import org.axonframework.messaging.MetaData;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.formatter.Formatters;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ArquillianExtension.class)
public class ParameterResolverTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterResolverTest.class);

    @Inject
    CommandGateway commandGateway;

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive archive = ArchiveTemplates.javaArchive();
        archive.addAsResource("persistence.xml", "META-INF/persistence.xml");
        archive.addPackage(ParameterResolverTest.class.getPackage());
        LOGGER.debug("Making archive with following content:\n" + archive.toString(Formatters.VERBOSE));
        return archive;
    }

    @Test
    public void testMetaInCommand() {
        String value = "meta";
        String result = (String) commandGateway.sendAndWait(
                new ParameterResolverTestComponents.CommandMeta(),
                MetaData.with(ParameterResolverTestComponents.KEY, value)
        );
        assertEquals("handleWithMeta:" + value, result);
    }

    @Test
    public void testMessageInCommand() {
        String value = "message";
        String result = (String) commandGateway.sendAndWait(
                new ParameterResolverTestComponents.CommandMessage(),
                MetaData.with(ParameterResolverTestComponents.KEY, value)
        );
        assertEquals("handleWithMessage:" + value, result);
    }

    @Test
    public void testUnitOfWorkInCommand() {
        String value = "UoW";
        String result = (String) commandGateway.sendAndWait(
                new ParameterResolverTestComponents.CommandUoW(),
                MetaData.with(ParameterResolverTestComponents.KEY, value)
        );
        assertEquals("handleWithUoW:" + value, result);
    }

    @Test
    public void testIdInCommand() {
        String result = (String) commandGateway.sendAndWait(
                new ParameterResolverTestComponents.CommandId()
        );
        assertTrue(result.startsWith("handleWithId:"));
    }

    @Test
    public void testConflictResolverInCommand() {
        String result = (String) commandGateway.sendAndWait(
                new ParameterResolverTestComponents.CommandConflictResolver()
        );
        assertEquals("handleWithConflictResolver:" + NoConflictResolver.class, result);
    }

}
