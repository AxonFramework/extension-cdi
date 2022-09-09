package org.axonframework.extensions.cdi.test.command;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.axonframework.extensions.cdi.test.TestUtils.echo;

public class CommandProcessingTestComponents {

    public static class Command {
        private final String name;
        public Command (String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }

    }

    @ApplicationScoped
    public static class Handler {
        public static final Logger LOGGER = LoggerFactory.getLogger(Handler.class);

        @Inject
        EventGateway eventGateway;

        @CommandHandler
        public String handle(Command cmd) {
            LOGGER.info("Handling command " + cmd);
            return echo(cmd.getName());
        }
    }

}
