package org.axonframework.extensions.cdi.transaction;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.axonframework.common.transaction.Transaction;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.extensions.cdi.annotations.AxonDefaultConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@AxonDefaultConfig
public class JTABridgeTransactionManager implements TransactionManager {

    private static Logger LOGGER = LoggerFactory.getLogger(JTABridgeTransactionManager.class);

    @Inject
    Instance<jakarta.transaction.TransactionManager> transactionManagerInstance;

    @Override
    public Transaction startTransaction() {
        jakarta.transaction.TransactionManager transactionManager;

        if (transactionManagerInstance.isResolvable()) {
            transactionManager = transactionManagerInstance.get();
            return new JTABridgeTransaction(transactionManager);
        } else {
            if (transactionManagerInstance.isAmbiguous())
                LOGGER.warn("Ambiguous TransactionManager reference! Transactions will not work!");
            if (transactionManagerInstance.isUnsatisfied())
                LOGGER.warn("TransactionManager instance was not found! Transactions will not work!");
            return new NoOpTransaction();
        }

    }

    public static class NoOpTransaction implements Transaction {

        @Override
        public void commit() {}

        @Override
        public void rollback() {}
    }

}
