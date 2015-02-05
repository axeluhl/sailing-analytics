package com.sap.sse.gwt.client.filestorage;

import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.gwt.shared.filestorage.FileStorageServiceDTO;
import com.sap.sse.gwt.shared.filestorage.FileStorageServicePropertyErrorsDTO;

public interface FileStorageManagementGwtServiceAsync {

    void getActiveFileStorageServiceName(AsyncCallback<String> callback);

    void getAvailableFileStorageServices(AsyncCallback<FileStorageServiceDTO[]> callback);

    void setActiveFileStorageService(String serviceName, AsyncCallback<Void> callback);

    void setFileStorageServiceProperties(String serviceName, Map<String, String> properties,
            AsyncCallback<Void> callback);

    void testFileStorageServiceProperties(String serviceName,
            AsyncCallback<FileStorageServicePropertyErrorsDTO> callback);

}
