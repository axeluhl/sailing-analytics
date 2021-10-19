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
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickConfigurationListener;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickRaceTrackingConnectivityParams;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickTrackingAdapter;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickTrackingAdapterFactory;
import com.sap.sailing.domain.yellowbrickadapter.persistence.PersistenceFactory;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.replication.FullyInitializedReplicableTracker;
import com.sap.sse.security.SecurityService;
import com.sap.sse.util.ClearStateTestSupport;
import com.sap.sse.util.ServiceTrackerFactory;

/**
 * When the activator starts, it loads the stored {@link YellowBrickConfiguration} objects from the database and passes
 * them to the {@link YellowBrickTrackingAdapterFactory} which is looked up in the OSGi service registry. Then, it adds
 * a listener to the {@link YellowBrickTrackingAdapterFactory} for new and removed connections which will then be mapped
 * to the corresponding calls on the {@link com.sap.sailing.domain.yellowbrickadapter.persistence.MongoObjectFactory}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
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
            final ServiceTracker<YellowBrickTrackingAdapterFactory, YellowBrickTrackingAdapterFactory> yellowBrickTrackingAdapterFactoryServiceTracker = ServiceTrackerFactory
                    .createAndOpen(context, YellowBrickTrackingAdapterFactory.class);
            final FullyInitializedReplicableTracker<SecurityService> securityServiceServiceTracker = FullyInitializedReplicableTracker.createAndOpen(context, SecurityService.class);
            try {
                final MongoObjectFactory mongoObjectFactory = mongoObjectFactoryServiceTracker.waitForService(0);
                final DomainObjectFactory domainObjectFactory = domainObjectFactoryServiceTracker.waitForService(0);
                final SecurityService securityService = securityServiceServiceTracker.getInitializedService(0);
                final DomainFactory baseDomainFactory = domainObjectFactory.getBaseDomainFactory();
                final YellowBrickTrackingAdapterFactory yellowBrickTrackingAdapterFactory = yellowBrickTrackingAdapterFactoryServiceTracker.waitForService(0);
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
                final YellowBrickTrackingAdapter yellowBrickTrackingAdapter = yellowBrickTrackingAdapterFactory.getYellowBrickTrackingAdapter(baseDomainFactory);
                for (YellowBrickConfiguration yellowBrickConfig : yellowBrickDomainObjectFactory.getYellowBrickConfigurations()) {
                    securityService.migrateOwnership(yellowBrickConfig);
                    yellowBrickTrackingAdapter.addYellowBrickConfiguration(yellowBrickConfig);
                }
                yellowBrickTrackingAdapter.addYellowBrickConfigurationListener(new YellowBrickConfigurationListener() {
                    @Override
                    public void yellowBrickConfigurationAdded(YellowBrickConfiguration configAdded) {
                        yellowBrickMongoObjectFactory.createYellowBrickConfiguration(configAdded);
                    }

                    @Override
                    public void yellowBrickConfigurationRemoved(YellowBrickConfiguration configRemoved) {
                        yellowBrickMongoObjectFactory.deleteYellowBrickConfiguration(configRemoved.getCreatorName(), configRemoved.getRaceUrl());
                    }

                    @Override
                    public void yellowBrickConfigurationUpdated(YellowBrickConfiguration configUpdated) {
                        yellowBrickMongoObjectFactory.updateYellowBrickConfiguration(configUpdated);
                    }
                });
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
