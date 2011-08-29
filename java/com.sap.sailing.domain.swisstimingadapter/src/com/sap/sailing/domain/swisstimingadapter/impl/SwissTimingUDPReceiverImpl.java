package com.sap.sailing.domain.swisstimingadapter.impl;

import java.net.SocketException;

import com.sap.sailing.domain.swisstimingadapter.SwissTimingMessage;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingMessageListener;
import com.sap.sailing.udpconnector.UDPMessageParser;
import com.sap.sailing.udpconnector.UDPReceiver;

public class SwissTimingUDPReceiverImpl extends UDPReceiver<SwissTimingMessage, SwissTimingMessageListener> {
    
    private final SwissTimingMessageParserImpl parser;

    public SwissTimingUDPReceiverImpl(int port) throws SocketException {
        super(port);
        parser = new SwissTimingMessageParserImpl();
    }
    
    @Override
    protected UDPMessageParser<SwissTimingMessage> getParser() {
        return parser;
    }

}
