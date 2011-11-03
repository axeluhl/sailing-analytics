package com.sap.sailing.domain.swisstimingadapter.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.sap.sailing.domain.swisstimingadapter.impl.SailMasterMessageImpl;
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
                if (stopped) {
                    s.close();
                }
            }
            listenOn.close();
            System.out.println("Server stopped. Thread ending.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processRequests(Socket s) throws IOException {
        InputStream is = s.getInputStream();
        String message = transceiver.receiveMessage(is);
        while (message != null) {
            respondToMessage(message, s.getOutputStream());
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
        if ("RaceId".equals(sections[0])) {
            transceiver.sendMessage("RaceId|4711,A wonderful test race|4712,Not such a wonderful race", os);
        } else if ("CourseConfig".equals(sections[0])) {
            if (sections[1].equals("4711")) {
                transceiver.sendMessage("CourseConfig|"+sections[1]+"|1,Lee Gate,LG1,LG2|2,Windward,WW1", os);
            } else if (sections[1].equals("4712")) {
                transceiver.sendMessage("CourseConfig|"+sections[1]+"|1,Lee Gate,LG1,LG2|2,Windward,WW1|3,Offset,OS1", os);
            }
        } else if ("StartList".equals(sections[0])) {
            if (sections[1].equals("4711")) {
                transceiver.sendMessage("Startlist|"+sections[1]+"|GER 8414,GER,Polgar/Koy|GER 8140,GER,Schlonski/Bohn", os);
            } else if (sections[1].equals("4712")) {
                transceiver.sendMessage("Startlist|"+sections[1]+"|GER 8340,GER,Stanjek/Kleen|GER 8433,GER,Babendererde/Jacobs|GER 8299,GER,Elsner/Schulz", os);
            }
        } else if ("RaceTime".equals(sections[0])) {
            if (sections[1].equals("4711")) {
                transceiver.sendMessage("RaceTime|"+sections[1]+"|10:15:22", os);
            } else if (sections[1].equals("4712")) {
                transceiver.sendMessage("RaceTime|"+sections[1]+"|18:17:23", os);
            }
        } else if ("ClockAtMark".equals(sections[0])) {
            
        } else if ("DistToMark".equals(sections[0])) {
            
        } else if ("CurrentBoatSpeed".equals(sections[0])) {
            
        } else if ("AverageBoatSpeed".equals(sections[0])) {
            
        } else if ("DistBetweenBoats".equals(sections[0])) {
            
        } else if ("RAC?".equals(sections[0])) {
            // Available Races
            transceiver.sendMessage("RAC!|2|4711;A wonderful test race|4712;Not such a wonderful race", os);
        } else if ("CCG?".equals(sections[0])) {
            // Cource Configuration
            if (sections[1].equals("4711")) {
                transceiver.sendMessage("CCG!|"+sections[1]+"|2|1;Lee Gate;LG1;LG2|2;Windward;WW1", os);
            } else if (sections[1].equals("4712")) {
                transceiver.sendMessage("CCG!|"+sections[1]+"|3|1;Lee Gate;LG1;LG2|2;Windward;WW1|3;Offset;OS1", os);
            }
        } else if ("STL?".equals(sections[0])) {
            // Startlist
            if (sections[1].equals("4711")) {
                transceiver.sendMessage("STL!|"+sections[1]+"|2|GER 8414;GER;Polgar/Koy|GER 8140;GER;Schlonski/Bohn", os);
            } else if (sections[1].equals("4712")) {
                transceiver.sendMessage("STL!|"+sections[1]+"|3|GER 8340;GER;Stanjek/Kleen|GER 8433;GER;Babendererde/Jacobs|GER 8299;GER;Elsner/Schulz", os);
            }
        } else if ("STT?".equals(sections[0])) {
            // StartTime
            if (sections[1].equals("4711")) {
                transceiver.sendMessage("STT!|"+sections[1]+"|10:15:22", os);
            } else if (sections[1].equals("4712")) {
                transceiver.sendMessage("STT!|"+sections[1]+"|18:17:23", os);
            }
        } else if ("CAM?".equals(sections[0])) {
            // Clock at Mark/Finish
        } else if ("DTM?".equals(sections[0])) {
            // Distance to Mark
        } else if ("CBS?".equals(sections[0])) {
            // Current Boat Speed
        } else if ("DBB?".equals(sections[0])) {
            // Distance between Boats
        } else if ("ABS?".equals(sections[0])) {
            // Average Boat Speed per Leg
        } else if ("TMD?".equals(sections[0])) {
            // Timing Data
        } else if ("VER?".equals(sections[0])) {
            // Version
            transceiver.sendMessage("VER!|1.0", os);
        } else if ("StopServer".equals(sections[0])) {
            stopped = true;
            transceiver.sendMessage("Server stopped", os);
        } else {
            transceiver.sendMessage("Request not understood", os);
        }
    }
}
