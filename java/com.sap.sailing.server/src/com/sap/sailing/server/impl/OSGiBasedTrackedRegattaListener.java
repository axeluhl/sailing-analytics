package com.sap.sailing.server.impl;

import java.util.function.Consumer;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRegattaListener;
import com.sap.sse.util.ServiceTrackerFactory;

/**
 * Extended version of {@link TrackedRegattaListenerManagerImpl} that also informs {@link TrackedRegattaListener
 * TrackedRegattaListeners} found in the OSGi service registry.
 */
public class OSGiBasedTrackedRegattaListener extends TrackedRegattaListenerManagerImpl {

    private ServiceTracker<TrackedRegattaListener, TrackedRegattaListener> serviceTracker;

    /**
     * A {@link ServiceTracker} is created using the given {@link BundleContext}. To ensure proper cleanup when the
     * calling {@link BundleActivator} is stopped, call {@link #close()}.
     */
    public OSGiBasedTrackedRegattaListener(BundleContext context) {
        serviceTracker = ServiceTrackerFactory.createAndOpen(context, TrackedRegattaListener.class);
        serviceTracker.open(true);
    }

    public void close() {
        serviceTracker.close();
    }

    /**
     * Encapsulates the common code to iterate over all TrackedRegattaListener instances that are published to the OSGi
     * service registry.
     */
    private void forEachListener(Consumer<TrackedRegattaListener> consumer) {
        for (ServiceReference<TrackedRegattaListener> serviceReference : serviceTracker.getServiceReferences()) {
            TrackedRegattaListener listener = serviceTracker.getService(serviceReference);
            if (listener != null) {
                consumer.accept(listener);
            }
        }
    }

    @Override
    public void regattaAdded(TrackedRegatta trackedRegatta) {
        super.regattaAdded(trackedRegatta);
        forEachListener((listener) -> listener.regattaAdded(trackedRegatta));
    }

    @Override
    public void regattaRemoved(TrackedRegatta trackedRegatta) {
        super.regattaRemoved(trackedRegatta);
        forEachListener((listener) -> listener.regattaRemoved(trackedRegatta));
    }
}
