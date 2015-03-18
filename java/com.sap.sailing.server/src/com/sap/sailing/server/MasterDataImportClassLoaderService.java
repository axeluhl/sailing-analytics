package com.sap.sailing.server;

/**
 * Bundles which com.sap.sailing.server has no dependency to can use this service to let the master data importer
 * retrieve its class loader so that deserialization works for their classes.
 * 
 * @author Frederik Petersen
 *
 */
public interface MasterDataImportClassLoaderService {
    
    ClassLoader getClassLoader();

}
