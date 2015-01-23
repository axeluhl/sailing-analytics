package com.sap.sse.filestorage;

import com.sap.sse.common.NoCorrespondingServiceRegisteredException;

/**
 * OSGi service for managing {@link FileStorageService FileStorageServices}.
 * 
 * @author Fredrik Teschke
 *
 */
public interface FileStorageManagementService {
    FileStorageService[] getAvailableFileStorageServices();

    FileStorageService getFileStorageService(String name);

    /**
     * Sets the property of the service, and also stores the property value so that it can be restored after a server
     * restart.
     * 
     * @throws NoCorrespondingServiceRegisteredException
     *             service may have disappeared from registry in the meantime
     * @throws IllegalArgumentException
     *             if the property with name {@code propertyName} doesn't exist for the service
     */
    void setFileStorageServiceProperty(String serviceName, String propertyName, String propertyValue)
            throws NoCorrespondingServiceRegisteredException, IllegalArgumentException;

    /**
     * @throws NoCorrespondingServiceRegisteredException
     *             if no service has been selected so far.
     */
    FileStorageService getActiveFileStorageService() throws NoCorrespondingServiceRegisteredException;

    /**
     * @throws InvalidPropertiesException
     *             if the service properties are {@link FileStorageService#testProperties() invalid}. Then the old
     *             active service (if any) remains active.
     */
    void setActiveFileStorageService(FileStorageService service) throws InvalidPropertiesException;
}
