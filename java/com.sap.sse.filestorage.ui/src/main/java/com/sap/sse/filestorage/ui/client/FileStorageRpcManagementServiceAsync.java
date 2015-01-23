package com.sap.sse.filestorage.ui.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.filestorage.ui.shared.FileStorageServiceDTO;

public interface FileStorageRpcManagementServiceAsync {

    void getAvailableFileStorageServices(AsyncCallback<FileStorageServiceDTO[]> callback);

    void setFileStorageServiceProperty(String serviceName, String propertyName, String propertyValue,
            AsyncCallback<Void> callback);

    void setActiveFileStorageService(String serviceName, AsyncCallback<Void> callback);

    void testFileStorageServiceProperties(String serviceName, AsyncCallback<Void> callback);

    void getActiveFileStorageServiceName(AsyncCallback<String> callback);

}
