package com.sap.sailing.shared.server.impl;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.sap.sailing.domain.sharedsailingdata.SharedSailingData;
import com.sap.sailing.shared.persistence.PersistenceFactory;
import com.sap.sailing.shared.server.ReplicatingSharedSailingData;
import com.sap.sse.osgi.CachedOsgiTypeBasedServiceFinderFactory;
import com.sap.sse.replication.FullyInitializedReplicableTracker;
import com.sap.sse.replication.Replicable;
import com.sap.sse.replication.ReplicationService;
import com.sap.sse.security.SecurityService;
import com.sap.sse.util.ClearStateTestSupport;
import com.sap.sse.util.ServiceTrackerFactory;

public class Activator implements BundleActivator {

    private static BundleContext context;

    private CachedOsgiTypeBasedServiceFinderFactory serviceFinderFactory;

    private Set<ServiceRegistration<?>> registrations = new HashSet<>();

    private FullyInitializedReplicableTracker<SecurityService> securityServiceTracker;

    private SharedSailingDataImpl sharedSailingData;

    public void start(BundleContext context) throws Exception {
        Activator.context = context;
        securityServiceTracker = new FullyInitializedReplicableTracker<>(context, SecurityService.class,
                /* customizer */ null, ServiceTrackerFactory.createAndOpen(context, ReplicationService.class));
        securityServiceTracker.open();
        serviceFinderFactory = new CachedOsgiTypeBasedServiceFinderFactory(context);
        sharedSailingData = new SharedSailingDataImpl(PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory(serviceFinderFactory),
                PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory(serviceFinderFactory), serviceFinderFactory,
                securityServiceTracker);
        registrations.add(context.registerService(SharedSailingData.class, sharedSailingData, /* properties */ null));
        registrations.add(context.registerService(ReplicatingSharedSailingData.class, sharedSailingData, /* properties */ null));
        registrations
                .add(context.registerService(ClearStateTestSupport.class, sharedSailingData, /* properties */ null));
        final Dictionary<String, String> replicableServiceProperties = new Hashtable<>();
        replicableServiceProperties.put(Replicable.OSGi_Service_Registry_ID_Property_Name,
                sharedSailingData.getId().toString());
        registrations.add(context.registerService(Replicable.class, sharedSailingData, /* properties */ replicableServiceProperties));
    }

    public static BundleContext getContext() {
        return context;
    }

    public void stop(BundleContext context) throws Exception {
        if (serviceFinderFactory != null) {
            serviceFinderFactory.close();
            serviceFinderFactory = null;
        }
        for (ServiceRegistration<?> reg : registrations) {
            reg.unregister();
        }
        registrations.clear();
        sharedSailingData = null;
        securityServiceTracker.close();
        securityServiceTracker = null;
    }
}
