package com.sap.sse.replication.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.sap.sse.replication.Replicable;

/**
 * Uses an OSGi {@link ServiceTracker} to track and follow the set of {@link Replicable} objects registered with
 * the OSGi service registry under that interface and uses the information and the events from the tracker to
 * prepare the responses to the {@link #getReplicable(String)} and {@link #getReplicables()} method calls.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class OSGiReplicableTracker extends AbstractReplicablesProvider {
    private final ServiceTracker<Replicable<?, ?>, Replicable<?, ?>> serviceTracker;
    private final Map<String, ServiceReference<Replicable<?, ?>>> serviceReferenceByIdAsString;
    private final Map<ServiceReference<Replicable<?, ?>>, String> idAsStringByServiceReference;
    private final BundleContext bundleContext;
    
    public OSGiReplicableTracker(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        this.serviceReferenceByIdAsString = new HashMap<>();
        this.idAsStringByServiceReference = new HashMap<>();
        this.serviceTracker = new ServiceTracker<Replicable<?, ?>, Replicable<?, ?>>(
                bundleContext, Replicable.class.getName(), new ServiceTrackerCustomizer<Replicable<?, ?>, Replicable<?, ?>>() {
                    @Override
                    public Replicable<?, ?> addingService(ServiceReference<Replicable<?, ?>> reference) {
                        final Replicable<?, ?> result = bundleContext.getService(reference);
                        serviceReferenceByIdAsString.put(result.getId().toString(), reference);
                        idAsStringByServiceReference.put(reference, result.getId().toString());
                        notifyReplicableLifeCycleListenersAboutReplicableAdded(result);
                        return result;
                    }

                    @Override
                    public void removedService(ServiceReference<Replicable<?, ?>> reference,
                            Replicable<?, ?> service) {
                        serviceReferenceByIdAsString.remove(idAsStringByServiceReference.get(reference));
                        idAsStringByServiceReference.remove(reference);
                        notifyReplicableLifeCycleListenersAboutReplicableRemoved(
                                (String) reference.getProperty(Replicable.OSGi_Service_Registry_ID_Property_Name));
                    }
                    
                    @Override public void modifiedService(ServiceReference<Replicable<?, ?>> reference, Replicable<?, ?> service) {}
                });
        serviceTracker.open();
    }
    
    @Override
    public Iterable<Replicable<?, ?>> getReplicables() {
        return Arrays.asList(serviceTracker.getServices(new Replicable<?, ?>[0]));
    }

    @Override
    public Replicable<?, ?> getReplicable(String replicableIdAsString) {
        final ServiceReference<Replicable<?, ?>> serviceReference = serviceReferenceByIdAsString.get(replicableIdAsString);
        return serviceReference == null ? null : bundleContext.getService(serviceReference);
    }
}