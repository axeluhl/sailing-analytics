package com.sap.sailing.server.impl;

import java.util.function.Consumer;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRegattaListener;
import com.sap.sse.util.ServiceTrackerFactory;

public class OSGiBasedTrackedRegattaListener implements TrackedRegattaListener {
    
    private ServiceTracker<TrackedRegattaListener, TrackedRegattaListener> serviceTracker;

    public OSGiBasedTrackedRegattaListener(BundleContext context) {
        serviceTracker = ServiceTrackerFactory.createAndOpen(context, TrackedRegattaListener.class);
        serviceTracker.open(true);
    }

    public void close() {
        serviceTracker.close();
    }
    
    private void forEachListener(Consumer<TrackedRegattaListener> consumer) {
        for(ServiceReference<TrackedRegattaListener> serviceReference : serviceTracker.getServiceReferences()) {
            TrackedRegattaListener listener = serviceTracker.getService(serviceReference);
            if(listener != null) {
                consumer.accept(listener);
            }
        }
    }

    @Override
    public void regattaAdded(TrackedRegatta trackedRegatta) {
        forEachListener((listener) -> listener.regattaAdded(trackedRegatta));
    }

    @Override
    public void regattaRemoved(TrackedRegatta trackedRegatta) {
        forEachListener((listener) -> listener.regattaRemoved(trackedRegatta));
    }
}
