package org.axonframework.extensions.cdi.config;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.BeanManager;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.Configurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionManagerConfigurer implements ComponentConfigurer {

    private static Logger LOGGER = LoggerFactory.getLogger(TransactionManagerConfigurer.class);

    @Override
    public void configure(BeanManager beanManager, Configurer configurer) {
        Instance.Handle<TransactionManager> transactionManagerHandle =
                getConfiguredOrDefaultInstance(beanManager, TransactionManager.class);

        if (transactionManagerHandle != null) {
            configurer.configureTransactionManager(c -> transactionManagerHandle.get());
            LOGGER.info("Registered `TransactionManager` from bean: {}", transactionManagerHandle.getBean());
        }

    }
}
