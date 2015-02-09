package com.sap.sse.filestorage.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class ServiceAddedListenerWrappingCustomizer<T> implements ServiceTrackerCustomizer<T, T> {
    private final ServiceAddedListener<T> listener;
    private final BundleContext context;
    
    public ServiceAddedListenerWrappingCustomizer(BundleContext context, ServiceAddedListener<T> listener) {
        this.listener = listener;
        this.context = context;
    }

    @Override
    public T addingService(ServiceReference<T> reference) {
        T service = context.getService(reference);
        listener.onServiceAdded(service);
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<T> reference, T service) {
        //ignore
    }

    @Override
    public void removedService(ServiceReference<T> reference, T service) {
        //ignore
    }
}
