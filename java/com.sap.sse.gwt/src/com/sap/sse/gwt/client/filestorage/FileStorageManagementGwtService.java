package com.sap.sse.gwt.client.filestorage;

import java.io.IOException;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.gwt.shared.filestorage.FileStorageServiceDTO;
import com.sap.sse.gwt.shared.filestorage.FileStorageServicePropertyErrorsDTO;

/**
 * GWT wrapper for the {@code FileStorageManagementService}. The only implementation currently
 * exists in {@code SailingServiceImpl}, ideally this would be pulled out into a separate RPC end point.
 * @author Fredrik Teschke
 *
 */
public interface FileStorageManagementGwtService extends RemoteService {
    
    FileStorageServiceDTO[] getAvailableFileStorageServices(String localeInfoName);

    /**
     * If serviceName is null, calls {@link FileStorageManagementGwtService#getActiveFileStorageServiceName()} and uses the returned name
     * 
     * @throws NoCorrespondingServiceRegisteredException service may have disappeared from registry in the meantime
     */
    FileStorageServicePropertyErrorsDTO testFileStorageServiceProperties(String serviceName, String localeInfoName) throws NoCorrespondingServiceRegisteredException, IOException;

    /**
     * @throws NoCorrespondingServiceRegisteredException service may have disappeared from registry in the meantime
     */
    void setActiveFileStorageService(String serviceName, String localeInfoName) throws NoCorrespondingServiceRegisteredException;

    /**
     * @return may be {@code null}
     */
    String getActiveFileStorageServiceName();
    
    /**
     * @throws NoCorrespondingServiceRegisteredException service may have disappeared from registry in the meantime
     * @throws IllegalArgumentException if the property with name {@code propertyName} doesn't exist for the service
     */
    void setFileStorageServiceProperties(String serviceName, Map<String, String> properties);
}
