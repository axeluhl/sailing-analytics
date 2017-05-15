package com.sap.sailing.domain.swisstimingreplayadapter.impl;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoRaceLogStoreFactory;
import com.sap.sailing.domain.persistence.MongoRegattaLogStoreFactory;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingAdapterFactory;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayServiceFactory;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParametersHandler;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.util.ServiceTrackerFactory;

public class Activator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    
    private final SwissTimingReplayServiceFactory swissTimingReplayServiceFactory;

    public Activator() {
        // there is exactly one instance of the racingEventService in the whole server
        swissTimingReplayServiceFactory = new SwissTimingReplayServiceFactoryImpl();
    }
    
    public void start(BundleContext context) throws Exception {
        // register the racing service in the OSGi registry
        context.registerService(SwissTimingReplayServiceFactory.class.getName(), swissTimingReplayServiceFactory, null);
        new Thread(() -> {
            final ServiceTracker<MongoObjectFactory, MongoObjectFactory> mongoObjectFactoryServiceTracker = ServiceTrackerFactory.createAndOpen(context, MongoObjectFactory.class);
            final ServiceTracker<DomainObjectFactory, DomainObjectFactory> domainObjectFactoryServiceTracker = ServiceTrackerFactory.createAndOpen(context, DomainObjectFactory.class);
            final ServiceTracker<SwissTimingAdapterFactory, SwissTimingAdapterFactory> swissTimingAdapterFactoryServiceTracker = ServiceTrackerFactory.createAndOpen(context, SwissTimingAdapterFactory.class);
            final ServiceTracker<RaceLogResolver, RaceLogResolver> raceLogResolverServiceTracker = ServiceTrackerFactory.createAndOpen(context, RaceLogResolver.class);
            try {
                final MongoObjectFactory mongoObjectFactory = mongoObjectFactoryServiceTracker.waitForService(0);
                final DomainObjectFactory domainObjectFactory = domainObjectFactoryServiceTracker.waitForService(0);
                final SwissTimingAdapterFactory swissTimingAdapterFactory = swissTimingAdapterFactoryServiceTracker.waitForService(0);
                final RaceLogResolver raceLogResolver = raceLogResolverServiceTracker.waitForService(0);
                final Dictionary<String, Object> properties = new Hashtable<String, Object>();
                final com.sap.sailing.domain.swisstimingadapter.DomainFactory domainFactory = swissTimingAdapterFactory
                        .getOrCreateSwissTimingAdapter(domainObjectFactory.getBaseDomainFactory())
                        .getSwissTimingDomainFactory();
                final SwissTimingReplayConnectivityParamsHandler paramsHandler = new SwissTimingReplayConnectivityParamsHandler(
                        MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStore(mongoObjectFactory, domainObjectFactory),
                        MongoRegattaLogStoreFactory.INSTANCE.getMongoRegattaLogStore(mongoObjectFactory, domainObjectFactory),
                        domainFactory, swissTimingReplayServiceFactory.createSwissTimingReplayService(domainFactory, raceLogResolver));
                properties.put(TypeBasedServiceFinder.TYPE, SwissTimingReplayConnectivityParameters.TYPE);
                context.registerService(RaceTrackingConnectivityParametersHandler.class, paramsHandler, properties);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Exception trying to register SwissTiming Replay RaceTrackingConnectivityParametersHandler implementation", e);
            }
        }, getClass().getName() + " registering connectivity handler").start();
        logger.log(Level.INFO, "Started "+context.getBundle().getSymbolicName());
    }
    
    public void stop(BundleContext context) throws Exception {
    }
}
