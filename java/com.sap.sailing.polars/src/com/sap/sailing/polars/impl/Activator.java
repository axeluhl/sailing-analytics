package com.sap.sailing.polars.impl;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sse.replication.Replicable;

public class Activator implements BundleActivator {
    
    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    
    private final Set<ServiceRegistration<?>> registrations = new HashSet<>();

    @Override
    public void start(BundleContext context) throws Exception {
        logger.info("Registering PolarDataService");
        PolarDataServiceImpl service = new PolarDataServiceImpl();
        registrations.add(context.registerService(PolarDataService.class, service, null));
        final Dictionary<String, String> replicableServiceProperties = new Hashtable<>();
        replicableServiceProperties.put(Replicable.OSGi_Service_Registry_ID_Property_Name, service.getId().toString());
        registrations.add(context.registerService(Replicable.class.getName(), service, replicableServiceProperties));
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
