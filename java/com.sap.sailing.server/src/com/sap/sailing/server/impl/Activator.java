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
import org.osgi.framework.ServiceRegistration;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.racelog.tracking.TypeBasedServiceFinder;
import com.sap.sailing.domain.persistence.racelog.tracking.GPSFixMongoHandler;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.GPSFixMongoHandlerImpl;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.GPSFixMovingMongoHandlerImpl;
import com.sap.sailing.domain.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceMXBean;
import com.sap.sailing.server.racelog.tracking.CachedOsgiTypeBasedServiceFinderFactory;
import com.sap.sse.common.Util;
import com.sap.sse.replication.Replicable;
import com.sap.sse.util.ClearStateTestSupport;

public class Activator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());

    private static final String CLEAR_PERSISTENT_COMPETITORS_PROPERTY_NAME = "persistentcompetitors.clear";
    
    private static ExtenderBundleTracker extenderBundleTracker;
    
    private CachedOsgiTypeBasedServiceFinderFactory serviceFinderFactory;

    private RacingEventServiceImpl racingEventService;
    
    private final boolean clearPersistentCompetitors;
    
    private Set<ServiceRegistration<?>> registrations = new HashSet<>();

    private ObjectName mBeanName;

    public Activator() {
        clearPersistentCompetitors = Boolean.valueOf(System.getProperty(CLEAR_PERSISTENT_COMPETITORS_PROPERTY_NAME, ""+false));
        logger.log(Level.INFO, "setting "+CLEAR_PERSISTENT_COMPETITORS_PROPERTY_NAME+" to "+clearPersistentCompetitors);
        // there is exactly one instance of the racingEventService in the whole server
        
    }
    
    public void start(BundleContext context) throws Exception {
        extenderBundleTracker = new ExtenderBundleTracker(context);
        extenderBundleTracker.open();
        
        // At this point the OSGi resolver is used as device type service finder.
        // In the case that we are not in an OSGi context (e.g. running a JUnit test instead),
        // this code block is not run, and the test case can inject some other type of finder
        // instead.
        serviceFinderFactory = new CachedOsgiTypeBasedServiceFinderFactory(context);
        racingEventService = new RacingEventServiceImpl(clearPersistentCompetitors, serviceFinderFactory);

        // register the racing service in the OSGi registry
        racingEventService.setBundleContext(context);
        final Dictionary<String, String> replicableServiceProperties = new Hashtable<>();
        replicableServiceProperties.put(Replicable.OSGi_Service_Registry_ID_Property_Name, racingEventService.getId().toString());
        context.registerService(Replicable.class.getName(), racingEventService, replicableServiceProperties);
        context.registerService(RacingEventService.class.getName(), racingEventService, null);
        context.registerService(ClearStateTestSupport.class.getName(), racingEventService, null);
                
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(TypeBasedServiceFinder.TYPE, GPSFixImpl.class.getName());
        registrations.add(context.registerService(GPSFixMongoHandler.class,
                        new GPSFixMongoHandlerImpl(racingEventService.getMongoObjectFactory(),
                                racingEventService.getDomainObjectFactory()), properties));
        properties.put(TypeBasedServiceFinder.TYPE, GPSFixMovingImpl.class.getName());
        registrations.add(context.registerService(GPSFixMongoHandler.class,
                        new GPSFixMovingMongoHandlerImpl(racingEventService.getMongoObjectFactory(),
                                racingEventService.getDomainObjectFactory()), properties));
        // Add an MBean for the service to the JMX bean server:
        RacingEventServiceMXBean mbean = new RacingEventServiceMXBeanImpl(racingEventService);
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        mBeanName = new ObjectName("com.sap.sailing:type=RacingEventService");
        mbs.registerMBean(mbean, mBeanName);
        logger.log(Level.INFO, "Started "+context.getBundle().getSymbolicName()+". Character encoding: "+
                Charset.defaultCharset());
    }
    
    public void stop(BundleContext context) throws Exception {
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
            racingEventService.stopTracking(regatta);
        }
        for (ServiceRegistration<?> reg : registrations) {
            reg.unregister();
        }
        registrations.clear();
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        mbs.unregisterMBean(mBeanName);
    }
}
