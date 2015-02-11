package com.sap.sse.filestorage.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.filestorage.FileStorageServicePropertyStore;

public enum EmptyFileStorageServicePropertyStoreImpl implements FileStorageServicePropertyStore {
    INSTANCE;

    @Override
    public Map<String, String> readAllProperties(String serviceName) {
        return new HashMap<String, String>();
    }

    @Override
    public void writeProperty(String serviceName, String propertyName, String propertyValue) {

    }

    @Override
    public String readActiveServiceName() {
        return null;
    }

    @Override
    public void writeActiveService(String serviceName) {
    }

}
