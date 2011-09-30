package com.sap.sailing.domain.swisstimingadapter.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class SailMasterConnector extends SailMasterTransceiver {
    private final String host;
    private final int port;
    private Socket socket;
    
    public SailMasterConnector(String host, int port) {
        super();
        this.host = host;
        this.port = port;
    }
    
    public String sendRequestAndGetResponse(String requestMessage) throws UnknownHostException, IOException {
        ensureSocketIsOpen();
        OutputStream os = socket.getOutputStream();
        sendMessage(requestMessage, os);
        return receiveMessage(socket.getInputStream());
    }

    private void ensureSocketIsOpen() throws UnknownHostException, IOException {
        if (socket == null) {
            socket = new Socket(host, port);
        }
    }
}
