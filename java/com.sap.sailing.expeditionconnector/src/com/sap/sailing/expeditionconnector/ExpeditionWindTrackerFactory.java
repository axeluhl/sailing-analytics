package com.sap.sailing.expeditionconnector;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.sap.sailing.declination.DeclinationService;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.WindTracker;
import com.sap.sailing.domain.tracking.WindTrackerFactory;
import com.sap.sailing.expeditionconnector.impl.Activator;

public class ExpeditionWindTrackerFactory implements WindTrackerFactory {
    private static Logger logger = Logger.getLogger(ExpeditionWindTrackerFactory.class.getName());
    
    private static ExpeditionWindTrackerFactory defaultInstance;

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

    public synchronized static ExpeditionWindTrackerFactory getInstance() {
        if (defaultInstance == null) {
            defaultInstance = new ExpeditionWindTrackerFactory();
        }
        return defaultInstance;
    }
    
    @Override
    public WindTracker createWindTracker(DynamicTrackedRegatta trackedRegatta, RaceDefinition race,
            boolean correctByDeclination) throws SocketException {
        WindTracker result = getExistingWindTracker(race);
        if (result == null) {
            DynamicTrackedRace trackedRace = trackedRegatta.getTrackedRace(race);
            UDPExpeditionReceiver receiver = getOrCreateWindReceiverOnDefaultPort();
            result = new ExpeditionWindTracker(trackedRace,
                    correctByDeclination ? DeclinationService.INSTANCE : null, receiver, this);
            windTrackers.put(race, result);
        }
        return result;
    }
    
    @Override
    public WindTracker getExistingWindTracker(RaceDefinition race) {
        return windTrackers.get(race);
    }
    
    public UDPExpeditionReceiver getOrCreateWindReceiverOnDefaultPort() throws SocketException {
        return getOrCreateWindReceiverForPort(defaultPort);
    }
    
    private synchronized UDPExpeditionReceiver getOrCreateWindReceiverForPort(int port) throws SocketException {
        UDPExpeditionReceiver receiver = windReceivers.get(port);
        if (receiver == null) {
            receiver = new UDPExpeditionReceiver(port);
            windReceivers.put(port, receiver);
            Thread t = new Thread(receiver, "Expedition Wind Receiver on port "+port);
            t.setDaemon(true);
            t.start();
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
}
