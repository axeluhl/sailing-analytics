package com.sap.sailing.domain.swisstimingadapter.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.swisstimingadapter.MessageType;
import com.sap.sailing.domain.swisstimingadapter.impl.SailMasterMessageImpl;
import com.sap.sailing.domain.swisstimingadapter.impl.SailMasterTransceiverImpl;

public class SailMasterDummy implements Runnable {
    private static final Logger logger = Logger.getLogger(SailMasterDummy.class.getName());
    
    public static final byte STX = 0x02;
    public static final byte ETX = 0x03;
    
    private final int port;
    
    private Set<Socket> sockets;
    
    private boolean stopped;
    
    private final SailMasterTransceiverImpl transceiver = new SailMasterTransceiverImpl();
    
    public SailMasterDummy(int port) {
        this.port = port;
        this.sockets = new HashSet<>();
    }
    
    public void run() {
        try {
            ServerSocket listenOn = new ServerSocket(port);
            stopped = false;
            while (!stopped) {
                final Socket socket = listenOn.accept();
                if (stopped) {
                    // could have been stopped while we were listening on a socket
                    logger.info("Stopping SailMasterDummy");
                    socket.close();
                } else {
                    logger.info("Received connect to SailMasterDummy on port "+port+": "+socket);
                    synchronized (this) {
                        sockets.add(socket);
                        this.notifyAll();
                    }
                    new Thread("SailMasterDummy on port " + port + ", processing requests on " + socket) {
                        @Override
                        public void run() {
                            try {
                                processRequests(socket);
                            } catch (IOException e) {
                                logger.log(Level.SEVERE, "Exception trying to process SailMaster requests", e);
                            }
                        }
                    }.start();
                }
            }
            listenOn.close();
            logger.info("Server stopped. Thread ending.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processRequests(Socket socket) throws IOException {
        InputStream is = socket.getInputStream();
        String message = transceiver.receiveMessage(is);
        while (message != null) {
            respondToMessage(message, socket.getOutputStream());
            if (stopped) {
                message = null;
                synchronized(this) {
                    sockets.remove(socket);
                    socket.close();
                }
            } else {
                message = transceiver.receiveMessage(is);
            }
        }
    }
    
    private void respondToMessage(String message, OutputStream os) throws IOException {
        SailMasterMessageImpl smMessage = new SailMasterMessageImpl(message);
        String[] sections = smMessage.getSections();
        if (MessageType.OPN.name().equals(sections[0])) {
            transceiver.sendMessage("OPN!|OK|123", os);
        } else if (MessageType.LSN.name().equals(sections[0])) {
            transceiver.sendMessage("LSN!|OK", os);
        } else if ((MessageType.RAC.name()+"?").equals(sections[0])) {
            // Available Races
            transceiver.sendMessage("RAC!|2|4711;A wonderful test race|4712;Not such a wonderful race", os);
        } else if ((MessageType.CCG.name()+"?").equals(sections[0])) {
            // Cource Configuration
            if (sections[1].equals("4711")) {
                transceiver.sendMessage("CCG!|"+sections[1]+"|2|1;Lee Gate;LG1;LG2|2;Windward;WW1", os);
            } else if (sections[1].equals("4712")) {
                transceiver.sendMessage("CCG!|"+sections[1]+"|3|1;Lee Gate;LG1;LG2|2;Windward;WW1|3;Offset;OS1", os);
            }
        } else if ((MessageType.STL.name()+"?").equals(sections[0])) {
            // Startlist
            if (sections[1].equals("4711")) {
                transceiver.sendMessage("STL!|"+sections[1]+"|2|GER 8414;GER;Polgar/Koy|GER 8140;GER;Schlonski/Bohn", os);
            } else if (sections[1].equals("4712")) {
                transceiver.sendMessage("STL!|"+sections[1]+"|3|GER 8340;GER;Stanjek/Kleen|GER 8433;GER;Babendererde/Jacobs|GER 8299;GER;Elsner/Schulz", os);
            }
        } else if ((MessageType.STT.name()+"?").equals(sections[0])) {
            // StartTime
            if (sections[1].equals("4711")) {
                transceiver.sendMessage("STT!|"+sections[1]+"|10:15:22", os);
            } else if (sections[1].equals("4712")) {
                transceiver.sendMessage("STT!|"+sections[1]+"|18:17:23", os);
            }
        } else if ((MessageType.CAM.name()+"?").equals(sections[0])) {
            // Clock at Mark/Finish
        } else if ((MessageType.DTM.name()+"?").equals(sections[0])) {
            // Distance to Mark
        } else if ((MessageType.CBS.name()+"?").equals(sections[0])) {
            // Current Boat Speed
        } else if ((MessageType.DBB.name()+"?").equals(sections[0])) {
            // Distance between Boats
        } else if ((MessageType.ABS.name()+"?").equals(sections[0])) {
            // Average Boat Speed per Leg
        } else if ((MessageType.TMD.name()+"?").equals(sections[0])) {
            // Timing Data
        } else if ((MessageType.VER.name()+"?").equals(sections[0])) {
            // Version
            transceiver.sendMessage("VER!|1.0", os);
        } else if ((MessageType._STOPSERVER.name()+"?").equals(sections[0])) {
            stopped = true;
            transceiver.sendMessage(MessageType._STOPSERVER.name()+"!", os);
        } else {
            transceiver.sendMessage("Request not understood", os);
        }
    }
    
    public void sendEvent(String message) throws IOException, InterruptedException {
        synchronized (this) {
            if (sockets.isEmpty()) {
                // wait a while to get notified about socket being set; could be a thread startup / synchronization
                // problem
                logger.info("No sockets found in SailMasterDummy; waiting for a second for one to appear...");
                this.wait(/* timeout in milliseconds */1000l);
            }
        }
        if (sockets.isEmpty()) {
            throw new IllegalStateException(SailMasterDummy.class.getSimpleName()+" must be running (run() method must be executing)");
        }
        synchronized (this) {
            for (Socket socket : sockets) {
                logger.info("Forwarding message "+message+" to socket "+socket);
                transceiver.sendMessage(message, socket.getOutputStream());
            }
        }
    }
}
