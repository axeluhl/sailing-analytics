package com.sap.sailing.domain.swisstimingadapter;

import com.sap.sailing.domain.swisstimingadapter.impl.SwissTimingFactoryImpl;

public interface SwissTimingFactory {
    SwissTimingFactory INSTANCE = new SwissTimingFactoryImpl();
    
    SwissTimingMessageParser createMessageParser();
    
    SailMasterConnector createSailMasterConnector(String host, int port);

    SwissTimingConfiguration createSwissTimingConfiguration(String name, String hostname, int port);
}
