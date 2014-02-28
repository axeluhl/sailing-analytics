package com.sap.sailing.domain.swisstimingadapter.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import com.sap.sailing.domain.swisstimingadapter.MessageType;
import com.sap.sailing.domain.swisstimingadapter.impl.SailMasterMessageImpl;
import com.sap.sailing.domain.swisstimingadapter.impl.SailMasterTransceiverImpl;

public class SailMasterDummy implements Runnable {
    private static final Logger logger = Logger.getLogger(SailMasterDummy.class.getName());
    
    public static final byte STX = 0x02;
    public static final byte ETX = 0x03;
    
    private final int port;
    
    private Socket socket;
    
    private boolean stopped;
    
    private final SailMasterTransceiverImpl transceiver = new SailMasterTransceiverImpl();
    
    public SailMasterDummy(int port) {
        this.port = port;
    }
    
    public void run() {
        try {
            ServerSocket listenOn = new ServerSocket(port);
            stopped = false;
            while (!stopped) {
                socket = listenOn.accept();
                synchronized (this) {
                    this.notifyAll();
                }
                processRequests();
                if (stopped) {
                    socket.close();
                }
            }
            listenOn.close();
            logger.info("Server stopped. Thread ending.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processRequests() throws IOException {
        InputStream is = socket.getInputStream();
        String message = transceiver.receiveMessage(is);
        while (message != null) {
            respondToMessage(message, socket.getOutputStream());
            if (stopped) {
                message = null;
            } else {
                message = transceiver.receiveMessage(is);
            }
        }
    }
    
    private void respondToMessage(String message, OutputStream os) throws IOException {
        SailMasterMessageImpl smMessage = new SailMasterMessageImpl(message);
        String[] sections = smMessage.getSections();
        if ((MessageType.RAC.name()+"?").equals(sections[0])) {
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
            if (socket == null) {
                // wait a while to get notified about socket being set; could be a thread startup / synchronization
                // problem
                this.wait(/* timeout in milliseconds */1000l);
            }
        }
        if (socket == null) {
            throw new IllegalStateException(SailMasterDummy.class.getSimpleName()+" must be running (run() method must be executing)");
        }
        transceiver.sendMessage(message, socket.getOutputStream());
    }
}
