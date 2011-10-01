package com.sap.sailing.domain.swisstimingadapter.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.swisstimingadapter.Competitor;
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
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
    
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
        assertResponseType("RaceId", sections[0]);
        List<Race> result = new ArrayList<Race>();
        for (int i=1; i<sections.length; i++) {
            String[] idAndDescription = sections[i].split(",");
            result.add(new RaceImpl(idAndDescription[1], idAndDescription[0]));
        }
        return result;
    }

    @Override
    public Course getCourse(String raceID) throws UnknownHostException, IOException {
        SailMasterMessage response = sendRequestAndGetResponse("CourseConfig|"+raceID);
        String[] sections = response.getSections();
        assertResponseType("CourseConfig", sections[0]);
        assertRaceID(raceID, sections[1]);
        List<Mark> marks = new ArrayList<Mark>();
        for (int i=2; i<sections.length; i++) {
            String[] markDetails = sections[i].split(",");
            marks.add(new MarkImpl(markDetails[1], Integer.valueOf(markDetails[0]), Arrays.asList(markDetails).subList(2, markDetails.length)));
        }
        return new CourseImpl(raceID, marks);
    }
    
    private String prefixTimeWithISOToday(String time) {
        return dateFormat.format(new Date()).substring(0, "yyyy-mm-ddT".length())+time;
    }

    private void assertRaceID(String raceID, String section) {
        if (!section.equals(raceID)) {
            throw new RuntimeException("Expected race ID "+raceID+" but received "+section);
        }
    }

    private void assertMarkIndex(int markIndex, String section) {
        if (Integer.valueOf(section).intValue() != markIndex) {
            throw new RuntimeException("Expected marker index " + markIndex + " in response but received " + section);
        }
    }

    private void assertBoatID(String boatID, String section) {
        if (!section.equals(boatID)) {
            throw new RuntimeException("Expected boat ID " + boatID + " in response but received " + section);
        }
    }

    private void assertResponseType(String responseType, String section) {
        if (!section.equals(responseType)) {
            throw new RuntimeException("Expected a "+responseType+" response for a "+responseType+" request but got "+section);
        }
    }

    private void assertLeg(String leg, String section) {
        if (!section.equals(leg)) {
            throw new RuntimeException("Expected leg " + leg + " in response but received " + section);
        }
    }

    @Override
    public StartList getStartList(String raceID) throws UnknownHostException, IOException {
        SailMasterMessage response = sendRequestAndGetResponse("StartList|"+raceID);
        String[] sections = response.getSections();
        assertResponseType("Startlist", sections[0]);
        assertRaceID(raceID, sections[1]);
        ArrayList<Competitor> competitors = new ArrayList<Competitor>();
        for (int i=2; i<sections.length; i++) {
            String[] competitorDetails = sections[i].split(",");
            competitors.add(new CompetitorImpl(competitorDetails[0], competitorDetails[1], competitorDetails[2]));
        }
        return new StartListImpl(raceID, competitors);
    }

    @Override
    public TimePoint getStartTime(String raceID) throws UnknownHostException, IOException, ParseException {
        SailMasterMessage response = sendRequestAndGetResponse("RaceTime|"+raceID);
        String[] sections = response.getSections();
        assertResponseType("RaceTime", sections[0]);
        assertRaceID(raceID, sections[1]);
        return new MillisecondsTimePoint(dateFormat.parse(prefixTimeWithISOToday(sections[2])));
    }

    @Override
    public Map<Integer, Pair<TimePoint, String>> getDeltaClockAtMark(String raceID, int markIndex)
            throws UnknownHostException, IOException, NumberFormatException, ParseException {
        SailMasterMessage response = sendRequestAndGetResponse("ClockAtMark|"+raceID+"|"+markIndex);
        String[] sections = response.getSections();
        assertResponseType("ClockAtMark", sections[0]);
        assertRaceID(raceID, sections[1]);
        Map<Integer, Pair<TimePoint, String>> result = new HashMap<Integer, Pair<TimePoint,String>>();
        for (int i=2; i<sections.length; i+=2) {
            String[] markTimeDetail = sections[i+1].split(",");
            result.put(Integer.valueOf(sections[i]), new Pair<TimePoint, String>(
                    new MillisecondsTimePoint(dateFormat.parse(prefixTimeWithISOToday(markTimeDetail[0]))), markTimeDetail[1]));
        }
        return result;
    }

    @Override
    public double getDistanceToMarkInMeters(String raceID, int markIndex, String boatID) throws UnknownHostException, IOException {
        SailMasterMessage response = sendRequestAndGetResponse("DistToMark|"+raceID+"|"+markIndex+"|"+boatID);
        String[] sections = response.getSections();
        assertResponseType("DistToMark", sections[0]);
        assertRaceID(raceID, sections[1]);
        assertMarkIndex(markIndex, sections[2]);
        assertBoatID(boatID, sections[3]);
        return Double.valueOf(sections[4]);
    }

    @Override
    public double getCurrentBoatSpeedInMetersPerSecond(String raceID, String boatID) throws UnknownHostException, IOException {
        SailMasterMessage response = sendRequestAndGetResponse("CurrentBoatSpeed|"+raceID+"|"+boatID);
        String[] sections = response.getSections();
        assertResponseType("CurrentBoatSpeed", sections[0]);
        assertRaceID(raceID, sections[1]);
        assertBoatID(boatID, sections[2]);
        return Double.valueOf(sections[3]);
    }

    @Override
    public double getAverageBoatSpeedInMetersPerSecond(String raceID, String leg, String boatID) throws UnknownHostException, IOException {
        SailMasterMessage response = sendRequestAndGetResponse("AverageBoatSpeed|"+raceID+"|"+leg+"|"+boatID);
        String[] sections = response.getSections();
        assertResponseType("AverageBoatSpeed", sections[0]);
        assertRaceID(raceID, sections[1]);
        assertLeg(leg, sections[2]);
        assertBoatID(boatID, sections[3]);
        return Double.valueOf(sections[4]);
    }

    @Override
    public double getDistanceBetweenBoatsInMeters(String raceID, String boatID1, String boatID2) throws UnknownHostException, IOException {
        SailMasterMessage response = sendRequestAndGetResponse("DistBetweenBoats|"+raceID+"|"+boatID1+"|"+boatID2);
        String[] sections = response.getSections();
        assertResponseType("DistBetweenBoats", sections[0]);
        assertRaceID(raceID, sections[1]);
        assertBoatID(boatID1, sections[2]);
        assertBoatID(boatID2, sections[3]);
        return Double.valueOf(sections[4]);
    }
}
