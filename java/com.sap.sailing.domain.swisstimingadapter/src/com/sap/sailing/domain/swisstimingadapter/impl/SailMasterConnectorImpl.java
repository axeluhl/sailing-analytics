package com.sap.sailing.domain.swisstimingadapter.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.swisstimingadapter.Course;
import com.sap.sailing.domain.swisstimingadapter.Mark;
import com.sap.sailing.domain.swisstimingadapter.Race;
import com.sap.sailing.domain.swisstimingadapter.SailMasterConnector;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;
import com.sap.sailing.domain.swisstimingadapter.StartList;
import com.sap.sailing.util.Util.Pair;

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

    @Override
    public Iterable<Race> getRaces() throws UnknownHostException, IOException {
        SailMasterMessage response = sendRequestAndGetResponse("RaceId");
        String[] sections = response.getSections();
        if (!sections[0].equals("RaceId")) {
            throw new RuntimeException("Expected a RaceId response for a RaceId request but got "+sections[0]);
        }
        List<Race> result = new ArrayList<Race>();
        for (int i=1; i<sections.length; i++) {
            String[] idAndDescription = sections[i].split(",");
            result.add(new RaceImpl(idAndDescription[1], idAndDescription[0]));
        }
        return result;
    }

    @Override
    public Course getCourse(String raceID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StartList getStartList(String raceID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimePoint getStartTime(String raceID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Mark, Pair<Long, String>> getDeltaClockAtMark(String raceID, int markIndex) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getDistanceToMarkInMeters(String raceID, int markIndex, String boatID) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getCurrentBoatSpeedInMetersPerSecond(String raceID, String boatID) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getAverageBoatSpeedInMetersPerSecond(String raceID, String leg, String boatID) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getDistanceBetweenBoatsInMeters(String raceID, String boatID1, String boatID2) {
        // TODO Auto-generated method stub
        return 0;
    }
}
