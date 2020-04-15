package com.sap.sse.filestorage;

import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.filestorage.impl.ReplicableFileStorageManagementService;
import com.sap.sse.filestorage.operations.FileStorageServiceOperation;
import com.sap.sse.replication.ReplicableWithObjectInputStream;

/**
 * OSGi service for managing {@link FileStorageService FileStorageServices}.
 * 
 * @author Fredrik Teschke
 *
 */
public interface FileStorageManagementService extends
        ReplicableWithObjectInputStream<ReplicableFileStorageManagementService, FileStorageServiceOperation<?>> {
    FileStorageService[] getAvailableFileStorageServices();

    /**
     * @return {@code null} if no service for that name is found
     */
    FileStorageService getFileStorageService(String name);

    /**
     * Sets the property of the service, and also stores the property value so that it can be restored after a server
     * restart.
     * @throws IllegalArgumentException
     *             if the property with name {@code propertyName} doesn't exist for the service
     */
    void setFileStorageServiceProperty(FileStorageService service, String propertyName, String propertyValue)
            throws NoCorrespondingServiceRegisteredException, IllegalArgumentException;

    /**
     * Get the currently configured {@link FileStorageService}.
     * 
     * @throws NoCorrespondingServiceRegisteredException
     *             if no service has been activated
     */
    FileStorageService getActiveFileStorageService() throws NoCorrespondingServiceRegisteredException;

    void setActiveFileStorageService(FileStorageService service);
}
