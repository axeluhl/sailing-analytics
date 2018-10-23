package com.sap.sailing.server.impl;

import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.WindFinderReviewedSpotsCollectionIdProvider;
import com.sap.sailing.domain.common.tracking.impl.DoubleVectorFixImpl;
import com.sap.sailing.domain.common.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.racelog.tracking.FixMongoHandler;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.DoubleVectorFixMongoHandlerImpl;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.GPSFixMongoHandlerImpl;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.GPSFixMovingMongoHandlerImpl;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.racelog.tracking.SensorFixStoreSupplier;
import com.sap.sailing.domain.tracking.TrackedRegattaListener;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceMXBean;
import com.sap.sailing.server.impl.preferences.model.BoatClassNotificationPreferences;
import com.sap.sailing.server.impl.preferences.model.CompetitorNotificationPreferences;
import com.sap.sailing.server.impl.preferences.model.SailorProfilePreferences;
import com.sap.sailing.server.notification.impl.SailingNotificationServiceImpl;
import com.sap.sailing.server.statistics.TrackedRaceStatisticsCache;
import com.sap.sailing.server.statistics.TrackedRaceStatisticsCacheImpl;
import com.sap.sse.MasterDataImportClassLoaderService;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.common.Util;
import com.sap.sse.mail.MailService;
import com.sap.sse.mail.queue.MailQueue;
import com.sap.sse.mail.queue.impl.ExecutorMailQueue;
import com.sap.sse.osgi.CachedOsgiTypeBasedServiceFinderFactory;
import com.sap.sse.replication.Replicable;
import com.sap.sse.security.PreferenceConverter;
import com.sap.sse.util.ClearStateTestSupport;
import com.sap.sse.util.ServiceTrackerFactory;

public class Activator implements BundleActivator {

    private static final Logger logger = Logger.getLogger(Activator.class.getName());

    private static final String CLEAR_PERSISTENT_COMPETITORS_PROPERTY_NAME = "persistentcompetitors.clear";
    
    private static final String RESTORE_TRACKED_RACES_PROPERTY_NAME = "restore.tracked.races";

    private static ExtenderBundleTracker extenderBundleTracker;

    private static BundleContext context;

    private CachedOsgiTypeBasedServiceFinderFactory serviceFinderFactory;

    private RacingEventServiceImpl racingEventService;

    private final boolean clearPersistentCompetitors;
    
    private final boolean restoreTrackedRaces;

    private Set<ServiceRegistration<?>> registrations = new HashSet<>();

    private ObjectName mBeanName;

    private ServiceTracker<MasterDataImportClassLoaderService, MasterDataImportClassLoaderService> masterDataImportClassLoaderServiceTracker;

    private ServiceTracker<PolarDataService, PolarDataService> polarDataServiceTracker;

    private OSGiBasedTrackedRegattaListener trackedRegattaListener;

    private MailQueue mailQueue;

    private SailingNotificationServiceImpl notificationService;

    private ServiceTracker<MailService, MailService> mailServiceTracker;
    
    public Activator() {
        clearPersistentCompetitors = Boolean
                .valueOf(System.getProperty(CLEAR_PERSISTENT_COMPETITORS_PROPERTY_NAME, "" + false));
        restoreTrackedRaces = Boolean
                .valueOf(System.getProperty(RESTORE_TRACKED_RACES_PROPERTY_NAME, "" + false));
        logger.log(Level.INFO,
                "setting " + CLEAR_PERSISTENT_COMPETITORS_PROPERTY_NAME + " to " + clearPersistentCompetitors);
        // there is exactly one instance of the racingEventService in the whole server
    }

    public void start(BundleContext context) throws Exception {
        Activator.context = context;

        extenderBundleTracker = new ExtenderBundleTracker(context);
        extenderBundleTracker.open();

        mailServiceTracker = ServiceTrackerFactory.createAndOpen(context, MailService.class);
        mailQueue = new ExecutorMailQueue(mailServiceTracker);
        notificationService = new SailingNotificationServiceImpl(context, mailQueue);

        trackedRegattaListener = new OSGiBasedTrackedRegattaListener(context);
        
        final TrackedRaceStatisticsCache trackedRaceStatisticsCache = new TrackedRaceStatisticsCacheImpl();
        registrations.add(context.registerService(TrackedRaceStatisticsCache.class.getName(), trackedRaceStatisticsCache, null));
        registrations.add(context.registerService(TrackedRegattaListener.class.getName(), trackedRaceStatisticsCache, null));

        // At this point the OSGi resolver is used as device type service finder.
        // In the case that we are not in an OSGi context (e.g. running a JUnit test instead),
        // this code block is not run, and the test case can inject some other type of finder
        // instead.
        serviceFinderFactory = new CachedOsgiTypeBasedServiceFinderFactory(context);

        racingEventService = new RacingEventServiceImpl(clearPersistentCompetitors, serviceFinderFactory,
                trackedRegattaListener, notificationService, trackedRaceStatisticsCache, restoreTrackedRaces);
        notificationService.setRacingEventService(racingEventService);

        masterDataImportClassLoaderServiceTracker = new ServiceTracker<MasterDataImportClassLoaderService, MasterDataImportClassLoaderService>(
                context, MasterDataImportClassLoaderService.class,
                new MasterDataImportClassLoaderServiceTrackerCustomizer(context, racingEventService));
        masterDataImportClassLoaderServiceTracker.open();

        polarDataServiceTracker = new ServiceTracker<PolarDataService, PolarDataService>(context,
                PolarDataService.class, new PolarDataServiceTrackerCustomizer(context, racingEventService));
        polarDataServiceTracker.open();

        // register the racing service in the OSGi registry
        racingEventService.setBundleContext(context);
        context.registerService(MongoObjectFactory.class, racingEventService.getMongoObjectFactory(), /* properties */ null);
        context.registerService(DomainObjectFactory.class, racingEventService.getDomainObjectFactory(), /* properties */ null);
        final Dictionary<String, String> replicableServiceProperties = new Hashtable<>();
        replicableServiceProperties.put(Replicable.OSGi_Service_Registry_ID_Property_Name, racingEventService.getId().toString());
        context.registerService(Replicable.class, racingEventService, replicableServiceProperties);
        context.registerService(RacingEventService.class, racingEventService, null);
        context.registerService(RaceLogResolver.class, racingEventService, null);
        context.registerService(ClearStateTestSupport.class, racingEventService, null);
        context.registerService(SensorFixStoreSupplier.class, racingEventService, null);
        context.registerService(WindFinderReviewedSpotsCollectionIdProvider.class, racingEventService, null);
        Dictionary<String, String> properties = new Hashtable<String, String>();
        final GPSFixMongoHandlerImpl gpsFixMongoHandler = new GPSFixMongoHandlerImpl(
                racingEventService.getMongoObjectFactory(), racingEventService.getDomainObjectFactory());
        properties.put(TypeBasedServiceFinder.TYPE, GPSFixImpl.class.getName());
        registrations.add(context.registerService(FixMongoHandler.class, gpsFixMongoHandler, properties));
        // legacy type name; some DBs may still contain fixes marked with this old package name:
        properties.put(TypeBasedServiceFinder.TYPE, "com.sap.sailing.domain.tracking.impl.GPSFixImpl");
        registrations.add(context.registerService(FixMongoHandler.class, gpsFixMongoHandler, properties));
        final GPSFixMovingMongoHandlerImpl gpsFixMovingMongoHandler = new GPSFixMovingMongoHandlerImpl(
                racingEventService.getMongoObjectFactory(), racingEventService.getDomainObjectFactory());
        properties.put(TypeBasedServiceFinder.TYPE, GPSFixMovingImpl.class.getName());
        registrations.add(context.registerService(FixMongoHandler.class, gpsFixMovingMongoHandler, properties));
        // legacy type name; some DBs may still contain fixes marked with this old package name:
        properties.put(TypeBasedServiceFinder.TYPE, "com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl");
        registrations.add(context.registerService(FixMongoHandler.class, gpsFixMovingMongoHandler, properties));
        properties.put(TypeBasedServiceFinder.TYPE, DoubleVectorFixImpl.class.getName());
        registrations.add(context.registerService(FixMongoHandler.class, new DoubleVectorFixMongoHandlerImpl(
                racingEventService.getMongoObjectFactory(), racingEventService.getDomainObjectFactory()), properties));
        registerPreferenceConvertersForUserStore(context);
        // Add an MBean for the service to the JMX bean server:
        RacingEventServiceMXBean mbean = new RacingEventServiceMXBeanImpl(racingEventService);
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        mBeanName = new ObjectName("com.sap.sailing:type=RacingEventService");
        mbs.registerMBean(mbean, mBeanName);
        logger.log(Level.INFO, "Started " + context.getBundle().getSymbolicName() + ". Character encoding: "
                + Charset.defaultCharset());
    }

    /**
     * {@link #racingEventService} must already be initialized when calling this method
     */
    protected void registerPreferenceConvertersForUserStore(BundleContext context) {
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(PreferenceConverter.KEY_PARAMETER_NAME, CompetitorNotificationPreferences.PREF_NAME);
        registrations.add(context.registerService(PreferenceConverter.class,
                new GenericJSONPreferenceConverter<>(() -> new CompetitorNotificationPreferences(racingEventService)),
                properties));
        properties.put(PreferenceConverter.KEY_PARAMETER_NAME, BoatClassNotificationPreferences.PREF_NAME);
        registrations.add(context.registerService(PreferenceConverter.class,
                new GenericJSONPreferenceConverter<>(() -> new BoatClassNotificationPreferences(racingEventService)),
                properties));
        properties.put(PreferenceConverter.KEY_PARAMETER_NAME, SailorProfilePreferences.PREF_NAME);
        registrations.add(context.registerService(PreferenceConverter.class,
                new GenericJSONPreferenceConverter<>(
                        () -> new SailorProfilePreferences(racingEventService.getCompetitorAndBoatStore())),
                properties));
    }

    public static BundleContext getContext() {
        return context;
    }

    public void stop(BundleContext context) throws Exception {
        masterDataImportClassLoaderServiceTracker.close();
        if (extenderBundleTracker != null) {
            extenderBundleTracker.close();
        }
        if (serviceFinderFactory != null) {
            serviceFinderFactory.close();
        }
        // stop the tracking of the wind and all races
        for (Util.Triple<Regatta, RaceDefinition, String> windTracker : racingEventService.getWindTrackedRaces()) {
            racingEventService.stopTrackingWind(windTracker.getA(), windTracker.getB());
        }
        for (Regatta regatta : racingEventService.getAllRegattas()) {
            racingEventService.stopTracking(regatta, /* willBeRemoved */ true);
        }
        for (ServiceRegistration<?> reg : registrations) {
            reg.unregister();
        }
        trackedRegattaListener.close();
        registrations.clear();
        notificationService.stop();
        mailQueue.stop();
        mailServiceTracker.close();
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        mbs.unregisterMBean(mBeanName);
    }

    private class MasterDataImportClassLoaderServiceTrackerCustomizer implements
            ServiceTrackerCustomizer<MasterDataImportClassLoaderService, MasterDataImportClassLoaderService> {

        private final BundleContext context;
        private RacingEventServiceImpl racingEventService;

        public MasterDataImportClassLoaderServiceTrackerCustomizer(BundleContext context,
                RacingEventServiceImpl racingEventService) {
            this.context = context;
            this.racingEventService = racingEventService;
        }

        @Override
        public MasterDataImportClassLoaderService addingService(
                ServiceReference<MasterDataImportClassLoaderService> reference) {
            MasterDataImportClassLoaderService service = context.getService(reference);
            racingEventService.addMasterDataClassLoader(service.getClassLoader());
            return service;
        }

        @Override
        public void modifiedService(ServiceReference<MasterDataImportClassLoaderService> reference,
                MasterDataImportClassLoaderService service) {
        }

        @Override
        public void removedService(ServiceReference<MasterDataImportClassLoaderService> reference,
                MasterDataImportClassLoaderService service) {
            racingEventService.removeMasterDataClassLoader(service.getClassLoader());
        }

    }

    private class PolarDataServiceTrackerCustomizer
            implements ServiceTrackerCustomizer<PolarDataService, PolarDataService> {

        private final BundleContext context;
        private RacingEventServiceImpl racingEventService;

        public PolarDataServiceTrackerCustomizer(BundleContext context, RacingEventServiceImpl racingEventService) {
            this.context = context;
            this.racingEventService = racingEventService;
        }

        @Override
        public PolarDataService addingService(ServiceReference<PolarDataService> reference) {
            PolarDataService service = context.getService(reference);
            racingEventService.setPolarDataService(service);
            return service;
        }

        @Override
        public void modifiedService(ServiceReference<PolarDataService> reference, PolarDataService service) {
        }

        @Override
        public void removedService(ServiceReference<PolarDataService> reference, PolarDataService service) {
            racingEventService.unsetPolarDataService(service);
        }

    }
}
