package com.sap.sse.osgi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.common.Util;

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
    private static final Logger logger = Logger.getLogger(CachedOsgiTypeBasedServiceFinder.class.getName());
    
    /**
     * Keys are the service types, as obtained through {@code reference.getProperty(TypeBasedServiceFinder.TYPE)}.
     * To allow for {@code null} types, {@link #getNullSafeType(String)} must be used to map type strings that
     * may be {@code null} to key strings that are not {@code null} and where the {@code null} string value
     * is represented by a dedicated "token."
     */
    private final ConcurrentHashMap<String, ServiceT> services = new ConcurrentHashMap<>();
    private final BundleContext context;
    private final Class<ServiceT> serviceType;
    private ServiceT fallback;
    private final Map<String, Set<Callback<ServiceT>>> callbacks;

    public CachedOsgiTypeBasedServiceFinder(Class<ServiceT> serviceType, BundleContext context) {
    	this.serviceType = serviceType;
        this.context = context;
        this.callbacks = new HashMap<>();
    }

    @Override
    public ServiceT findService(String type) {
        ServiceT service = services.get(getNullSafeType(type));
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
    public void applyServiceWhenAvailable(String type, Callback<ServiceT> callback) {
        final ServiceT service; 
        synchronized (services) {
            service = services.get(getNullSafeType(type));
            if (service == null) {
                Util.addToValueSet(callbacks, getNullSafeType(type), callback);
            }
        }
        // no synchronization needed here anymore if the service was already found
        if (service != null) {
            callback.withService(service);
        }
    }

    @Override
    public ServiceT addingService(ServiceReference<ServiceT> reference) {
        final String type = (String) reference.getProperty(TypeBasedServiceFinder.TYPE);
        final ServiceT service = context.getService(reference);
        final Set<Callback<ServiceT>> callbacksToNotify;
        synchronized (services) {
            services.put(getNullSafeType(type), service);
            callbacksToNotify = callbacks.remove(type);
        }
        if (callbacksToNotify != null) {
            callbacksToNotify.forEach(c -> {
                try {
                    c.withService(service);
                } catch (Exception e) {
                    logger.log(Level.SEVERE,
                            "Exception trying to inform " + c + " about the availability of service " + service,e);
                }
            });
        }
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<ServiceT> reference, ServiceT service) {
        addingService(reference);
    }

    @Override
    public void removedService(ServiceReference<ServiceT> reference, ServiceT service) {
        String type = (String) reference.getProperty(TypeBasedServiceFinder.TYPE);
        services.remove(getNullSafeType(type));
    }

    /**
     * Used in {@link #getNullSafeType(String)} to ensure that no {@code null} keys are used in the {@link #services}
     * map. This value is equivalent to a {@code null} key in {@link #services}.
     */
    private static final String NULL_STRING = UUID.randomUUID().toString();
    private String getNullSafeType(String type) {
        return type==null?NULL_STRING:type;
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
