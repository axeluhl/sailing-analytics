package com.sap.sse.filestorage.impl;

import org.osgi.framework.BundleContext;

import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.filestorage.FileStorageManagementService;
import com.sap.sse.filestorage.FileStorageService;
import com.sap.sse.filestorage.InvalidPropertiesException;
import com.sap.sse.osgi.CachedOsgiTypeBasedServiceFinderFactory;

public class TransientFileStorageManagementServiceImpl implements FileStorageManagementService {
    private FileStorageService active;

    private TypeBasedServiceFinder<FileStorageService> serviceFinder;

    public void setContext(BundleContext context) {
        serviceFinder = new CachedOsgiTypeBasedServiceFinderFactory(context)
                .createServiceFinder(FileStorageService.class);
    }

    @Override
    public FileStorageService getActiveFileStorageService() {
        return active;
    }

    @Override
    public void setActiveFileStorageService(FileStorageService service) throws InvalidPropertiesException {
        active = service;
    }

    @Override
    public FileStorageService[] getAvailableFileStorageServices() {
        return serviceFinder.findAllServices().toArray(new FileStorageService[0]);
    }

    @Override
    public FileStorageService getFileStorageService(String name) {
        return serviceFinder.findService(name);
    }

    @Override
    public void setFileStorageServiceProperty(String serviceName, String propertyName, String propertyValue)
            throws NoCorrespondingServiceRegisteredException, IllegalArgumentException {
        //TODO mgmt service that persists properties on master
        serviceFinder.findService(serviceName).setProperty(propertyName, propertyValue);
    }
}
