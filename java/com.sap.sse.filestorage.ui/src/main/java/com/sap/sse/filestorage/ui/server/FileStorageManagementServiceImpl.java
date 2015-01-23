package com.sap.sse.filestorage.ui.server;

import com.sap.sse.filestorage.InvalidPropertiesException;
import com.sap.sse.filestorage.ui.client.FileStorageManagementService;
import com.sap.sse.filestorage.ui.shared.FileStorageServiceDTO;

public class FileStorageManagementServiceImpl implements FileStorageManagementService {

    @Override
    public FileStorageServiceDTO getAvailableFileStorageServices() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setFileStorageServiceProperty(String serviceName, String propertyName, String propertyValue) {
        // TODO Auto-generated method stub

    }

    @Override
    public void testFileStorageServiceProperties(String serviceName) throws InvalidPropertiesException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setFileStorageServiceToUse(String serviceName) throws InvalidPropertiesException {
        // TODO Auto-generated method stub

    }

}
