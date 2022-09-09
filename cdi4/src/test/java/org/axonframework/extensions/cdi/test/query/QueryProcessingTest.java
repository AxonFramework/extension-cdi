package org.axonframework.extensions.cdi.test.query;

import jakarta.inject.Inject;
import org.axonframework.extensions.cdi.test.ArchiveTemplates;
import org.axonframework.queryhandling.QueryGateway;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.ExecutionException;

import static org.axonframework.extensions.cdi.test.TestUtils.echo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ArquillianExtension.class)
public class QueryProcessingTest {

    @Inject
    QueryGateway queryGateway;

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive archive = ArchiveTemplates.javaArchive();
        archive.addPackage(QueryProcessingTest.class.getPackage());
        archive.addAsResource("configs/disable-axonserver.properties","META-INF/microprofile-config.properties");
        return archive;
    }

    @Test
    public void test() throws ExecutionException, InterruptedException {
        String message = "test command";
        String result = queryGateway.query(
                new QueryProcessingTestComponents.Query(message),
                QueryProcessingTestComponents.QueryResult.class
        ).get().getText();
        assertEquals(echo(message), result);
    }
}
