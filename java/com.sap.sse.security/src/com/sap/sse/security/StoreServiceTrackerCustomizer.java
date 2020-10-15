package com.sap.sse.security;

import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.sap.sse.security.interfaces.UserStore;

public abstract class StoreServiceTrackerCustomizer<T> implements ServiceTrackerCustomizer<T, T> {
    private final Logger logger;

    private final BundleContext context;

    public StoreServiceTrackerCustomizer(BundleContext context, Logger logger) {
        this.logger = logger;
        this.context = context;
    }

    protected abstract void setStore(T store);

    protected abstract void removeStore();

    protected abstract T getStore();

    @Override
    public T addingService(ServiceReference<T> reference) {
        T contextStore = context.getService(reference);
        T thisStore = getStore();
        if (thisStore != null && thisStore != contextStore) {
            logger.severe("Multiple " + UserStore.class.getSimpleName()
                    + " instances found. Only one instance is handled.");
        } else {
            setStore(contextStore);
        }
        return contextStore;
    }

    @Override
    public void modifiedService(ServiceReference<T> reference, T store) {
    }

    @Override
    public void removedService(ServiceReference<T> reference, T store) {
        if (getStore() == store) {
            removeStore();
        }
    }
}
