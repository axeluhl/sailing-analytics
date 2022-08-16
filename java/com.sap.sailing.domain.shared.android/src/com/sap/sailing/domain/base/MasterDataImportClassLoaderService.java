package com.sap.sailing.domain.base;

import com.sap.sse.shared.classloading.ClassLoaderSupplier;

/**
 * Bundles which {@code com.sap.sailing.server} has no dependency to can use this service to let the master data importer
 * retrieve its class loader so that deserialization works for their classes. They should register an instance
 * of an implementing class that lives in their bundle as follows:
 * 
 * <pre>
 *     context.registerService(MasterDataImportClassLoaderService.class, new MasterDataImportClassLoaderServiceImpl(), null)
 * </pre>
 * 
 * @author Frederik Petersen
 *
 */
public interface MasterDataImportClassLoaderService extends ClassLoaderSupplier {
}
