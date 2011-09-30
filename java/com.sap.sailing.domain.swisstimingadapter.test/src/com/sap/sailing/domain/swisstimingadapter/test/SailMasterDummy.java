package com.sap.sailing.domain.swisstimingadapter.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.sap.sailing.domain.swisstimingadapter.impl.SailMasterTransceiver;

public class SailMasterDummy implements Runnable {
    public static final byte STX = 0x02;
    public static final byte ETX = 0x03;
    
    private final int port;
    
    private boolean stopped;
    
    private final SailMasterTransceiver transceiver = new SailMasterTransceiver();
    
    public SailMasterDummy(int port) {
        this.port = port;
    }
    
    public void run() {
        try {
            ServerSocket listenOn = new ServerSocket(port);
            stopped = false;
            while (!stopped) {
                Socket s = listenOn.accept();
                processRequests(s);
            }
            System.out.println("Server stopped. Thread ending.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processRequests(Socket s) throws IOException {
        InputStream is = s.getInputStream();
        String message = transceiver.receiveMessage(is);
        if (message != null) {
            respondToMessage(message, s.getOutputStream());
        }
    }
    
    private void respondToMessage(String message, OutputStream os) throws IOException {
        if ("RaceId".equals(message)) {
            transceiver.sendMessage("RaceId|4711,A wonderful test race|4712,Not such a wonderful race", os);
        } else if ("StopServer".equals(message)) {
            stopped = true;
            transceiver.sendMessage("Server stopped", os);
        } else {
            transceiver.sendMessage("Request not understood", os);
        }
    }
}
