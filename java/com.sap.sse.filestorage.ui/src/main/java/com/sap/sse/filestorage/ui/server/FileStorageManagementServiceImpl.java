package com.sap.sse.filestorage.ui.server;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.sap.sse.filestorage.FileStorageService;
import com.sap.sse.filestorage.InvalidPropertiesException;
import com.sap.sse.filestorage.ui.client.FileStorageManagementService;
import com.sap.sse.filestorage.ui.shared.FileStorageServiceDTO;

public class FileStorageManagementServiceImpl implements FileStorageManagementService {
    private final BundleContext context;

    public FileStorageManagementServiceImpl() {
        this.context = Activator.getContext();
    }

    @Override
    public FileStorageServiceDTO[] getAvailableFileStorageServices() throws InvalidSyntaxException {
        List<FileStorageServiceDTO> serviceDtos = new ArrayList<>();
        for (ServiceReference<FileStorageService> ref : context.getServiceReferences(FileStorageService.class, null)) {
            FileStorageService service = context.getService(ref);
            serviceDtos.add(FileStorageServiceDTO.convert(service));
            context.ungetService(ref);
        }
        return serviceDtos.toArray(new FileStorageServiceDTO[0]);
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
