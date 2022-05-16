package com.sap.sse.util;

import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class ServiceTrackerFactory {
    private static final Logger logger = Logger.getLogger(ServiceTrackerFactory.class.getName());
    
    /**
     * @return {@code null} if {@code context} is {@code null}
     */
    public static <T> ServiceTracker<T, T> createAndOpen(BundleContext context, Class<T> clazz) {
        return createAndOpen(context, clazz, /* customizer */ null);
    }
    
    public static <T> ServiceTracker<T, T> createAndOpen(BundleContext context, Class<T> clazz, ServiceTrackerCustomizer<T, T> customizer) {
        final ServiceTracker<T, T> result;
        if (context == null) {
            logger.info("No BundleContext provided. Returning null.");
            result = null;
        } else {
            result = new ServiceTracker<T, T>(context, clazz, customizer);
            result.open();
        }
        return result;
    }
}
