package com.sap.sailing.domain.swisstimingadapter.impl;

import com.sap.sailing.domain.swisstimingadapter.SailMasterConnector;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingConfiguration;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingMessageParser;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingRaceTracker;
import com.sap.sailing.domain.tracking.WindStore;

public class SwissTimingFactoryImpl implements SwissTimingFactory {

    @Override
    public SwissTimingMessageParser createMessageParser() {
        return new SwissTimingMessageParserImpl();
    }

    @Override
    public SailMasterConnector createSailMasterConnector(String host, int port) {
        return new SailMasterConnectorImpl(host, port);
    }

    @Override
    public SwissTimingConfiguration createSwissTimingConfiguration(String name, String hostname, int port) {
        return new SwissTimingConfigurationImpl(name, hostname, port);
    }

    @Override
    public SwissTimingRaceTracker createRaceTracker(String raceID, String hostname, int port, WindStore windStore) {
        return new SwissTimingRaceTrackerImpl(raceID, hostname, port, this);
    }
    
}
