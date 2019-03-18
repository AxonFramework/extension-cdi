package org.axonframework.extensions.cdi.config;

import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.extensions.cdi.transaction.JtaTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import java.lang.invoke.MethodHandles;

@ApplicationScoped
public class CdiProducer {

    private static final Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    @Produces @IfNoneDefined
    @Singleton
    public TransactionManager transactionManager() {
        logger.info("transactionManager()");

        return new JtaTransactionManager();
    }

}
