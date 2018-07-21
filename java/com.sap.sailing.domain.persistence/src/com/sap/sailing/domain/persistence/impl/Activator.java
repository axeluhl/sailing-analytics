package com.sap.sailing.domain.persistence.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sse.mongodb.MongoDBService;

public class Activator implements BundleActivator {
    @Override
    public void start(BundleContext context) throws Exception {
        for (CollectionNames name : CollectionNames.values()) {
            MongoDBService.INSTANCE.registerExclusively(CollectionNames.class, name.name());
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
