package org.axonframework.extensions.cdi.util;

import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import java.util.function.Supplier;

public class SupplierBasedLifeCycle<T> implements ContextualLifecycle<T> {

    private final Supplier<T> supplier;

    public SupplierBasedLifeCycle(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T create(Bean<T> bean, CreationalContext<T> creationalContext) {
        return supplier.get();
    }

    @Override
    public void destroy(Bean<T> bean, T t, CreationalContext<T> creationalContext) {
        // Donothing
    }
}
