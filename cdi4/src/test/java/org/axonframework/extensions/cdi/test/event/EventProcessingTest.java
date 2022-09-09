package org.axonframework.extensions.cdi.test.event;

import jakarta.inject.Inject;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.extensions.cdi.test.ArchiveTemplates;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.axonframework.extensions.cdi.test.TestUtils.success;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ArquillianExtension.class)
public class EventProcessingTest {

    @Deployment
    public static JavaArchive createDeployment () {
        JavaArchive archive = ArchiveTemplates.javaArchive();
        archive.addAsResource("configs/disable-axonserver.properties","META-INF/microprofile-config.properties");
        archive.addPackage(EventProcessingTest.class.getPackage());
        return archive;
    }

    @Inject
    EventGateway eventGateway;

    @Test
    public void test() {
        success.remove();
        eventGateway.publish(new EventProcessingTestComponents.Event());
        assertNotNull(success.get());
        assertTrue(success.get());
    }
}
