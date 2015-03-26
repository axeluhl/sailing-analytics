package com.sap.sse.util;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class ServiceTrackerFactory {
    /**
     * @return {@code null} if {@code context} is {@code null}
     */
    public static <T> ServiceTracker<T, T> createAndOpen(BundleContext context, Class<T> clazz) {
        if (context == null) {
            return null;
        }
        ServiceTracker<T, T> result = new ServiceTracker<T, T>(context, clazz, null);
        result.open();
        return result;
    }
}
