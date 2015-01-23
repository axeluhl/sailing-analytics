package com.sap.sse.filestorage.ui.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sse.filestorage.InvalidPropertiesException;
import com.sap.sse.filestorage.ui.shared.FileStorageServiceDTO;


/**
 * For use with GWT for managing the file storage service to use.
 * @author Fredrik Teschke
 *
 */
public interface FileStorageManagementService extends RemoteService {
    FileStorageServiceDTO getAvailableFileStorageServices();
    void setFileStorageServiceProperty(String serviceName, String propertyName, String propertyValue);
    void testFileStorageServiceProperties(String serviceName) throws InvalidPropertiesException;
    void setFileStorageServiceToUse(String serviceName) throws InvalidPropertiesException;
}
