package com.sap.sse.filestorage.impl;

import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.filestorage.FileStorageManagementService;
import com.sap.sse.filestorage.common.FileStorageService;

/**
 * Publishes those methods of {@link FileStorageManagementService} that are required by operations implemented as lambda
 * expressions to fulfill their tasks. These operations should not be invoked by external service clients.
 * {@link FileStorageManagementService} is the one registered with the OSGi registry and thus the publicly-visible
 * interface (description copied from ReplicableSecurityService).
 * 
 * @author Fredrik Teschke
 *
 */
public interface ReplicableFileStorageManagementService {
    void internalSetFileStorageServiceProperty(String serviceName, String propertyName, String propertyValue)
            throws NoCorrespondingServiceRegisteredException, IllegalArgumentException;

    void internalSetActiveFileStorageService(FileStorageService service);
}
