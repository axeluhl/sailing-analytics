package com.sap.sailing.shared.server.impl;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkRole;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.shared.persistence.PersistenceFactory;
import com.sap.sailing.shared.server.SharedSailingData;
import com.sap.sse.osgi.CachedOsgiTypeBasedServiceFinderFactory;
import com.sap.sse.replication.FullyInitializedReplicableTracker;
import com.sap.sse.replication.Replicable;
import com.sap.sse.security.SecurityService;
import com.sap.sse.util.ClearStateTestSupport;

public class Activator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());

    private static BundleContext context;

    private CachedOsgiTypeBasedServiceFinderFactory serviceFinderFactory;

    private Set<ServiceRegistration<?>> registrations = new HashSet<>();

    private FullyInitializedReplicableTracker<SecurityService> securityServiceTracker;

    private SharedSailingDataImpl sharedSailingData;

    public void start(BundleContext context) throws Exception {
        Activator.context = context;
        securityServiceTracker = FullyInitializedReplicableTracker.createAndOpen(context, SecurityService.class);
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
        new Thread(()->migrateOwnerships()).start();
    }

    private void migrateOwnerships() {
        try {
            final SecurityService securityService = securityServiceTracker.getInitializedService(0);
            for (final CourseTemplate courseTemplate : sharedSailingData.getAllCourseTemplates()) {
                securityService.migrateOwnership(courseTemplate);
            }
            securityService.assumeOwnershipMigrated(SecuredDomainType.COURSE_TEMPLATE.getName());
            for (final MarkProperties markProperties : sharedSailingData.getAllMarkProperties()) {
                securityService.migrateOwnership(markProperties);
            }
            securityService.assumeOwnershipMigrated(SecuredDomainType.MARK_PROPERTIES.getName());
            for (final MarkRole markRole : sharedSailingData.getAllMarkRoles()) {
                securityService.migrateOwnership(markRole);
            }
            securityService.assumeOwnershipMigrated(SecuredDomainType.MARK_ROLE.getName());
            for (final MarkTemplate markTemplate : sharedSailingData.getAllMarkTemplates()) {
                securityService.migrateOwnership(markTemplate);
            }
            securityService.assumeOwnershipMigrated(SecuredDomainType.MARK_TEMPLATE.getName());
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Interrupted while trying to migrate ownership of shared sailing objects", e);
        }
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
