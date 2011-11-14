package com.sap.sailing.domain.swisstimingadapter.impl;

import java.io.IOException;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.swisstimingadapter.Race;
import com.sap.sailing.domain.swisstimingadapter.RaceSpecificMessageLoader;
import com.sap.sailing.domain.swisstimingadapter.SailMasterConnector;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;
import com.sap.sailing.domain.swisstimingadapter.SailMasterTransceiver;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingConfiguration;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingMessageParser;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingRaceTracker;
import com.sap.sailing.domain.tracking.TrackedEventRegistry;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.util.Util.Triple;

public class SwissTimingFactoryImpl implements SwissTimingFactory {
    private final Map<Triple<String, Integer, RaceSpecificMessageLoader>, SailMasterConnector> connectors;
    
    public SwissTimingFactoryImpl() {
        connectors = new HashMap<Triple<String, Integer, RaceSpecificMessageLoader>, SailMasterConnector>();
    }

    @Override
    public SwissTimingMessageParser createMessageParser() {
        return new SwissTimingMessageParserImpl();
    }

    @Override
    public SailMasterConnector getOrCreateSailMasterConnector(String host, int port, RaceSpecificMessageLoader messageLoader) throws InterruptedException {
        Triple<String, Integer, RaceSpecificMessageLoader> key = new Triple<String, Integer, RaceSpecificMessageLoader>(host, port, messageLoader);
        SailMasterConnector result = connectors.get(key);
        if (result == null) {
            result = new SailMasterConnectorImpl(host, port, messageLoader);
            connectors.put(key, result);
            // TODO how do connectors get stopped, terminated and removed from the connectors map again?
        } else if (result.isStopped()) {
            result = new SailMasterConnectorImpl(host, port, messageLoader);
            connectors.put(key, result);
        }
        return result;
    }

    @Override
    public SwissTimingConfiguration createSwissTimingConfiguration(String name, String hostname, int port) {
        return new SwissTimingConfigurationImpl(name, hostname, port);
    }

    @Override
    public SwissTimingRaceTracker createRaceTracker(String raceID, String hostname, int port, WindStore windStore,
            RaceSpecificMessageLoader messageLoader, TrackedEventRegistry trackedEventRegistry)
            throws InterruptedException, UnknownHostException, IOException, ParseException {
        return new SwissTimingRaceTrackerImpl(raceID, hostname, port, this, messageLoader, trackedEventRegistry);
    }

    @Override
    public SailMasterTransceiver createSailMasterTransceiver() {
        return new SailMasterTransceiverImpl();
    }

    @Override
    public SailMasterMessage createMessage(String message, Long sequenceNumber) {
        return new SailMasterMessageImpl(message, sequenceNumber);
    }
    
    @Override
    public Race createRace(String raceId, String description, TimePoint startTime) {
    	return new RaceImpl(raceId, description, startTime);
    }

    
}
