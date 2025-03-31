package com.sap.sse.filestorage.impl;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.filestorage.FileStorageManagementService;
import com.sap.sse.filestorage.FileStorageService;
import com.sap.sse.filestorage.FileStorageServicePropertyStore;
import com.sap.sse.mongodb.MongoDBConfiguration;
import com.sap.sse.osgi.CachedOsgiTypeBasedServiceFinderFactory;
import com.sap.sse.replication.Replicable;

public class Activator implements BundleActivator {
    private ServiceTracker<FileStorageService, FileStorageService> tracker;

    @Override
    public void start(BundleContext context) throws Exception {
        MongoDBConfiguration dbConfig = MongoDBConfiguration.getDefaultConfiguration();
        dbConfig.getService().registerExclusively(MongoFileStorageServicePropertyStoreImpl.class,
                MongoFileStorageServicePropertyStoreImpl.PROPERTIES_COLLECTION_NAME);
        dbConfig.getService().registerExclusively(MongoFileStorageServicePropertyStoreImpl.class,
                MongoFileStorageServicePropertyStoreImpl.ACTIVE_SERVICE_COLLECTION_NAME);

        FileStorageServicePropertyStore propertyStore = new MongoFileStorageServicePropertyStoreImpl(
                dbConfig.getService());

        Dictionary<String, String> dict = new Hashtable<>();
        dict.put(TypeBasedServiceFinder.TYPE, AmazonS3FileStorageServiceImpl.NAME);
        context.registerService(FileStorageService.class, new AmazonS3FileStorageServiceImpl(context), dict);
        
        Dictionary<String, String> localStorageDict = new Hashtable<>();
        localStorageDict.put(TypeBasedServiceFinder.TYPE, LocalFileStorageServiceImpl.NAME);
        context.registerService(FileStorageService.class, new LocalFileStorageServiceImpl(context), localStorageDict);

        // register mgmt service
        FileStorageManagementServiceImpl mgmtService = new FileStorageManagementServiceImpl(
                new CachedOsgiTypeBasedServiceFinderFactory(context).createServiceFinder(FileStorageService.class),
                propertyStore);
        context.registerService(FileStorageManagementService.class, mgmtService, null);

        // register mgmt service as replicable
        Dictionary<String, String> replicableServiceProperties = new Hashtable<>();
        replicableServiceProperties.put(Replicable.OSGi_Service_Registry_ID_Property_Name, mgmtService.getId()
                .toString());
        context.registerService(Replicable.class.getName(), mgmtService, replicableServiceProperties);

        // track all FileStorageServices, so that their properties can be set from the database when added to the OSGi
        // registry
        tracker = new ServiceTracker<>(context, FileStorageService.class,
                new ServiceAddedListenerWrappingCustomizer<FileStorageService>(context, mgmtService));
        tracker.open();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (tracker != null) {
            tracker.close();
        }
    }

}
