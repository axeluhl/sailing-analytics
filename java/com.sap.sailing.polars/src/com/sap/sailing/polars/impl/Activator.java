package com.sap.sailing.polars.impl;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.polars.jaxrs.client.PolarDataClient;
import com.sap.sse.replication.Replicable;

/**
 * Handles OSGi (de-)registration of the polar data service. 
 * 
 * @author D054528 (Frederik Petersen)
 *
 */
public class Activator implements BundleActivator {

    private static final String POLAR_DATA_SOURCE_URL_PROPERTY_NAME = "polardata.source.url";

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
        
        String polarDataSourceURL = System.getProperty(POLAR_DATA_SOURCE_URL_PROPERTY_NAME);
        if (polarDataSourceURL != null && !polarDataSourceURL.isEmpty()) {
            waitForRacingEventServiceToObtainDomainFactory(polarDataSourceURL, service);
        }
    }
    
    /**
     * Spawns a daemon thread that waits for the domain factory to be registered with the {@link PolarDataService}, then
     * runs the polar data import from the given URL. The domain factory is required to resolve boat classes during
     * de-serialization.
     */
    private void waitForRacingEventServiceToObtainDomainFactory(final String polarDataSourceURL, final PolarDataServiceImpl polarService) {
        final Thread t = new Thread(()->{
                try {
                    Thread.currentThread().setContextClassLoader(getClass().getClassLoader()); // ensure that classpath:... Shiro ini files are resolved properly
                    logger.info("Waiting for domain factory to be registered with PolarService...");
                    polarService.runWithDomainFactory(domainFactory -> { 
                        PolarDataClient polarDataClient = new PolarDataClient(polarDataSourceURL, polarService, domainFactory);
                        try {
                            polarDataClient.updatePolarDataRegressions();
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, "Exception while trying to import polar data from "+polarDataSourceURL, e);
                        }
                        });
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "Interrupted while waiting for UserStore service", e);
                }
            }, "PolarService activator waiting for domain factory to be registered");
        t.setDaemon(true);
        t.start();
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
