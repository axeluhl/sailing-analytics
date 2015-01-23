package com.sap.sse.osgi;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TypeBasedServiceFinder;

/**
 * Caches OSGI services of a certain type, based on their type property. Has to be registered as the
 * {@link ServiceTrackerCustomizer} of a {@link ServiceTracker}.
 * 
 * @author Fredrik Teschke
 * 
 * @param <ServiceT>
 *            the type of service to be tracked
 */
public class CachedOsgiTypeBasedServiceFinder<ServiceT> implements ServiceTrackerCustomizer<ServiceT, ServiceT>,
        TypeBasedServiceFinder<ServiceT> {
    private final Map<String, ServiceT> services = new HashMap<>();
    private final BundleContext context;
    private Class<ServiceT> serviceType;
    private ServiceT fallback;

    public CachedOsgiTypeBasedServiceFinder(Class<ServiceT> serviceType, BundleContext context) {
    	this.serviceType = serviceType;
        this.context = context;
    }

    @Override
    public ServiceT findService(String type) {
        ServiceT service = services.get(type);

        if (service == null) {
            if (fallback != null) {
                return fallback;
            } else {
                throw new NoCorrespondingServiceRegisteredException("Could not find service", type, serviceType.getSimpleName());       
            }
        }

        return service;
    }

    @Override
    public ServiceT addingService(ServiceReference<ServiceT> reference) {
        String type = (String) reference.getProperty(TypeBasedServiceFinder.TYPE);
        ServiceT service = context.getService(reference);
        services.put(type, service);
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<ServiceT> reference, ServiceT service) {
        addingService(reference);

    }

    @Override
    public void removedService(ServiceReference<ServiceT> reference, ServiceT service) {
        String type = (String) reference.getProperty(TypeBasedServiceFinder.TYPE);
        services.remove(type);
    }

    @Override
    public void setFallbackService(ServiceT fallback) {
        this.fallback = fallback;
    }
}
