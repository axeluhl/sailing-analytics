package com.sap.sailing.domain.persistence.impl;

import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.sap.sailing.mongodb.MongoDBService;

public class Activator implements BundleActivator {
    private Set<ServiceRegistration<?>> registrations = new HashSet<>();
    @Override
    public void start(BundleContext context) throws Exception {
        for (CollectionNames name : CollectionNames.values())
            MongoDBService.INSTANCE.registerExclusively(CollectionNames.class, name.name());
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        for (ServiceRegistration<?> reg : registrations) {
            reg.unregister();
        }
        registrations.clear();
    }

}
