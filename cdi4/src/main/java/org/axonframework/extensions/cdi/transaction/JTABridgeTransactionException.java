package org.axonframework.extensions.cdi.transaction;

public class JTABridgeTransactionException extends RuntimeException {

    public JTABridgeTransactionException(String message) {
        super(message);
    }

    public JTABridgeTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
