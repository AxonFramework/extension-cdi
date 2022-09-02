package org.axonframework.extensions.cdi.test.query;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import org.axonframework.extensions.cdi.AxonCDIConfguration;
import org.axonframework.queryhandling.QueryHandler;

import static org.axonframework.extensions.cdi.test.TestUtils.echo;

public class QueryProcessingTestComponents {

    public static class Query {
        private final String query;
        public Query(String query) { this.query = query; }
        public String getQuery() { return query; }
    }

    public static class QueryResult {
        private final String text;
        public QueryResult(String text) { this.text = text; }
        public String getText() { return text; }
    }

    @ApplicationScoped
    public static class Handler {

        @QueryHandler
        public QueryResult handle(Query query) {
            return new QueryResult(echo(query.getQuery()));
        }
    }

    @Dependent
    static class Config {
        @Produces
        public AxonCDIConfguration axonCDIConfguration () {
            return AxonCDIConfguration.builder()
                    .disableAxonServerConnector(true)
                    .build();
        }
    }
}
