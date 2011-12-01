package com.sap.sailing.expeditionconnector;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.declination.DeclinationService;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.WindTracker;
import com.sap.sailing.domain.tracking.WindTrackerFactory;
import com.sap.sailing.expeditionconnector.impl.Activator;

public class ExpeditionWindTrackerFactory implements WindTrackerFactory, BundleActivator {
    private static Logger logger = Logger.getLogger(ExpeditionWindTrackerFactory.class.getName());
    
    private static WindTrackerFactory defaultInstance;
    
    private static BundleContext defaultBundleContext;

    /**
     * Remembers the wind tracker and the port on which the UDP receiver with which the wind tracker is
     * registers is listening for incoming Expedition messages.
     */
    private final Map<RaceDefinition, WindTracker> windTrackers;
    
    private final Map<Integer, UDPExpeditionReceiver> windReceivers;
    
    private final int defaultPort;

    public ExpeditionWindTrackerFactory() {
        this.windTrackers = new HashMap<RaceDefinition, WindTracker>();
        windReceivers = new HashMap<Integer, UDPExpeditionReceiver>();
        defaultPort = Activator.getInstance().getExpeditionUDPPort();
        logger.info("Created "+getClass().getName()+" with default UDP port "+defaultPort);
    }

    public static WindTrackerFactory getInstance() {
        if (defaultInstance == null) {
            defaultInstance = new ExpeditionWindTrackerFactory();
        }
        return defaultInstance;
    }
    
    public static BundleContext getBundleContext() {
        return defaultBundleContext;
    }

    @Override
    public WindTracker createWindTracker(DynamicTrackedEvent trackedEvent, RaceDefinition race,
            boolean correctByDeclination) throws SocketException {
        WindTracker result = windTrackers.get(race);
        if (result == null) {
            DynamicTrackedRace trackedRace = trackedEvent.getTrackedRace(race);
            UDPExpeditionReceiver receiver = getOrCreateWindReceiverForPort(defaultPort);
            result = new ExpeditionWindTracker(trackedRace,
                    correctByDeclination ? DeclinationService.INSTANCE : null, receiver, this);
            windTrackers.put(race, result);
        }
        return result;
    }

    private synchronized UDPExpeditionReceiver getOrCreateWindReceiverForPort(int port) throws SocketException {
        UDPExpeditionReceiver receiver = windReceivers.get(port);
        if (receiver == null) {
            receiver = new UDPExpeditionReceiver(port);
            windReceivers.put(port, receiver);
            new Thread(receiver, "Expedition Wind Receiver on port "+port).start();
        }
        return receiver;
    }
    
    /**
     * Notifies the factory that the wind tracker has stopped tracking wind for <code>race</code>. This
     * will remove the tracker from the respective caches.
     */
    synchronized void windTrackerStopped(RaceDefinition race, ExpeditionWindTracker windTracker) {
        if (windTrackers.get(race) != windTracker) {
            throw new IllegalArgumentException("Intenral error: expected to remove wind tracker "+windTracker+
                    ", but another wind tracker "+windTrackers.get(race)+" was registered.");
        }
        windTrackers.remove(race);
        if (windTracker.getReceiver().isStopped()) {
            UDPExpeditionReceiver receiver = windReceivers.get(windTracker.getReceiver().getPort());
            if (receiver != windTracker.getReceiver()) {
                throw new IllegalArgumentException("Internal error: expected to remove wind receiver "+
                        windTracker.getReceiver()+" but found receiver "+receiver);
            }
            windReceivers.remove(windTracker.getReceiver().getPort());
        }
    }

    @Override
    public void start(BundleContext context) throws Exception {
        defaultBundleContext = context;
        defaultInstance = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
    
}
