package org.axonframework.extensions.cdi.test.command;

import org.axonframework.extensions.cdi.test.ArchiveTemplates;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class CommandProcessingWarFlatTest extends CommandProcessingTest {

    @Deployment
    public static WebArchive createWebDeployment () {
        WebArchive archive = ArchiveTemplates.webArchiveFlat();
        archive.addClass(CommandProcessingTestComponents.class);
        return archive;
    }

}
