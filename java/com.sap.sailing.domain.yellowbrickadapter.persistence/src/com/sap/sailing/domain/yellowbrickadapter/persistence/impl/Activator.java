package com.sap.sailing.domain.yellowbrickadapter.persistence.impl;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoRaceLogStoreFactory;
import com.sap.sailing.domain.persistence.MongoRegattaLogStoreFactory;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParametersHandler;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickConfiguration;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickRaceTrackingConnectivityParams;
import com.sap.sailing.domain.yellowbrickadapter.persistence.PersistenceFactory;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.replication.FullyInitializedReplicableTracker;
import com.sap.sse.security.SecurityService;
import com.sap.sse.util.ClearStateTestSupport;
import com.sap.sse.util.ServiceTrackerFactory;

public class Activator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    private static BundleContext context;

    static BundleContext getContext() {
        return context;
    }

    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        for (CollectionNames name : CollectionNames.values()) {
            MongoDBService.INSTANCE.registerExclusively(CollectionNames.class, name.name());
        }
        new Thread(() -> {
            final ServiceTracker<MongoObjectFactory, MongoObjectFactory> mongoObjectFactoryServiceTracker = ServiceTrackerFactory
                    .createAndOpen(context, MongoObjectFactory.class);
            final ServiceTracker<DomainObjectFactory, DomainObjectFactory> domainObjectFactoryServiceTracker = ServiceTrackerFactory
                    .createAndOpen(context, DomainObjectFactory.class);
            final FullyInitializedReplicableTracker<SecurityService> securityServiceServiceTracker = FullyInitializedReplicableTracker.createAndOpen(context, SecurityService.class);
            try {
                final MongoObjectFactory mongoObjectFactory = mongoObjectFactoryServiceTracker.waitForService(0);
                final DomainObjectFactory domainObjectFactory = domainObjectFactoryServiceTracker.waitForService(0);
                final SecurityService securityService = securityServiceServiceTracker.getInitializedService(0);
                final DomainFactory baseDomainFactory = domainObjectFactory.getBaseDomainFactory();
                final Dictionary<String, Object> properties = new Hashtable<String, Object>();
                final com.sap.sailing.domain.yellowbrickadapter.persistence.DomainObjectFactory yellowBrickDomainObjectFactory = PersistenceFactory.INSTANCE
                        .createDomainObjectFactory(mongoObjectFactory.getDatabase());
                com.sap.sailing.domain.yellowbrickadapter.persistence.MongoObjectFactory yellowBrickMongoObjectFactory = com.sap.sailing.domain.yellowbrickadapter.persistence.PersistenceFactory.INSTANCE
                        .createMongoObjectFactory(mongoObjectFactory.getDatabase());
                final YellowBrickConnectivityParamsHandler paramsHandler = new YellowBrickConnectivityParamsHandler(
                        MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStore(mongoObjectFactory, domainObjectFactory),
                        MongoRegattaLogStoreFactory.INSTANCE.getMongoRegattaLogStore(mongoObjectFactory,
                                domainObjectFactory),
                        baseDomainFactory, yellowBrickMongoObjectFactory, securityService);
                for (YellowBrickConfiguration yellowBrickConfig : yellowBrickDomainObjectFactory.getYellowBrickConfigurations()) {
                    securityService.migrateOwnership(yellowBrickConfig);
                }
                // we do not necessarily have TracTrac configurations, so ensure that migration is marked as done
                securityService.assumeOwnershipMigrated(SecuredDomainType.YELLOWBRICK_ACCOUNT.getName());
                properties.put(TypeBasedServiceFinder.TYPE, YellowBrickRaceTrackingConnectivityParams.TYPE);
                context.registerService(RaceTrackingConnectivityParametersHandler.class, paramsHandler, properties);
                context.registerService(ClearStateTestSupport.class.getName(), new ClearStateTestSupport() {
                    @Override
                    public void clearState() throws Exception {
                        yellowBrickMongoObjectFactory.clear();
                    }
                }, null);
            } catch (Exception e) {
                logger.log(Level.SEVERE,
                        "Exception trying to register TracTrac RaceTrackingConnectivityParametersHandler implementation",
                        e);
            }
        }, getClass().getName() + " registering connectivity handler").start();
    }

    public void stop(BundleContext bundleContext) throws Exception {
        Activator.context = null;
    }
}
