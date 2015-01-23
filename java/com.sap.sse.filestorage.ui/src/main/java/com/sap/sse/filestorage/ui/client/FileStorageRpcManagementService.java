package com.sap.sse.filestorage.ui.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.filestorage.InvalidPropertiesException;
import com.sap.sse.filestorage.ui.shared.FileStorageServiceDTO;

/**
 * For use with GWT for managing the file storage service to use.
 * 
 * @author Fredrik Teschke
 *
 */
public interface FileStorageRpcManagementService extends RemoteService {
    FileStorageServiceDTO[] getAvailableFileStorageServices();

    /**
     * @throws NoCorrespondingServiceRegisteredException service may have disappeared from registry in the meantime
     * @throws IllegalArgumentException if the property with name {@code propertyName} doesn't exist for the service
     */
    void setFileStorageServiceProperty(String serviceName, String propertyName, String propertyValue)
            throws NoCorrespondingServiceRegisteredException, IllegalArgumentException;

    /**
     * @throws NoCorrespondingServiceRegisteredException service may have disappeared from registry in the meantime
     * @throws InvalidPropertiesException is thrown if properties are not valid
     */
    void testFileStorageServiceProperties(String serviceName) throws NoCorrespondingServiceRegisteredException,
            InvalidPropertiesException;

    /**
     * @throws NoCorrespondingServiceRegisteredException service may have disappeared from registry in the meantime
     * @throws InvalidPropertiesException only a service with valid properties can be selected for usage
     */
    void setActiveFileStorageService(String serviceName) throws NoCorrespondingServiceRegisteredException,
            InvalidPropertiesException;

    /**
     * @return may be {@code null}
     */
    String getActiveFileStorageServiceName();
}
