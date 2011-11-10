package com.sap.sailing.domain.swisstimingadapter;

import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.swisstimingadapter.impl.SwissTimingFactoryImpl;
import com.sap.sailing.domain.tracking.WindStore;

public interface SwissTimingFactory {
    SwissTimingFactory INSTANCE = new SwissTimingFactoryImpl();
    
    SwissTimingMessageParser createMessageParser();
    
    SailMasterConnector createSailMasterConnector(String hostname, int port) throws InterruptedException;
    
    SailMasterTransceiver createSailMasterTransceiver();

    SwissTimingConfiguration createSwissTimingConfiguration(String name, String hostname, int port);

    SwissTimingRaceTracker createRaceTracker(String raceID, String hostname, int port, WindStore windStore) throws InterruptedException;

    Race createRace(String raceId, String description, TimePoint startTime);

    SailMasterMessage createMessage(String message, Long sequenceNumber);
}
