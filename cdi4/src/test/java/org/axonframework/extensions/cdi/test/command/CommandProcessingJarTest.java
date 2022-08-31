package org.axonframework.extensions.cdi.test.command;

import org.axonframework.extensions.cdi.test.ArchiveTemplates;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

public class CommandProcessingJarTest extends CommandProcessingTest {

    @Deployment
    public static JavaArchive createJavaDeployment () {
        JavaArchive archive = ArchiveTemplates.javaArchive();
        archive.addClass(CommandProcessingTestComponents.class);
        return archive;
    }

}
