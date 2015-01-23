package com.sap.sse.filestorage.impl;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.filestorage.FileStorageManagementService;
import com.sap.sse.filestorage.FileStorageService;

public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        Dictionary<String, String> dict = new Hashtable<>();
        dict.put(TypeBasedServiceFinder.TYPE, AmazonS3FileStorageServiceImpl.NAME);
        context.registerService(FileStorageService.class, new AmazonS3FileStorageServiceImpl(), dict);

        TransientFileStorageManagementServiceImpl mgmtService = new TransientFileStorageManagementServiceImpl();
        context.registerService(FileStorageManagementService.class, mgmtService,
                null);
        mgmtService.setContext(context);
        

        //TODO load service properties from DB on startup
    }

    @Override
    public void stop(BundleContext context) throws Exception {

    }

}
