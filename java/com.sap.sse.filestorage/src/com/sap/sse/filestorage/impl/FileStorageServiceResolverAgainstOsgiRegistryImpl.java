package com.sap.sse.filestorage.impl;

import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.filestorage.FileStorageService;
import com.sap.sse.filestorage.FileStorageServiceResolver;

public class FileStorageServiceResolverAgainstOsgiRegistryImpl implements FileStorageServiceResolver {
    private final TypeBasedServiceFinder<FileStorageService> serviceFinder;

    public FileStorageServiceResolverAgainstOsgiRegistryImpl(TypeBasedServiceFinder<FileStorageService> serviceFinder) {
        this.serviceFinder = serviceFinder;
    }

    @Override
    public FileStorageService getFileStorageService(String serviceName) {
        try {
            return serviceFinder.findService(serviceName);
        } catch (NoCorrespondingServiceRegisteredException e) {
            return null;
        }
    }

}
