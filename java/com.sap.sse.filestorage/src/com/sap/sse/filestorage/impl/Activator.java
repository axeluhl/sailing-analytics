package com.sap.sse.filestorage.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sse.filestorage.FileStorageService;

public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        context.registerService(FileStorageService.class, new AmazonS3FileStorageServiceImpl(), null);        
    }

    @Override
    public void stop(BundleContext context) throws Exception {

    }

}
