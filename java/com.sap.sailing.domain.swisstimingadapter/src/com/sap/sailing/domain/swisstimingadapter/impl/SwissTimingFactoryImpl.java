package com.sap.sailing.domain.swisstimingadapter.impl;

import com.sap.sailing.domain.swisstimingadapter.SailMasterConnector;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingMessageParser;

public class SwissTimingFactoryImpl implements SwissTimingFactory {

    @Override
    public SwissTimingMessageParser createMessageParser() {
        return new SwissTimingMessageParserImpl();
    }

    @Override
    public SailMasterConnector createSailMasterConnector(String host, int port) {
        return new SailMasterConnectorImpl(host, port);
    }
    
}
