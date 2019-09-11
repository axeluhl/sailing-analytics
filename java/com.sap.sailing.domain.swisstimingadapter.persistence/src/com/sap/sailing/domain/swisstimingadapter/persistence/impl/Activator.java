package com.sap.sailing.domain.swisstimingadapter.persistence.impl;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoRaceLogStoreFactory;
import com.sap.sailing.domain.persistence.MongoRegattaLogStoreFactory;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingAdapterFactory;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingArchiveConfiguration;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingConfiguration;
import com.sap.sailing.domain.swisstimingadapter.impl.SwissTimingTrackingConnectivityParameters;
import com.sap.sailing.domain.swisstimingadapter.persistence.SwissTimingAdapterPersistence;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParametersHandler;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.security.SecurityService;
import com.sap.sse.util.ServiceTrackerFactory;

public class Activator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    
    @Override
    public void start(BundleContext context) throws Exception {
        for (CollectionNames name : CollectionNames.values()) {
            MongoDBService.INSTANCE.registerExclusively(CollectionNames.class, name.name());
        }
        new Thread(() -> {
            final ServiceTracker<SecurityService, SecurityService> securityServiceServiceTracker = ServiceTrackerFactory
                    .createAndOpen(context, SecurityService.class);
            final ServiceTracker<MongoObjectFactory, MongoObjectFactory> mongoObjectFactoryServiceTracker = ServiceTrackerFactory.createAndOpen(context, MongoObjectFactory.class);
            final ServiceTracker<DomainObjectFactory, DomainObjectFactory> domainObjectFactoryServiceTracker = ServiceTrackerFactory.createAndOpen(context, DomainObjectFactory.class);
            final ServiceTracker<SwissTimingAdapterFactory, SwissTimingAdapterFactory> swissTimingAdapterFactoryServiceTracker = ServiceTrackerFactory.createAndOpen(context, SwissTimingAdapterFactory.class);
            try {
                final MongoObjectFactory mongoObjectFactory = mongoObjectFactoryServiceTracker.waitForService(0);
                final DomainObjectFactory domainObjectFactory = domainObjectFactoryServiceTracker.waitForService(0);
                final SwissTimingAdapterFactory swissTimingAdapterFactory = swissTimingAdapterFactoryServiceTracker.waitForService(0);
                final SecurityService securityService = securityServiceServiceTracker.waitForService(0);
                final Dictionary<String, Object> properties = new Hashtable<String, Object>();
                final com.sap.sailing.domain.swisstimingadapter.DomainFactory domainFactory = swissTimingAdapterFactory.getOrCreateSwissTimingAdapter(
                        domainObjectFactory.getBaseDomainFactory()).getSwissTimingDomainFactory();
                final SwissTimingConnectivityParamsHandler paramsHandler = new SwissTimingConnectivityParamsHandler(
                        MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStore(mongoObjectFactory, domainObjectFactory),
                        MongoRegattaLogStoreFactory.INSTANCE.getMongoRegattaLogStore(mongoObjectFactory, domainObjectFactory),
                        domainFactory);
                properties.put(TypeBasedServiceFinder.TYPE, SwissTimingTrackingConnectivityParameters.TYPE);
                context.registerService(RaceTrackingConnectivityParametersHandler.class, paramsHandler, properties);

                for (SwissTimingArchiveConfiguration swissTimingArchive : SwissTimingAdapterPersistence.INSTANCE
                        .getSwissTimingArchiveConfigurations()) {
                    securityService.migrateOwnership(swissTimingArchive);
                }

                for (SwissTimingConfiguration swissTiming : SwissTimingAdapterPersistence.INSTANCE
                        .getSwissTimingConfigurations()) {
                    securityService.migrateOwnership(swissTiming);
                }

                // we do not necessarily have swisstiming configs, so ensure that migration is marked as done
                securityService.assumeOwnershipMigrated(SecuredDomainType.SWISS_TIMING_ACCOUNT.getName());
                securityService.assumeOwnershipMigrated(SecuredDomainType.SWISS_TIMING_ARCHIVE_ACCOUNT.getName());

            } catch (Exception e) {
                logger.log(Level.SEVERE, "Exception trying to register SwissTiming RaceTrackingConnectivityParametersHandler implementation", e);
            }
        }, getClass().getName() + " registering connectivity handler").start();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
