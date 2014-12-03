package com.sap.sse.security.userstore.mongodb.impl;

import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.userstore.mongodb.UserStoreImpl;

public class Activator implements BundleActivator {

    private static BundleContext context;
    private ServiceRegistration<?> registration;

    static BundleContext getContext() {
        return context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        registration = context.registerService(UserStore.class.getName(),
                new UserStoreImpl(), null);
        Logger.getLogger(Activator.class.getName()).info("User store registered.");
        for (CollectionNames name : CollectionNames.values()) {
            MongoDBService.INSTANCE.registerExclusively(CollectionNames.class, name.name());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bundleContext) throws Exception {
        registration.unregister();
        Activator.context = null;
    }

}
