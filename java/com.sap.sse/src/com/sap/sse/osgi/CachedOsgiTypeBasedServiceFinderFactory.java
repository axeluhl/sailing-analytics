package com.sap.sse.osgi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.common.TypeBasedServiceFinderFactory;

/**
 * Use the OSGi service registry to find the desired services.
 * <p>
 * 
 * Using this indirection it is possible to integrate new service types into the system without the need to change any
 * existing code.
 * <p>
 * 
 * Note that it is not easily possible to use the {@link CachedOsgiTypeBasedServiceFinderFactory} in test case scenarios
 * because there is usually no OSGi context available during test execution. Consider using the
 * {@link MockServiceFinderFactory} instead.
 * 
 * @author Fredrik Teschke
 *
 */
public class CachedOsgiTypeBasedServiceFinderFactory implements TypeBasedServiceFinderFactory {
    private final Map<Class<?>, CachedOsgiTypeBasedServiceFinder<?>> serviceFinders = new HashMap<>();
    private final Set<ServiceTracker<?, ?>> serviceTrackers = new HashSet<>();
    private final BundleContext context;

    public CachedOsgiTypeBasedServiceFinderFactory(BundleContext context) {
        this.context = context;
    }

    @Override
    public <ServiceT> TypeBasedServiceFinder<ServiceT> createServiceFinder(Class<ServiceT> clazz) {
        @SuppressWarnings("unchecked")
        CachedOsgiTypeBasedServiceFinder<ServiceT> finder = (CachedOsgiTypeBasedServiceFinder<ServiceT>) serviceFinders.get(clazz);
        if (finder == null) {
            finder = new CachedOsgiTypeBasedServiceFinder<>(clazz, context);
            ServiceTracker<ServiceT, ServiceT> tracker = new ServiceTracker<ServiceT, ServiceT>(context, clazz, finder);
            serviceFinders.put(clazz, finder);
            serviceTrackers.add(tracker);
            tracker.open();
        }
        return finder;
    }

    public void close() {
        for (ServiceTracker<?, ?> tracker : serviceTrackers) {
            tracker.close();
        }
        serviceTrackers.clear();
        serviceFinders.clear();
    }
}
