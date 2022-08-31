package org.axonframework.extensions.cdi.transaction;

import org.axonframework.common.transaction.Transaction;

public class JTABridgeTransaction implements Transaction {

    private jakarta.transaction.TransactionManager transactionManager;

    public JTABridgeTransaction(jakarta.transaction.TransactionManager transactionManager) {
        this.transactionManager = transactionManager;

        try {
            transactionManager.begin();
        } catch (Exception e) {
            throw new JTABridgeTransactionException("Can not start transaction", e);
        }
    }

    @Override
    public void commit() {
        try {
            transactionManager.commit();
        } catch (Exception e) {
            throw new JTABridgeTransactionException("Can not commit transaction", e);
        }
    }

    @Override
    public void rollback() {
        try {
            transactionManager.rollback();
        } catch (Exception e) {
            throw new JTABridgeTransactionException("Can not rollback transaction", e);
        }
    }
}
