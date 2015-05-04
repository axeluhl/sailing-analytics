package com.sap.sailing.polars.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.sap.sailing.domain.polars.PolarDataService;

public class Activator implements BundleActivator {
    
    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    
    private final Set<ServiceRegistration<?>> registrations = new HashSet<>();

    @Override
    public void start(BundleContext context) throws Exception {
        logger.info("Registering PolarDataService");
        registrations.add(context.registerService(PolarDataService.class, new PolarDataServiceImpl(), null));
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        logger.info("Unregistering PolarDataService");
        for (ServiceRegistration<?> reg : registrations) {
            reg.unregister();
        }
        registrations.clear();
    }

}
