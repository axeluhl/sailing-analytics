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
import com.sap.sailing.polars.ReplicablePolarService;
import com.sap.sailing.polars.jaxrs.client.PolarDataClient;
import com.sap.sse.replication.Replicable;
import com.sap.sse.util.ClearStateTestSupport;

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
        final ServiceRegistration<PolarDataService> polarDataServiceRegistration = context.registerService(PolarDataService.class, service, null);
        registrations.add(polarDataServiceRegistration);
        final Dictionary<String, String> replicableServiceProperties = new Hashtable<>();
        replicableServiceProperties.put(Replicable.OSGi_Service_Registry_ID_Property_Name, service.getId().toString());
        registrations.add(context.registerService(Replicable.class.getName(), service, replicableServiceProperties));
        registrations.add(context.registerService(ClearStateTestSupport.class.getName(), service, null));
        final String polarDataSourceURL = System.getProperty(POLAR_DATA_SOURCE_URL_PROPERTY_NAME);
        if (polarDataSourceURL != null && !polarDataSourceURL.isEmpty()) {
            waitForRacingEventServiceToObtainDomainFactory(polarDataSourceURL, service, context, polarDataServiceRegistration);
        }
    }
    
    /**
     * Spawns a daemon thread that waits for the domain factory to be registered with the {@link PolarDataService}, then
     * unregisters the service from the OSGi registry because it will temporarily become unusable, runs the polar data
     * import from the given URL and registers the service again, adding the service registration object to the set of
     * {@link #registrations}. The domain factory is required to resolve boat classes during de-serialization.
     * 
     * @param polarDataServiceRegistration
     *            used to remove the service registration temporarily while updating the service by a remote import
     * @param registerPolarServiceCallback
     *            called when the polar data has successfully been obtained; expected to register the polar service as
     *            {@link Replicable} and as the {@link PolarDataService} with the OSGi registry
     */
    private void waitForRacingEventServiceToObtainDomainFactory(final String polarDataSourceURL,
            final ReplicablePolarService polarService, final BundleContext context,
            ServiceRegistration<PolarDataService> polarDataServiceRegistration) {
        final Thread t = new Thread(() -> {
                try {
                    Thread.currentThread().setContextClassLoader(getClass().getClassLoader()); // ensure that classpath:... Shiro ini files are resolved properly
                    logger.info("Waiting for domain factory to be registered with PolarService...");
                    // Note: although the domainFactory parameter isn't used, using runWithDomainFactory ensures that the domain factory is there
                    polarService.runWithDomainFactory(domainFactory -> { 
                        PolarDataClient polarDataClient = new PolarDataClient(polarDataSourceURL, polarService);
                        try {
                            polarDataServiceRegistration.unregister();
                            polarDataClient.updatePolarDataRegressions();
                            registrations.add(context.registerService(PolarDataService.class, polarService, null));
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
