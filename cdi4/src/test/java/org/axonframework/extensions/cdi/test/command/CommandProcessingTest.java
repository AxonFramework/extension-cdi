package org.axonframework.extensions.cdi.test.command;

import jakarta.inject.Inject;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.ExecutionException;

import static org.axonframework.extensions.cdi.test.TestUtils.echo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ArquillianExtension.class)
public abstract class CommandProcessingTest {

    @Inject
    CommandGateway commandGateway;

    @Test
    public void test() {
        String message = "test command";
        String result = (String) commandGateway.sendAndWait(new CommandProcessingTestComponents.Command(message));
        assertEquals(echo(message), result);
    }
}
