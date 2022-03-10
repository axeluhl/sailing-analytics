package com.sap.sse.replication.interfaces.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.sap.sse.MasterDataImportClassLoaderService;
import com.sap.sse.replication.InitialLoadClassLoaderRegistry;

public class ServiceTrackerForInitialLoadClassLoaderRegistration
        implements ServiceTrackerCustomizer<MasterDataImportClassLoaderService, MasterDataImportClassLoaderService> {
    private final BundleContext context;
    private InitialLoadClassLoaderRegistry replicable;

    public ServiceTrackerForInitialLoadClassLoaderRegistration(BundleContext context,
            InitialLoadClassLoaderRegistry racingEventService) {
        this.context = context;
        this.replicable = racingEventService;
    }

    @Override
    public MasterDataImportClassLoaderService addingService(
            ServiceReference<MasterDataImportClassLoaderService> reference) {
        MasterDataImportClassLoaderService service = context.getService(reference);
        replicable.addMasterDataClassLoader(service.getClassLoader());
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<MasterDataImportClassLoaderService> reference,
            MasterDataImportClassLoaderService service) {
    }

    @Override
    public void removedService(ServiceReference<MasterDataImportClassLoaderService> reference,
            MasterDataImportClassLoaderService service) {
        replicable.removeMasterDataClassLoader(service.getClassLoader());
    }
}
