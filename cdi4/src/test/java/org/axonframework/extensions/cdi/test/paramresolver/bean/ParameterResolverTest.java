package org.axonframework.extensions.cdi.test.paramresolver.bean;

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

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ArquillianExtension.class)
public class ParameterResolverTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterResolverTest.class);

    @Inject
    CommandGateway commandGateway;

    @Deployment
    public static JavaArchive createDeployment () {
        JavaArchive archive = ArchiveTemplates.javaArchive();
        archive.addAsResource("persistence.xml", "META-INF/persistence.xml");
        archive.addPackage(org.axonframework.extensions.cdi.test.paramresolver.bean.ParameterResolverTest.class.getPackage());
        LOGGER.debug("Making archive with following content:\n" + archive.toString(Formatters.VERBOSE));
        return archive;
    }


    @Test
    public void testBeanInCommand() {
        String result = (String) commandGateway.sendAndWait(
                new ParameterResolverTestComponents.CommandBean()
        );
        assertEquals("handleWithBean:myBean", result);
    }

    @Test
    public void testNamedBeanInCommand() {
        String result = (String) commandGateway.sendAndWait(
                new ParameterResolverTestComponents.CommandBean2()
        );
        assertEquals("handleWithBean:myOtherBean", result);
    }

}
