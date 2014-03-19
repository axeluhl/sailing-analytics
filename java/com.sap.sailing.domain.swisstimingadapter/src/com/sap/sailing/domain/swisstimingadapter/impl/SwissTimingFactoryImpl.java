package com.sap.sailing.domain.swisstimingadapter.impl;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.swisstimingadapter.DomainFactory;
import com.sap.sailing.domain.swisstimingadapter.Race;
import com.sap.sailing.domain.swisstimingadapter.SailMasterConnector;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;
import com.sap.sailing.domain.swisstimingadapter.SailMasterTransceiver;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingArchiveConfiguration;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingConfiguration;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingMessageParser;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingRaceTracker;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.WindStore;

public class SwissTimingFactoryImpl implements SwissTimingFactory {
    private static final Logger logger = Logger.getLogger(SwissTimingFactoryImpl.class.getName());
    
    private final Map<Triple<String, Integer, String>, SailMasterConnector> connectors;

    public SwissTimingFactoryImpl() {
        connectors = new HashMap<>();
    }

    @Override
    public SwissTimingMessageParser createMessageParser() {
        return new SwissTimingMessageParserImpl();
    }

    @Override
    public SailMasterConnector getOrCreateSailMasterConnector(String host, int port, String raceId,
            String raceDescription) throws InterruptedException, ParseException {
        if (Boolean.valueOf(System.getProperty("simulateLiveMode", "false"))) {
            return getOrCreateSailMasterLiveSimulatorConnector(host, port, raceId, raceDescription);
        } else {
            Triple<String, Integer, String> key = new Triple<String, Integer, String>(host, port, raceId);
            SailMasterConnector result = connectors.get(key);
            if (result == null || result.isStopped()) {
                if (result == null) {
                    logger.info("Creating a new connector for "+key+" because none found");
                } else {
                    logger.info("Creating a new connector for "+key+" because the old one was stopped");
                }
                result = new SailMasterConnectorImpl(host, port, raceId, raceDescription);
                connectors.put(key, result);
                // TODO how do connectors get stopped, terminated and removed from the connectors map again?
            } else {
                logger.info("Re-using connector for "+key+" because it wasn't stopped");
            }
            return result;
        }
    }

    @Override
    public SailMasterConnector getOrCreateSailMasterLiveSimulatorConnector(String host, int port, String raceId,
            String raceDescription) throws InterruptedException, ParseException {
        Triple<String, Integer, String> key = new Triple<>(host, port, raceId);
        SailMasterConnector result = connectors.get(key);
        if (result == null) {
            result = new SailMasterLiveSimulatorConnectorImpl(host, port, raceId, raceDescription);
            connectors.put(key, result);
            // TODO how do connectors get stopped, terminated and removed from the connectors map again?
        } else if (result.isStopped()) {
            result = new SailMasterLiveSimulatorConnectorImpl(host, port, raceId, raceDescription);
            connectors.put(key, result);
        }
        return result;
    }

    @Override
    public SwissTimingConfiguration createSwissTimingConfiguration(String name, String hostname, int port, boolean canSendRequests) {
        return new SwissTimingConfigurationImpl(name, hostname, port, canSendRequests);
    }

    @Override
    public SwissTimingRaceTracker createRaceTracker(String raceID, String raceDescription, String hostname, int port,
            long delayToLiveInMillis, RaceLogStore raceLogStore, WindStore windStore, DomainFactory domainFactory,
            TrackedRegattaRegistry trackedRegattaRegistry) throws InterruptedException,
            UnknownHostException, IOException, ParseException {
        return new SwissTimingRaceTrackerImpl(raceID, raceDescription, hostname, port, raceLogStore, windStore, domainFactory, this,
                trackedRegattaRegistry, delayToLiveInMillis);
    }

    @Override
    public RaceTracker createRaceTracker(Regatta regatta, String raceID, String raceDescription, String hostname,
            int port, long delayToLiveInMillis, WindStore windStore, DomainFactory domainFactory,
            TrackedRegattaRegistry trackedRegattaRegistry) throws UnknownHostException,
            InterruptedException, IOException, ParseException {
        return new SwissTimingRaceTrackerImpl(regatta, raceID, raceDescription, hostname, port, windStore, domainFactory,
                this, trackedRegattaRegistry, delayToLiveInMillis);
    }

    @Override
    public SailMasterTransceiver createSailMasterTransceiver() {
        return new SailMasterTransceiverImpl();
    }

    @Override
    public SailMasterMessage createMessage(String message) {
        return new SailMasterMessageImpl(message);
    }

    @Override
    public Race createRace(String raceId, String description) {
        return new RaceImpl(raceId, description);
    }

    @Override
    public SwissTimingArchiveConfiguration createSwissTimingArchiveConfiguration(String jsonUrl) {
        return new SwissTimingArchiveConfigurationImpl(jsonUrl);
    }
}
