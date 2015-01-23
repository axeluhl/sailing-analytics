package com.sap.sse.filestorage.ui.server;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sse.filestorage.FileStorageManagementService;
import com.sap.sse.filestorage.FileStorageService;
import com.sap.sse.filestorage.InvalidPropertiesException;
import com.sap.sse.filestorage.ui.client.FileStorageRpcManagementService;
import com.sap.sse.filestorage.ui.shared.FileStorageServiceDTO;

public class FileStorageRpcManagementServiceImpl implements FileStorageRpcManagementService {
    private final ServiceTracker<FileStorageManagementService, FileStorageManagementService> managementServiceTracker;

    public FileStorageRpcManagementServiceImpl() {
        BundleContext context = Activator.getContext();
        managementServiceTracker = new ServiceTracker<>(context, FileStorageManagementService.class.getName(), null);
        managementServiceTracker.open();
    }
    
    private FileStorageManagementService getManagementService() {
        return managementServiceTracker.getService();
    }

    @Override
    public FileStorageServiceDTO[] getAvailableFileStorageServices() {
        List<FileStorageServiceDTO> serviceDtos = new ArrayList<>();
        for (FileStorageService s : getManagementService().getAvailableFileStorageServices()) {
            serviceDtos.add(FileStorageServiceDTO.convert(s));
        }
        return serviceDtos.toArray(new FileStorageServiceDTO[0]);
    }

    @Override
    public void setFileStorageServiceProperty(String serviceName, String propertyName, String propertyValue) {
        getManagementService().setFileStorageServiceProperty(serviceName, propertyName, propertyValue);
    }

    @Override
    public void testFileStorageServiceProperties(String serviceName) throws InvalidPropertiesException {
        getManagementService().getFileStorageService(serviceName).testProperties();
    }

    @Override
    public void setActiveFileStorageService(String serviceName) throws InvalidPropertiesException {
        getManagementService().setActiveFileStorageService(getManagementService().getFileStorageService(serviceName));
    }

    @Override
    public String getActiveFileStorageServiceName() {
        return getManagementService().getActiveFileStorageService().getName();
    }
}
