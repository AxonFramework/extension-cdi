package org.axonframework.extensions.cdi.transaction;

import org.axonframework.common.AxonException;

public class JtaTransactionException extends AxonException {


    public JtaTransactionException(String message) {
        super(message);
    }

    public JtaTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
