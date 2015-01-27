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

public class Activator implements BundleActivator {
    private ServiceTracker<FileStorageService, FileStorageService> tracker;

    @Override
    public void start(BundleContext context) throws Exception {
        MongoDBConfiguration dbConfig = MongoDBConfiguration.getDefaultConfiguration();
        dbConfig.getService().registerExclusively(MongoFileStorageServicePropertyStoreImpl.class,
                MongoFileStorageServicePropertyStoreImpl.COLLECTION_NAME);

        FileStorageServicePropertyStore propertyStore = new MongoFileStorageServicePropertyStoreImpl(
                dbConfig.getService());

        Dictionary<String, String> dict = new Hashtable<>();
        dict.put(TypeBasedServiceFinder.TYPE, AmazonS3FileStorageServiceImpl.NAME);
        context.registerService(FileStorageService.class, new AmazonS3FileStorageServiceImpl(), dict);

        FileStorageManagementServiceImpl mgmtService = new FileStorageManagementServiceImpl(context, propertyStore);
        context.registerService(FileStorageManagementService.class, mgmtService, null);

        // track all FileStorageServices, so that their properties can be set from the database when added to the OSGi
        // registry
        tracker = new ServiceTracker<>(context, FileStorageService.class, mgmtService);
        tracker.open();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (tracker != null) {
            tracker.close();
        }
    }

}
