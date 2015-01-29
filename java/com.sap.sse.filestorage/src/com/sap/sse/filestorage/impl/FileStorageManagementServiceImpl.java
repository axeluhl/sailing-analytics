package com.sap.sse.filestorage.impl;

import java.util.Map.Entry;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.filestorage.FileStorageManagementService;
import com.sap.sse.filestorage.FileStorageService;
import com.sap.sse.filestorage.FileStorageServicePropertyStore;
import com.sap.sse.osgi.CachedOsgiTypeBasedServiceFinderFactory;

/**
 * Implements {@link ServiceTrackerCustomizer} so that all {@link FileStorageServices} announced in the
 * registry can receive their stored properties.
 * 
 * TODO implements Replicable, build Operations
 *      add fully qualified classname of Replicable to java/target/env.sh
 *      register as Replicable OSGi service (com.sap.sse.security.impl.Activator#83)
 * TODO store active selection
 * 
 * @author Fredrik Teschke
 *
 */
public class FileStorageManagementServiceImpl implements FileStorageManagementService,
    ServiceTrackerCustomizer<FileStorageService, FileStorageService> {
    private final Logger logger = Logger.getLogger(FileStorageManagementServiceImpl.class.getName());
    
    private FileStorageService active;

    private final TypeBasedServiceFinder<FileStorageService> serviceFinder;
    private final FileStorageServicePropertyStore propertyStore;
    private final BundleContext context;
    
    public FileStorageManagementServiceImpl(BundleContext context, FileStorageServicePropertyStore propertyStore) {
        this.context = context;
        serviceFinder = new CachedOsgiTypeBasedServiceFinderFactory(context)
                .createServiceFinder(FileStorageService.class);
        this.propertyStore = propertyStore;
        
        context.addServiceListener(new ServiceListener() {
            
            @Override
            public void serviceChanged(ServiceEvent event) {
                // TODO Auto-generated method stub
                
            }
        });
    }

    @Override
    public FileStorageService getActiveFileStorageService() {
        if (active == null) {
            throw new NoCorrespondingServiceRegisteredException();
        }
        return active;
    }

    @Override
    public void setActiveFileStorageService(FileStorageService service) {
        active = service;
    }

    @Override
    public FileStorageService[] getAvailableFileStorageServices() {
        return serviceFinder.findAllServices().toArray(new FileStorageService[0]);
    }

    @Override
    public FileStorageService getFileStorageService(String name) {
        return serviceFinder.findService(name);
    }

    @Override
    public void setFileStorageServiceProperty(String serviceName, String propertyName, String propertyValue)
            throws NoCorrespondingServiceRegisteredException, IllegalArgumentException {
        //TODO replicate
        propertyStore.writeProperty(serviceName, propertyName, propertyValue);
        serviceFinder.findService(serviceName).internalSetProperty(propertyName, propertyValue);
    }

    @Override
    public FileStorageService addingService(ServiceReference<FileStorageService> reference) {
        FileStorageService service = context.getService(reference);
        logger.info("Found new FileStorageService: adding properties to " + service.getName());
        for (Entry<String, String> property : propertyStore.readAllProperties(service.getName()).entrySet()) {
            service.internalSetProperty(property.getKey(), property.getValue());
        }
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<FileStorageService> reference, FileStorageService service) {
        //ignore
    }

    @Override
    public void removedService(ServiceReference<FileStorageService> reference, FileStorageService service) {
        //ignore
    }
}
