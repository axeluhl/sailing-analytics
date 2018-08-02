package com.sap.sailing.domain.swisstimingadapter.impl;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.swisstimingadapter.DomainFactory;
import com.sap.sailing.domain.swisstimingadapter.Race;
import com.sap.sailing.domain.swisstimingadapter.SailMasterConnector;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;
import com.sap.sailing.domain.swisstimingadapter.SailMasterTransceiver;
import com.sap.sailing.domain.swisstimingadapter.StartList;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingArchiveConfiguration;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingConfiguration;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingMessageParser;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingRaceTracker;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sse.common.Util;

public class SwissTimingFactoryImpl implements SwissTimingFactory {
    private static final Logger logger = Logger.getLogger(SwissTimingFactoryImpl.class.getName());
    
    private final Map<Util.Triple<String, Integer, String>, SailMasterConnector> connectors;

    public SwissTimingFactoryImpl() {
        connectors = new HashMap<>();
    }

    @Override
    public SwissTimingMessageParser createMessageParser() {
        return new SwissTimingMessageParserImpl();
    }

    @Override
    public SailMasterConnector getOrCreateSailMasterConnector(String host, int port, String raceId, String raceName,
            String raceDescription, BoatClass boatClass) throws InterruptedException, ParseException {
        if (Boolean.valueOf(System.getProperty("simulateLiveMode", "false"))) {
            return getOrCreateSailMasterLiveSimulatorConnector(host, port, raceId, raceName, raceDescription, boatClass);
        } else {
            Util.Triple<String, Integer, String> key = new Util.Triple<String, Integer, String>(host, port, raceId);
            SailMasterConnector result = connectors.get(key);
            if (result == null || result.isStopped()) {
                if (result == null) {
                    logger.info("Creating a new connector for "+key+" because none found");
                } else {
                    logger.info("Creating a new connector for "+key+" because the old one was stopped");
                }
                result = new SailMasterConnectorImpl(host, port, raceId, raceName, raceDescription, boatClass);
                connectors.put(key, result);
                // TODO how do connectors get stopped, terminated and removed from the connectors map again?
            } else {
                logger.info("Re-using connector for "+key+" because it wasn't stopped");
            }
            return result;
        }
    }

    @Override
    public SailMasterConnector getOrCreateSailMasterLiveSimulatorConnector(String host, int port, String raceId, String raceName,
            String raceDescription, BoatClass boatClass) throws InterruptedException, ParseException {
        Util.Triple<String, Integer, String> key = new Util.Triple<>(host, port, raceId);
        SailMasterConnector result = connectors.get(key);
        if (result == null) {
            result = new SailMasterLiveSimulatorConnectorImpl(host, port, raceId, raceName, raceDescription, boatClass);
            connectors.put(key, result);
            // TODO how do connectors get stopped, terminated and removed from the connectors map again?
        } else if (result.isStopped()) {
            result = new SailMasterLiveSimulatorConnectorImpl(host, port, raceId, raceName, raceDescription, boatClass);
            connectors.put(key, result);
        }
        return result;
    }

    @Override
    public SwissTimingConfiguration createSwissTimingConfiguration(String name, String jsonURL, String hostname, Integer port, String updateURL, String updateUsername, String updatePassword) {
        return new SwissTimingConfigurationImpl(name, jsonURL, hostname, port, updateURL, updateUsername, updatePassword);
    }

    @Override
    public SwissTimingRaceTracker createRaceTracker(String raceID, String raceName, String raceDescription,
            BoatClass boatClass, String hostname, int port, StartList startList, long delayToLiveInMillis,
            RaceLogStore raceLogStore, RegattaLogStore regattaLogStore, WindStore windStore,
            boolean useInternalMarkPassingAlgorithm, DomainFactory domainFactory,
            TrackedRegattaRegistry trackedRegattaRegistry, RaceLogResolver raceLogResolver, SwissTimingTrackingConnectivityParameters connectivityParams)
            throws InterruptedException, UnknownHostException, IOException, ParseException {
        return new SwissTimingRaceTrackerImpl(raceID, raceName, raceDescription, boatClass, hostname, port, startList,
                raceLogStore, regattaLogStore, windStore, domainFactory, this,
                trackedRegattaRegistry, raceLogResolver, delayToLiveInMillis, useInternalMarkPassingAlgorithm, connectivityParams);
    }

    @Override
    public RaceTracker createRaceTracker(Regatta regatta, String raceID, String raceName, String raceDescription,
            BoatClass boatClass, String hostname, int port, StartList startList, long delayToLiveInMillis,
            WindStore windStore, boolean useInternalMarkPassingAlgorithm, DomainFactory domainFactory,
            TrackedRegattaRegistry trackedRegattaRegistry, RaceLogResolver raceLogResolver, RaceLogStore raceLogStore,
            RegattaLogStore regattaLogStore, SwissTimingTrackingConnectivityParameters connectivityParams)
            throws UnknownHostException, InterruptedException, IOException, ParseException {
        return new SwissTimingRaceTrackerImpl(regatta, raceID, raceName, raceDescription, boatClass, hostname, port,
                startList, windStore, domainFactory, this, trackedRegattaRegistry, raceLogStore, regattaLogStore,
                raceLogResolver, delayToLiveInMillis, useInternalMarkPassingAlgorithm, connectivityParams);
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
    public Race createRace(String raceId, String raceName, String description, BoatClass boatClass) {
        return new RaceImpl(raceId, raceName, description, boatClass);
    }

    @Override
    public SwissTimingArchiveConfiguration createSwissTimingArchiveConfiguration(String jsonUrl) {
        return new SwissTimingArchiveConfigurationImpl(jsonUrl);
    }
}
