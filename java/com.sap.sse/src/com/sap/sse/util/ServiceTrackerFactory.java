package com.sap.sse.util;

import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class ServiceTrackerFactory {
    private static final Logger logger = Logger.getLogger(ServiceTrackerFactory.class.getName());
    
    /**
     * @return {@code null} if {@code context} is {@code null}
     */
    public static <T> ServiceTracker<T, T> createAndOpen(BundleContext context, Class<T> clazz) {
        final ServiceTracker<T, T> result;
        if (context == null) {
            logger.info("No BundleContext provided. Returning null.");
            result = null;
        } else {
            result = new ServiceTracker<T, T>(context, clazz, null);
            result.open();
        }
        return result;
    }
}
