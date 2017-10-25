package com.sap.sailing.expeditionconnector;

import java.net.SocketException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.sap.sailing.declination.DeclinationService;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.WindTracker;
import com.sap.sailing.domain.tracking.WindTrackerFactory;
import com.sap.sailing.expeditionconnector.impl.Activator;
import com.sap.sailing.expeditionconnector.impl.ExpeditionGpsDeviceIdentifierImpl;
import com.sap.sailing.expeditionconnector.impl.ExpeditionSensorDeviceIdentifierImpl;

public class ExpeditionTrackerFactory implements WindTrackerFactory, DeviceRegistry {
    private static Logger logger = Logger.getLogger(ExpeditionTrackerFactory.class.getName());
    
    private static ExpeditionTrackerFactory defaultInstance;

    /**
     * Remembers the wind tracker and the port on which the UDP receiver with which the wind tracker is
     * registers is listening for incoming Expedition messages.
     */
    private final Map<RaceDefinition, WindTracker> windTrackers;
    
    private final Map<Integer, UDPExpeditionReceiver> windReceivers;
    
    /**
     * When one or more device configurations exist then each {@link UDPExpeditionReceiver} created by this factory will
     * also produce {@link GPSFixMoving} and {@link DoubleVectorFix} fixes and send them to the {@link SensorFixStore},
     * using device identifiers of type {@link ExpeditionGpsDeviceIdentifier} and
     * {@link ExpeditionSensorDeviceIdentifier}, respectively, whose inner ID is the
     * {@link ExpeditionDeviceConfiguration#getDeviceUuid() UUID} of the device configuration.
     */
    private final ConcurrentHashMap<UUID, ExpeditionDeviceConfiguration> deviceConfigurations;
    
    /**
     * Holds the mappings from Expedition boat IDs (default being 0, counting upwards) to the
     * {@link ExpeditionDeviceConfiguration} that {@link ExpeditionDeviceConfiguration#getExpeditionBoatId() has this
     * boat ID}.
     */
    private final ConcurrentHashMap<Integer, ExpeditionDeviceConfiguration> devicesPerBoatId;
    
    private final int defaultPort;

    private final SensorFixStore sensorFixStore;

    public ExpeditionTrackerFactory(SensorFixStore sensorFixStore) {
        this.windTrackers = new HashMap<RaceDefinition, WindTracker>();
        this.sensorFixStore = sensorFixStore;
        windReceivers = new HashMap<Integer, UDPExpeditionReceiver>();
        defaultPort = Activator.getInstance().getExpeditionUDPPort();
        deviceConfigurations = new ConcurrentHashMap<>(); // TODO add persistence; these need to be loaded from the DB
        devicesPerBoatId = new ConcurrentHashMap<>();
        logger.info("Created "+getClass().getName()+" with default UDP port "+defaultPort);
    }

    public synchronized static ExpeditionTrackerFactory getInstance() {
        return getInstance(/* sensorFixStore */ null);
    }
    
    public static ExpeditionTrackerFactory getInstance(SensorFixStore sensorFixStore) {
        if (defaultInstance == null) {
            defaultInstance = new ExpeditionTrackerFactory(sensorFixStore);
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
            receiver = new UDPExpeditionReceiver(port, this);
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
    synchronized void trackerStopped(RaceDefinition race, ExpeditionWindTracker windTracker) {
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
    public String toString() {
        return "ExpeditionWindTrackerFactory [defaultPort=" + defaultPort + "]";
    }

    public Iterable<? extends ExpeditionDeviceConfiguration> getDeviceConfigurations() {
        return Collections.unmodifiableCollection(deviceConfigurations.values());
    }
    
    public void addOrReplaceDeviceConfiguration(ExpeditionDeviceConfiguration deviceConfiguration) {
        if (deviceConfiguration.getExpeditionBoatId() != null &&
                devicesPerBoatId.containsKey(deviceConfiguration.getExpeditionBoatId()) &&
                !devicesPerBoatId.get(deviceConfiguration.getExpeditionBoatId()).equals(deviceConfiguration)) {
            throw new IllegalStateException("Trying to create an ambiguous Expedition Boat ID mapping: established is "+
                    devicesPerBoatId.get(deviceConfiguration.getExpeditionBoatId())+
                    " and boat ID #"+deviceConfiguration.getExpeditionBoatId()+" therefore cannot be mapped to "+deviceConfiguration+
                    " at the same time.");
        }
        deviceConfigurations.put(deviceConfiguration.getDeviceUuid(), deviceConfiguration);
        final ExpeditionDeviceConfiguration old = deviceConfigurations.get(deviceConfiguration.getDeviceUuid());
        if (old != null && old.getExpeditionBoatId() != null) {
            devicesPerBoatId.remove(old.getExpeditionBoatId());
        }
        if (deviceConfiguration.getExpeditionBoatId() != null) {
            devicesPerBoatId.put(deviceConfiguration.getExpeditionBoatId(), deviceConfiguration);
        }
    }

    public void removeDeviceConfiguration(ExpeditionDeviceConfiguration deviceConfiguration) {
        deviceConfigurations.remove(deviceConfiguration.getDeviceUuid());
        if (deviceConfiguration.getExpeditionBoatId() != null) {
            devicesPerBoatId.remove(deviceConfiguration.getExpeditionBoatId());
        }
    }

    @Override
    public ExpeditionGpsDeviceIdentifier getGpsDeviceIdentifier(int boatId) {
        final ExpeditionDeviceConfiguration deviceConfig = devicesPerBoatId.get(boatId);
        final ExpeditionGpsDeviceIdentifier result;
        if (deviceConfig == null) {
            result = null;
        } else {
            result = new ExpeditionGpsDeviceIdentifierImpl(deviceConfig.getDeviceUuid());
        }
        return result;
    }

    @Override
    public ExpeditionSensorDeviceIdentifier getSensorDeviceIdentifier(int boatId) {
        final ExpeditionDeviceConfiguration deviceConfig = devicesPerBoatId.get(boatId);
        final ExpeditionSensorDeviceIdentifier result;
        if (deviceConfig == null) {
            result = null;
        } else {
            result = new ExpeditionSensorDeviceIdentifierImpl(deviceConfig.getDeviceUuid());
        }
        return result;
    }

    @Override
    public SensorFixStore getSensorFixStore() {
        return sensorFixStore;
    }
}
