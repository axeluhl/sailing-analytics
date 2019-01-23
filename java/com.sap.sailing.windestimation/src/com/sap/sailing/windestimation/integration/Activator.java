package com.sap.sailing.windestimation.integration;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.windestimation.WindEstimationFactoryService;
import com.sap.sse.replication.Replicable;
import com.sap.sse.util.ClearStateTestSupport;

/**
 * Handles OSGi (de-)registration of the polar data service.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class Activator implements BundleActivator {

    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    private static final String WIND_ESTIMATION_MODEL_DATA_SOURCE_URL_PROPERTY_NAME = "windestimation.source.url";

    private final Set<ServiceRegistration<?>> registrations = new HashSet<>();

    @Override
    public void start(BundleContext context) throws Exception {
        logger.info("Registering WindEstimationFactoryService");
        WindEstimationFactoryServiceImpl service = new WindEstimationFactoryServiceImpl(true, null);
        final ServiceRegistration<WindEstimationFactoryService> serviceRegistration = context
                .registerService(WindEstimationFactoryService.class, service, null);
        registrations.add(serviceRegistration);
        final Dictionary<String, String> replicableServiceProperties = new Hashtable<>();
        replicableServiceProperties.put(Replicable.OSGi_Service_Registry_ID_Property_Name, service.getId().toString());
        registrations.add(context.registerService(Replicable.class.getName(), service, replicableServiceProperties));
        registrations.add(context.registerService(ClearStateTestSupport.class.getName(), service, null));
        final String windEstimationModelDataSourceURL = System
                .getProperty(WIND_ESTIMATION_MODEL_DATA_SOURCE_URL_PROPERTY_NAME);
        waitForRacingEventServiceToObtainDomainFactoryAndPolarService(windEstimationModelDataSourceURL, service,
                context, serviceRegistration);
    }

    /**
     * Spawns a daemon thread that waits for the domain factory and polar service to be registered with the
     * {@link WindEstimationFactoryService}, then unregisters the service from the OSGi registry because it will
     * temporarily become unusable, runs the wind estimation model data import from the given URL and registers the
     * service again, adding the service registration object to the set of {@link #registrations}. The domain factory is
     * required to resolve boat classes during deserialization. The polar service is required to preload all maneuver
     * classifiers for each existing boat class.
     */
    private void waitForRacingEventServiceToObtainDomainFactoryAndPolarService(
            final String windEstimationModelDataSourceURL,
            final WindEstimationFactoryService windEstimationFactoryService, final BundleContext context,
            ServiceRegistration<WindEstimationFactoryService> windEstimationModelDataServiceRegistration) {
        final Thread t = new Thread(() -> {
            try {
                Thread.currentThread().setContextClassLoader(getClass().getClassLoader()); // ensure that classpath:...
                logger.info("Waiting for domain factory to be registered with WindEstimationFactoryService...");
                // Note: although the domainFactory parameter isn't used, using runWithDomainFactory ensures that the
                // domain factory is there
                windEstimationFactoryService.runWithDomainFactory(domainFactory -> {
                    // TODO implement import client for wind estimation models
                    // PolarDataClient polarDataClient = new PolarDataClient(windEstimationModelDataSourceURL,
                    // windEstimationFactoryService);
                    // try {
                    windEstimationModelDataServiceRegistration.unregister();
                    // polarDataClient.updatePolarDataRegressions();
                    ServiceReference<PolarDataService> polarServiceReference = context
                            .getServiceReference(PolarDataService.class);
                    // TODO ensure that polarDataService has been loaded and initialized
                    PolarDataService polarDataService = context.getService(polarServiceReference);
                    registrations.add(context.registerService(WindEstimationFactoryService.class,
                            new WindEstimationFactoryServiceImpl(false, polarDataService), null));
                    // } catch (Exception e) {
                    // logger.log(Level.SEVERE,
                    // "Exception while trying to import wind estimation model data from " +
                    // windEstimationModelDataSourceURL, e);
                    // }
                });
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Interrupted while waiting for UserStore service", e);
            }
        }, "WindEstimationFactoryService activator waiting for domain factory to be registered");
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        logger.info("Unregistering WindEstimationFactoryService");
        for (ServiceRegistration<?> reg : registrations) {
            reg.unregister();
        }
        registrations.clear();
    }

}
