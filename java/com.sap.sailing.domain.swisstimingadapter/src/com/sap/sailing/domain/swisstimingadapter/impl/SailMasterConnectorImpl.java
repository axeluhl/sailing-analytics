package com.sap.sailing.domain.swisstimingadapter.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.sap.sailing.domain.swisstimingadapter.SailMasterConnector;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;

public class SailMasterConnectorImpl extends SailMasterTransceiver implements SailMasterConnector {
    private final String host;
    private final int port;
    private Socket socket;
    
    public SailMasterConnectorImpl(String host, int port) {
        super();
        this.host = host;
        this.port = port;
    }
    
    public SailMasterMessage sendRequestAndGetResponse(String requestMessage) throws UnknownHostException, IOException {
        ensureSocketIsOpen();
        OutputStream os = socket.getOutputStream();
        sendMessage(requestMessage, os);
        return new SailMasterMessageImpl(receiveMessage(socket.getInputStream()));
    }

    private void ensureSocketIsOpen() throws UnknownHostException, IOException {
        if (socket == null) {
            socket = new Socket(host, port);
        }
    }
}
