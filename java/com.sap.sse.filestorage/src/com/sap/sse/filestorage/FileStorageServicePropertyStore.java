package com.sap.sse.filestorage;

import java.util.Map;

/**
 * Store that can save property values of {@link FileStorageService}s.
 * 
 * TODO mongo store, register it, change startup level of filestorage bundle, replication
 * 
 * @author Fredrik Teschke
 *
 */
public interface FileStorageServicePropertyStore {
    Map<String, String> readAllProperties(String serviceName);
    
    void writeProperty(String serviceName, String propertyName, String propertyValue);
}
