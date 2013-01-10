package com.sap.sailing.domain.swisstimingadapter.impl;

import java.io.IOException;
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
    
    /**
     * Launches a listener and dumps messages received to the console
     * @param args 0: port to listen on
     *  
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        SwissTimingUDPReceiverImpl receiver = new SwissTimingUDPReceiverImpl(Integer.valueOf(args[0]));
        receiver.addListener(new SwissTimingMessageListener() {
            @Override
            public void received(SwissTimingMessage message) {
                System.out.println(message);
            }
        }, /* validMessagesOnly */ false);
        receiver.run();
    }

}
