package com.sap.sse.osgi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TypeBasedServiceFinder;

/**
 * Caches OSGI services of a certain type, based on their {@link TypeBasedServiceFinder#TYPE}
 * {@link BundleContext#registerService(Class, Object, java.util.Dictionary) property}. Has to be registered as the
 * {@link ServiceTrackerCustomizer} of a {@link ServiceTracker}. The {@link #findService(String)} method looks
 * up the requested type string against the {@link TypeBasedServiceFinder#TYPE} property that OSGi-based
 * service implementations must have provided as a property during service registration with the OSGi service
 * registry. Don't confuse this "type" string with the classes of the service implementations which all
 * have to implement {@code ServiceT} but whose name has nothing to do with the type string used here for
 * service lookup.
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

    @Override
    public Set<ServiceT> findAllServices() {
        return new HashSet<ServiceT>(services.values());
    }
}
