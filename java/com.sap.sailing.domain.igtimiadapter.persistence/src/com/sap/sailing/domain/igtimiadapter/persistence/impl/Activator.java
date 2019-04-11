package com.sap.sailing.domain.igtimiadapter.persistence.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.igtimiadapter.persistence.PersistenceFactory;
import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.util.ClearStateTestSupport;

public class Activator implements BundleActivator {
    @Override
    public void start(BundleContext context) throws Exception {
        for (CollectionNames name : CollectionNames.values()) {
            MongoDBService.INSTANCE.registerExclusively(CollectionNames.class, name.name());
        }
        context.registerService(ClearStateTestSupport.class.getName(), new ClearStateTestSupport() {
            @Override
            public void clearState() throws Exception {
                PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory().clear();
            }
        }, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
