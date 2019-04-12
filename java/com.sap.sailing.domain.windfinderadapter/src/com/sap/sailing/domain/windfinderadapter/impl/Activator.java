package com.sap.sailing.domain.windfinderadapter.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.sap.sailing.domain.tracking.WindTrackerFactory;
import com.sap.sailing.domain.windfinder.WindFinderTrackerFactory;

public class Activator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    
    static final String BASE_URL_FOR_JSON_DOCUMENTS = "http://external.windfinder.com/sap";

    private static BundleContext context;
    
    /**
     * Registrations of OSGi services to be de-registered when the bundle shuts down
     */
    private Set<ServiceRegistration<?>> registrations = new HashSet<>();

    static BundleContext getContext() {
        return context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        logger.info("Creating ExpeditionTrackerFactory");
        final WindFinderTrackerFactoryImpl windfinderTrackerFactory = new WindFinderTrackerFactoryImpl();
        registrations.add(context.registerService(WindFinderTrackerFactory.class, windfinderTrackerFactory, /* properties */null));
        registrations.add(context.registerService(WindTrackerFactory.class, windfinderTrackerFactory, /* properties */null));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bundleContext) throws Exception {
        for (ServiceRegistration<?> reg : registrations) {
            reg.unregister();
        }
        registrations.clear();
        Activator.context = null;
    }

}
