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
        SailMasterMessage response = sendRequestAndGetResponse("RAC?");
        String[] sections = response.getSections();
        assertResponseType("RAC!", sections[0]);
        int count = Integer.valueOf(sections[1]);
        List<Race> result = new ArrayList<Race>();
        for (int i=0; i<count; i++) {
            String[] idAndDescription = sections[2+i].split(";");
            result.add(new RaceImpl(idAndDescription[1], idAndDescription[0]));
        }
        return result;
    }

    @Override
    public Course getCourse(String raceID) throws UnknownHostException, IOException {
        SailMasterMessage response = sendRequestAndGetResponse("CCG?|"+raceID);
        String[] sections = response.getSections();
        assertResponseType("CCG!", sections[0]);
        assertRaceID(raceID, sections[1]);
        int count = Integer.valueOf(sections[2]);
        List<Mark> marks = new ArrayList<Mark>();
        for (int i=0; i<count; i++) {
            String[] markDetails = sections[3+i].split(";");
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
        SailMasterMessage response = sendRequestAndGetResponse("STL?|"+raceID);
        String[] sections = response.getSections();
        assertResponseType("STL!", sections[0]);
        assertRaceID(raceID, sections[1]);
        int count = Integer.valueOf(sections[2]);
        ArrayList<Competitor> competitors = new ArrayList<Competitor>();
        for (int i=0; i<count; i++) {
            String[] competitorDetails = sections[3+i].split(";");
            competitors.add(new CompetitorImpl(competitorDetails[0], competitorDetails[1], competitorDetails[2]));
        }
        return new StartListImpl(raceID, competitors);
    }

    @Override
    public TimePoint getStartTime(String raceID) throws UnknownHostException, IOException, ParseException {
        SailMasterMessage response = sendRequestAndGetResponse("STT?|"+raceID);
        String[] sections = response.getSections();
        assertResponseType("STT!", sections[0]);
        assertRaceID(raceID, sections[1]);
        return new MillisecondsTimePoint(dateFormat.parse(prefixTimeWithISOToday(sections[2])));
    }

    @Override
    public Map<Integer, Pair<TimePoint, String>> getDeltaClockAtMark(String raceID)
            throws UnknownHostException, IOException, NumberFormatException, ParseException {
        SailMasterMessage response = sendRequestAndGetResponse("CAM?|"+raceID);
        String[] sections = response.getSections();
        assertResponseType("CAM!", sections[0]);
        assertRaceID(raceID, sections[1]);
        int count = Integer.valueOf(sections[2]);
        Map<Integer, Pair<TimePoint, String>> result = new HashMap<Integer, Pair<TimePoint,String>>();
        for (int i=0; i<count; i++) {
            String[] markTimeDetail = sections[3+i].split(";");
            result.put(Integer.valueOf(markTimeDetail[0]), new Pair<TimePoint, String>(
                    new MillisecondsTimePoint(dateFormat.parse(prefixTimeWithISOToday(markTimeDetail[1]))), markTimeDetail[2]));
        }
        return result;
    }

    @Override
    public double getDistanceToMarkInMeters(String raceID, int markIndex, String boatID) throws UnknownHostException, IOException {
        SailMasterMessage response = sendRequestAndGetResponse("DTM?|"+raceID+"|"+markIndex+"|"+boatID);
        String[] sections = response.getSections();
        assertResponseType("DTM!", sections[0]);
        assertRaceID(raceID, sections[1]);
        assertMarkIndex(markIndex, sections[2]);
        assertBoatID(boatID, sections[3]);
        return Double.valueOf(sections[4]);
    }

    @Override
    public double getCurrentBoatSpeedInMetersPerSecond(String raceID, String boatID) throws UnknownHostException, IOException {
        SailMasterMessage response = sendRequestAndGetResponse("CBS?|"+raceID+"|"+boatID);
        String[] sections = response.getSections();
        assertResponseType("CBS!", sections[0]);
        assertRaceID(raceID, sections[1]);
        assertBoatID(boatID, sections[2]);
        return Double.valueOf(sections[3]);
    }

    @Override
    public double getDistanceBetweenBoatsInMeters(String raceID, String boatID1, String boatID2) throws UnknownHostException, IOException {
        SailMasterMessage response = sendRequestAndGetResponse("DBB?|"+raceID+"|"+boatID1+"|"+boatID2);
        String[] sections = response.getSections();
        assertResponseType("DBB!", sections[0]);
        assertRaceID(raceID, sections[1]);
        assertBoatID(boatID1, sections[2]);
        assertBoatID(boatID2, sections[3]);
        return Double.valueOf(sections[4]);
    }

    @Override
    public double getAverageBoatSpeedInMetersPerSecond(String raceID, String leg, String boatID) throws UnknownHostException, IOException {
        SailMasterMessage response = sendRequestAndGetResponse("ABS?|"+raceID+"|"+leg+"|"+boatID);
        String[] sections = response.getSections();
        assertResponseType("ABS!", sections[0]);
        assertRaceID(raceID, sections[1]);
        assertLeg(leg, sections[2]);
        assertBoatID(boatID, sections[3]);
        return Double.valueOf(sections[4]);
    }
    
    @Override
    public Map<Integer, Pair<Integer, Long>> getMarkPassingTimesInMillisecondsSinceStart(String raceID, String boatID)
            throws UnknownHostException, IOException {
        SailMasterMessage response = sendRequestAndGetResponse("TMD?|"+raceID+"|"+boatID);
        String[] sections = response.getSections();
        assertResponseType("ABS!", sections[0]);
        assertRaceID(raceID, sections[1]);
        assertBoatID(boatID, sections[2]);
        int count = Integer.valueOf(sections[3]);
        Map<Integer, Pair<Integer, Long>> result = new HashMap<Integer, Pair<Integer, Long>>();
        for (int i=0; i<count; i++) {
            String[] markTimeDetail = sections[4+i].split(";");
            String[] timeDetail = markTimeDetail[2].split(":");
            long millisecondsSinceStart = 1000 * (Integer.valueOf(timeDetail[2]) + 60 * Integer.valueOf(timeDetail[1]) + 3600 * Integer
                    .valueOf(timeDetail[0]));
            result.put(Integer.valueOf(markTimeDetail[0]), new Pair<Integer, Long>(
                    Integer.valueOf(markTimeDetail[1]), millisecondsSinceStart));
        }
        return result;
    }

}
