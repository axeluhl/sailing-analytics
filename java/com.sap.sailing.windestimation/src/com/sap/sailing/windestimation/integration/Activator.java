package com.sap.sailing.windestimation.integration;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.sap.sailing.domain.windestimation.WindEstimationFactoryService;
import com.sap.sailing.windestimation.model.exception.ModelLoadingException;
import com.sap.sailing.windestimation.model.exception.ModelPersistenceException;
import com.sap.sailing.windestimation.model.store.FileSystemModelStore;
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
    private static final String WIND_ESTIMATION_MODEL_DATA_SOURCE_FOLDER_PROPERTY_NAME = "windestimation.source.folder";

    private final Set<ServiceRegistration<?>> registrations = new HashSet<>();

    @Override
    public void start(BundleContext context) throws Exception {
        logger.info("Registering WindEstimationFactoryService");
        final String windEstimationModelDataSourceURL = System
                .getProperty(WIND_ESTIMATION_MODEL_DATA_SOURCE_URL_PROPERTY_NAME);
        final String windEstimationModelDataSourceFolder = System
                .getProperty(WIND_ESTIMATION_MODEL_DATA_SOURCE_FOLDER_PROPERTY_NAME);
        if (windEstimationModelDataSourceURL != null && windEstimationModelDataSourceFolder != null) {
            throw new ModelLoadingException("Maximal one of the two parameters must be provided: \""
                    + WIND_ESTIMATION_MODEL_DATA_SOURCE_URL_PROPERTY_NAME + "\" or \""
                    + WIND_ESTIMATION_MODEL_DATA_SOURCE_FOLDER_PROPERTY_NAME + "\"");
        }
        if (windEstimationModelDataSourceURL != null) {
            importWindEstimationModelsFromUrl(windEstimationModelDataSourceURL);
        }
        WindEstimationFactoryServiceImpl service = new WindEstimationFactoryServiceImpl();
        if (windEstimationModelDataSourceFolder != null) {
            importWindEstimationModelsFromFolder(service, windEstimationModelDataSourceFolder);
        }
        final ServiceRegistration<WindEstimationFactoryService> serviceRegistration = context
                .registerService(WindEstimationFactoryService.class, service, null);
        registrations.add(serviceRegistration);
        final Dictionary<String, String> replicableServiceProperties = new Hashtable<>();
        replicableServiceProperties.put(Replicable.OSGi_Service_Registry_ID_Property_Name, service.getId().toString());
        registrations.add(context.registerService(Replicable.class.getName(), service, replicableServiceProperties));
        registrations.add(context.registerService(ClearStateTestSupport.class.getName(), service, null));
    }

    private void importWindEstimationModelsFromFolder(WindEstimationFactoryServiceImpl service,
            String windEstimationModelDataSourceFolder) {
        FileSystemModelStore modelStore = new FileSystemModelStore(windEstimationModelDataSourceFolder);
        try {
            service.importAllModelsFromModelStore(modelStore);
        } catch (ModelPersistenceException e) {
            throw new ModelLoadingException(
                    "Could not import wind estimation models from folder :" + windEstimationModelDataSourceFolder, e);
        }
    }

    private void importWindEstimationModelsFromUrl(String windEstimationModelDataSourceURL) {
        logger.info("Importing wind estimation data from URL: " + windEstimationModelDataSourceURL);
        // TODO implement import client for wind estimation models
        // PolarDataClient polarDataClient = new PolarDataClient(windEstimationModelDataSourceURL,
        // windEstimationFactoryService);
        // try {
        // polarDataClient.updatePolarDataRegressions();
        // TODO ensure that polarDataService has been loaded and initialized
        // } catch (Exception e) {
        // logger.log(Level.SEVERE,
        // "Exception while trying to import wind estimation model data from " +
        // windEstimationModelDataSourceURL, e);
        // }
        logger.info("Import of wind estimation data finished successfully ");
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
