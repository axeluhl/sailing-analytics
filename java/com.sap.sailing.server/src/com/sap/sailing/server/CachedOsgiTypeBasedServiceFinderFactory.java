package com.sap.sailing.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.devices.TypeBasedServiceFinder;
import com.sap.sailing.domain.devices.TypeBasedServiceFinderFactory;

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
        CachedOsgiTypeBasedServiceFinder<ServiceT> finder = (CachedOsgiTypeBasedServiceFinder<ServiceT>) serviceFinders
                .get(clazz);

        if (finder == null) {
            finder = new CachedOsgiTypeBasedServiceFinder<>(context);
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
