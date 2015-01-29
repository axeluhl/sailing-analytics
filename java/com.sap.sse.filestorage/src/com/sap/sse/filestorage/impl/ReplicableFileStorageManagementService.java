package com.sap.sse.filestorage.impl;

import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.filestorage.common.FileStorageService;

/**
 * Defines internal operations that do are called only by
 * @author Fredrik Teschke
 *
 */
public interface ReplicableFileStorageManagementService {
    void internalSetFileStorageServiceProperty(String serviceName, String propertyName, String propertyValue)
            throws NoCorrespondingServiceRegisteredException, IllegalArgumentException;
    void internalSetActiveFileStorageService(FileStorageService service);
}
