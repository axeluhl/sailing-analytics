package com.sap.sailing.windestimation.integration;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.sap.sailing.domain.windestimation.WindEstimationFactoryService;
import com.sap.sailing.windestimation.jaxrs.client.WindEstimationDataClient;
import com.sap.sailing.windestimation.model.exception.ModelLoadingException;
import com.sap.sailing.windestimation.model.exception.ModelPersistenceException;
import com.sap.sailing.windestimation.model.store.FileSystemModelStoreImpl;
import com.sap.sailing.windestimation.model.store.ModelDomainType;
import com.sap.sailing.windestimation.model.store.MongoDbModelStoreImpl;
import com.sap.sse.mongodb.AlreadyRegisteredException;
import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.replication.Replicable;
import com.sap.sse.util.ClearStateTestSupport;

/**
 * Handles OSGi (de-)registration of the wind estimation factory service.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class Activator implements BundleActivator {

    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    
    /**
     * Use a system property by this name to specify a server base URL, such as
     * {@code https://www.sapsailing.com} in case you'd like to load the models
     * from there in case they are <em>not yet</em> stored in the local database.
     */
    private static final String WIND_ESTIMATION_MODEL_DATA_SOURCE_URL_PROPERTY_NAME = "windestimation.source.url";

    /**
     * Use a system property by this name to specify a server base URL, such as {@code https://www.sapsailing.com} in
     * case you'd like to <em>always</em> load the models from there upon activation of this bundle, regardless of
     * whether they are stored yet in the local database.
     */
    private static final String WIND_ESTIMATION_MODEL_DATA_SOURCE_ALWAYS_URL_PROPERTY_NAME = "windestimation.source.always.url";
    
    /**
     * Use a system property by this name to specify the path to a folder in the
     * file system that contains serialized model files to load.
     */
    private static final String WIND_ESTIMATION_MODEL_DATA_SOURCE_FOLDER_PROPERTY_NAME = "windestimation.source.folder";
    
    /**
     * If a system property with this name is provided, the property value will be used as a bearer token in an HTTP
     * header <tt>Authorization: Bearer ...</tt> to authenticate the request fetching the wind estimation data from the
     * URL specified in either the {@link #WIND_ESTIMATION_MODEL_DATA_SOURCE_URL_PROPERTY_NAME} or the
     * {@link #WIND_ESTIMATION_MODEL_DATA_SOURCE_ALWAYS_URL_PROPERTY_NAME} property.
     */
    private static final String WIND_ESTIMATION_MODEL_BEARER_TOKEN_PROPERTY_NAME = "windestimation.source.bearertoken";

    private final Set<ServiceRegistration<?>> registrations = new HashSet<>();
    
    private WindEstimationFactoryServiceImpl service;

    @Override
    public void start(BundleContext context) throws Exception {
        logger.info("Registering WindEstimationFactoryService");
        registerIncorporatedMongoDbCollectionsExclusively();
        // Initialize the factory with its model cache; this will try to load existing models from the DB on a master
        // node. If this fails, e.g., because no model is stored in the DB yet, or it was removed from the DB, or the
        // model serialization pattern has changed incompatibly, other methods of obtaining a wind estimation model set
        // will be evaluated below. See also bug5716.
        service = new WindEstimationFactoryServiceImpl();
        final String windEstimationModelDataSourceURL = System
                .getProperty(WIND_ESTIMATION_MODEL_DATA_SOURCE_URL_PROPERTY_NAME);
        final String windEstimationModelDataSourceAlwaysURL = System
                .getProperty(WIND_ESTIMATION_MODEL_DATA_SOURCE_ALWAYS_URL_PROPERTY_NAME);
        final String windEstimationModelDataSourceFolder = System
                .getProperty(WIND_ESTIMATION_MODEL_DATA_SOURCE_FOLDER_PROPERTY_NAME);
        final String windEstimationModelBearerToken = System
                .getProperty(WIND_ESTIMATION_MODEL_BEARER_TOKEN_PROPERTY_NAME);
        int loadFromCount = (windEstimationModelDataSourceURL != null ? 1 : 0) +
                (windEstimationModelDataSourceAlwaysURL != null ? 1 : 0) +
                (windEstimationModelDataSourceFolder != null ? 1 : 0);
        if (loadFromCount > 1) {
            throw new ModelLoadingException("At most one of the three parameters may be provided: \""
                    + WIND_ESTIMATION_MODEL_DATA_SOURCE_URL_PROPERTY_NAME + "\" or \""
                    + WIND_ESTIMATION_MODEL_DATA_SOURCE_ALWAYS_URL_PROPERTY_NAME + "\" or \""
                    + WIND_ESTIMATION_MODEL_DATA_SOURCE_FOLDER_PROPERTY_NAME + "\"");
        }
        if (windEstimationModelDataSourceURL != null && !service.isReady()) {
            importWindEstimationModelsFromUrl(windEstimationModelDataSourceURL, Optional.ofNullable(windEstimationModelBearerToken));
        } else if (windEstimationModelDataSourceAlwaysURL != null) {
            importWindEstimationModelsFromUrl(windEstimationModelDataSourceAlwaysURL, Optional.ofNullable(windEstimationModelBearerToken));
        } else if (windEstimationModelDataSourceFolder != null) {
            importWindEstimationModelsFromFolder(windEstimationModelDataSourceFolder);
        }
        final ServiceRegistration<WindEstimationFactoryService> serviceRegistration = context
                .registerService(WindEstimationFactoryService.class, service, null);
        registrations.add(serviceRegistration);
        final Dictionary<String, String> replicableServiceProperties = new Hashtable<>();
        replicableServiceProperties.put(Replicable.OSGi_Service_Registry_ID_Property_Name, service.getId().toString());
        registrations.add(context.registerService(Replicable.class.getName(), service, replicableServiceProperties));
        registrations.add(context.registerService(ClearStateTestSupport.class.getName(), service, null));
    }

    private void registerIncorporatedMongoDbCollectionsExclusively() throws AlreadyRegisteredException {
        MongoDBService mongoDBService = MongoDBService.INSTANCE;
        for (ModelDomainType domainType : ModelDomainType.values()) {
            String collectionName = MongoDbModelStoreImpl.getCollectionName(domainType);
            mongoDBService.registerExclusively(MongoDbModelStoreImpl.class, collectionName);
            mongoDBService.registerExclusively(MongoDbModelStoreImpl.class, collectionName + ".files");
            mongoDBService.registerExclusively(MongoDbModelStoreImpl.class, collectionName + ".chunks");
        }
    }

    private void importWindEstimationModelsFromFolder(String windEstimationModelDataSourceFolder) {
        FileSystemModelStoreImpl modelStore = new FileSystemModelStoreImpl(windEstimationModelDataSourceFolder);
        try {
            service.importAllModelsFromModelStore(modelStore);
        } catch (ModelPersistenceException e) {
            throw new ModelLoadingException(
                    "Could not import wind estimation models from folder :" + windEstimationModelDataSourceFolder, e);
        }
    }

    /**
     * @param windEstimationModelBearerToken
     *            if present, this bearer token will be used to authenticate the request for the wind estimation model
     *            data
     */
    private void importWindEstimationModelsFromUrl(String windEstimationModelDataSourceURL, Optional<String> windEstimationModelBearerToken) {
        logger.info("Importing wind estimation data from URL: " + windEstimationModelDataSourceURL);
        WindEstimationDataClient windEstimationDataClient = new WindEstimationDataClient(
                windEstimationModelDataSourceURL, service, windEstimationModelBearerToken);
        try {
            windEstimationDataClient.updateWindEstimationModels();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception while trying to import wind estimation model data from "
                    + windEstimationModelDataSourceURL, e);
        }
        logger.info("Import of wind estimation data finished successfully ");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        service.shutdown();
        logger.info("Unregistering WindEstimationFactoryService");
        for (ServiceRegistration<?> reg : registrations) {
            reg.unregister();
        }
        registrations.clear();
    }

}
