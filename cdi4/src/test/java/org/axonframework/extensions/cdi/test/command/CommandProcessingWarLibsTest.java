package org.axonframework.extensions.cdi.test.command;

import org.axonframework.extensions.cdi.test.ArchiveTemplates;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.formatter.Formatters;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandProcessingWarLibsTest extends CommandProcessingTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandProcessingWarLibsTest.class);

    @Deployment
    public static WebArchive createWebDeployment () {
        WebArchive archive = ArchiveTemplates.webArchiveWithLibs();
        archive.addClass(CommandProcessingTestComponents.class);

        LOGGER.debug("Making archive with following content:\n" + archive.toString(Formatters.VERBOSE));
        return archive;
    }

}
