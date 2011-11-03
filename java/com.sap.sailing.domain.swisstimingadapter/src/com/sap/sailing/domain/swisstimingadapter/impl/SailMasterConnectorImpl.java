package com.sap.sailing.domain.swisstimingadapter.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MeterDistance;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.swisstimingadapter.Competitor;
import com.sap.sailing.domain.swisstimingadapter.Course;
import com.sap.sailing.domain.swisstimingadapter.Fix;
import com.sap.sailing.domain.swisstimingadapter.Mark;
import com.sap.sailing.domain.swisstimingadapter.MessageType;
import com.sap.sailing.domain.swisstimingadapter.Race;
import com.sap.sailing.domain.swisstimingadapter.RaceStatus;
import com.sap.sailing.domain.swisstimingadapter.SailMasterConnector;
import com.sap.sailing.domain.swisstimingadapter.SailMasterListener;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;
import com.sap.sailing.domain.swisstimingadapter.StartList;
import com.sap.sailing.domain.swisstimingadapter.TrackerType;
import com.sap.sailing.util.Util.Pair;

/**
 * Implements the connector to the SwissTiming Sail Master system. It uses a hostname and port number
 * to establish the connecting via TCP. The connector offers a number of explicit service request
 * methods. Additionally, the connector can receive "spontaneous" events sent by the sail master
 * system. Clients can register for those spontaneous events (see {@link #addSailMasterListener}).
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class SailMasterConnectorImpl extends SailMasterTransceiver implements SailMasterConnector, Runnable {
    private static final Logger logger = Logger.getLogger(SailMasterConnectorImpl.class.getName());
    
    private final String host;
    private final int port;
    private Socket socket;
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
    private final Set<SailMasterListener> listeners;
    private final Thread receiverThread;
    private boolean stopped;
    
    private final Map<MessageType, BlockingQueue<SailMasterMessage>> unprocessedMessagesByType;
    
    public SailMasterConnectorImpl(String host, int port) {
        super();
        this.host = host;
        this.port = port;
        this.listeners = new HashSet<SailMasterListener>();
        this.unprocessedMessagesByType = new HashMap<MessageType, BlockingQueue<SailMasterMessage>>();
        receiverThread = new Thread(this, "SwissTiming SailMaster Receiver");
        receiverThread.start();
    }
    
    public void run() {
        try {
            while (!stopped) {
                ensureSocketIsOpen();
                try {
                    SailMasterMessage message = new SailMasterMessageImpl(receiveMessage(socket.getInputStream()));
                    if (message.isResponse()) {
                        // this is a response for an explicit request
                        rendevouz(message);
                        if (message.getType() == MessageType._STOPSERVER) {
                            stop();
                        }
                    } else if (message.isEvent()) {
                        // a spontaneous event
                        notifyListeners(message);
                    }
                } catch (SocketException se) {
                    // This occurs if the socket was closed which may mean the connector was stopped. Check in while
                    socket = null;
                }
            }
        } catch (Exception e) {
            logger.throwing(SailMasterConnectorImpl.class.getName(), "run", e);
        }
        logger.info("Stopping Sail Master connector thread");
    }
    
    @Override
    public SailMasterMessage receiveMessage(MessageType type) throws InterruptedException {
        BlockingQueue<SailMasterMessage> blockingQueue = getBlockingQueue(type);
        SailMasterMessage result = blockingQueue.take();
        return result;
    }

    private synchronized BlockingQueue<SailMasterMessage> getBlockingQueue(MessageType type) {
        BlockingQueue<SailMasterMessage> blockingQueue;
        blockingQueue = unprocessedMessagesByType.get(type);
        if (blockingQueue == null) {
            blockingQueue = new LinkedBlockingQueue<SailMasterMessage>();
            unprocessedMessagesByType.put(type, blockingQueue);
        }
        return blockingQueue;
    }
    
    private void rendevouz(SailMasterMessage message) {
        BlockingQueue<SailMasterMessage> blockingQueue = getBlockingQueue(message.getType());
        blockingQueue.offer(message);
    }

    private void notifyListeners(SailMasterMessage message) throws ParseException {
        switch (message.getType()) {
        case RPD:
            notifyListenersRPD(message);
            break;
        }
    }

    private void notifyListenersRPD(SailMasterMessage message) throws ParseException {
        assert message.getType() == MessageType.RPD;
        String[] sections = message.getSections();
        String raceID = sections[1];
        RaceStatus status = RaceStatus.values()[Integer.valueOf(sections[2])];
        TimePoint timePoint = new MillisecondsTimePoint(dateFormat.parse(prefixTimeWithISOToday(sections[3])));
        TimePoint startTimeEstimatedStartTime = sections[4].trim().length() == 0 ? null : new MillisecondsTimePoint(
                dateFormat.parse(prefixTimeWithISOToday(sections[4])));
        Long millisecondsSinceRaceStart = sections[5].trim().length() == 0 ? null : parseHHMMSSToMilliseconds(sections[5]);
        Integer nextMarkIndexForLeader = sections[6].trim().length() == 0 ? null : Integer.valueOf(sections[6]);
        Distance distanceToNextMarkForLeader = sections[7].trim().length() == 0 ? null : new MeterDistance(Double.valueOf(sections[7]));
        int count = Integer.valueOf(sections[8]);
        Collection<Fix> fixes = new ArrayList<Fix>();
        for (int i=0; i<count; i++) {
            int fixDetailIndex = 0;
            String[] fixSections = sections[9+i].split(";");
            if (fixSections.length > 2) {
                String boatID = fixSections[fixDetailIndex++];
                TrackerType trackerType = TrackerType.values()[Integer.valueOf(fixSections[fixDetailIndex++])];
                Long ageOfDataInMilliseconds = 1000l * Long.valueOf(fixSections[fixDetailIndex++]);
                Position position = new DegreePosition(Double.valueOf(fixSections[fixDetailIndex++]),
                        Double.valueOf(fixSections[fixDetailIndex++]));
                Double speedOverGroundInKnots = Double.valueOf(fixSections[fixDetailIndex++]);
                Speed averageSpeedOverGround = null;
                if (trackerType == TrackerType.COMPETITOR) {
                    averageSpeedOverGround = fixSections[fixDetailIndex].trim().length() == 0 ? null
                            : new KnotSpeedImpl(Double.valueOf(fixSections[fixDetailIndex]));
                    fixDetailIndex++;
                }
                Speed velocityMadeGood = fixSections[fixDetailIndex].trim().length() == 0 ? null : new KnotSpeedImpl(
                        Double.valueOf(fixSections[fixDetailIndex]));
                fixDetailIndex++;
                SpeedWithBearing speed = new KnotSpeedWithBearingImpl(speedOverGroundInKnots, new DegreeBearingImpl(
                        Double.valueOf(fixSections[fixDetailIndex++])));
                Integer nextMarkIndex = fixSections.length <= fixDetailIndex
                        || fixSections[fixDetailIndex].trim().length() == 0 ? null : Integer
                        .valueOf(fixSections[fixDetailIndex]);
                fixDetailIndex++;
                Integer rank = fixSections.length <= fixDetailIndex || fixSections[fixDetailIndex].trim().length() == 0 ? null
                        : Integer.valueOf(fixSections[fixDetailIndex]);
                fixDetailIndex++;
                Distance distanceToLeader = fixSections.length <= fixDetailIndex
                        || fixSections[fixDetailIndex].trim().length() == 0 ? null : new MeterDistance(
                        Double.valueOf(fixSections[fixDetailIndex]));
                fixDetailIndex++;
                Distance distanceToNextMark = fixSections.length <= fixDetailIndex
                        || fixSections[fixDetailIndex].trim().length() == 0 ? null : new MeterDistance(
                        Double.valueOf(fixSections[fixDetailIndex]));
                fixDetailIndex++;
                fixes.add(new FixImpl(boatID, trackerType, ageOfDataInMilliseconds, position, speed, nextMarkIndex,
                        rank,
                        averageSpeedOverGround, velocityMadeGood, distanceToLeader, distanceToNextMark));
            }
        }
        for (SailMasterListener listener : listeners) {
            listener.receivedRacePositionData(raceID, status, timePoint, startTimeEstimatedStartTime, millisecondsSinceRaceStart,
                    nextMarkIndexForLeader, distanceToNextMarkForLeader, fixes);
        }
    }

    public void stop() throws IOException {
        stopped = true;
        socket.close();
        socket = null;
    }
    
    @Override
    public void addSailMasterListener(SailMasterListener listener) {
        listeners.add(listener);
    }
    
    @Override
    public void removeSailMasterListener(SailMasterListener listener) {
        listeners.remove(listener);
    }
    
    public SailMasterMessage sendRequestAndGetResponse(MessageType messageType, String... args) throws UnknownHostException, IOException, InterruptedException {
        ensureSocketIsOpen();
        OutputStream os = socket.getOutputStream();
        StringBuilder requestMessage = new StringBuilder();
        requestMessage.append(messageType.name());
        requestMessage.append('?');
        for (String arg : args) {
            requestMessage.append('|');
            requestMessage.append(arg);
        }
        sendMessage(requestMessage.toString(), os);
        return receiveMessage(messageType);
    }

    private synchronized void ensureSocketIsOpen() throws UnknownHostException, IOException {
        if (socket == null) {
            socket = new Socket(host, port);
        }
    }

    @Override
    public Iterable<Race> getRaces() throws UnknownHostException, IOException, InterruptedException {
        SailMasterMessage response = sendRequestAndGetResponse(MessageType.RAC);
        String[] sections = response.getSections();
        assertResponseType(MessageType.RAC, response);
        int count = Integer.valueOf(sections[1]);
        List<Race> result = new ArrayList<Race>();
        for (int i=0; i<count; i++) {
            String[] idAndDescription = sections[2+i].split(";");
            result.add(new RaceImpl(idAndDescription[1], idAndDescription[0]));
        }
        return result;
    }

    @Override
    public Course getCourse(String raceID) throws UnknownHostException, IOException, InterruptedException {
        SailMasterMessage response = sendRequestAndGetResponse(MessageType.CCG, raceID);
        String[] sections = response.getSections();
        assertResponseType(MessageType.CCG, response);
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

    private void assertResponseType(MessageType responseType, SailMasterMessage message) {
        if (!message.isResponse()) {
            throw new RuntimeException("Expected a response message but got "+message);
        }
        if (message.getType() != responseType) {
            throw new RuntimeException("Expected a "+responseType+" response for a "+responseType+" request but got "+message.getType());
        }
    }

    private void assertLeg(String leg, String section) {
        if (!section.equals(leg)) {
            throw new RuntimeException("Expected leg " + leg + " in response but received " + section);
        }
    }

    @Override
    public StartList getStartList(String raceID) throws UnknownHostException, IOException, InterruptedException {
        SailMasterMessage response = sendRequestAndGetResponse(MessageType.STL, raceID);
        String[] sections = response.getSections();
        assertResponseType(MessageType.STL, response);
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
    public TimePoint getStartTime(String raceID) throws UnknownHostException, IOException, ParseException, InterruptedException {
        SailMasterMessage response = sendRequestAndGetResponse(MessageType.STT, raceID);
        String[] sections = response.getSections();
        assertResponseType(MessageType.STT, response);
        assertRaceID(raceID, sections[1]);
        return new MillisecondsTimePoint(dateFormat.parse(prefixTimeWithISOToday(sections[2])));
    }

    @Override
    public Map<Integer, Pair<TimePoint, String>> getDeltaClockAtMark(String raceID)
            throws UnknownHostException, IOException, NumberFormatException, ParseException, InterruptedException {
        SailMasterMessage response = sendRequestAndGetResponse(MessageType.CAM, raceID);
        String[] sections = response.getSections();
        assertResponseType(MessageType.CAM, response);
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
    public double getDistanceToMarkInMeters(String raceID, int markIndex, String boatID) throws UnknownHostException, IOException, InterruptedException {
        SailMasterMessage response = sendRequestAndGetResponse(MessageType.DTM, raceID, ""+markIndex, boatID);
        String[] sections = response.getSections();
        assertResponseType(MessageType.DTM, response);
        assertRaceID(raceID, sections[1]);
        assertMarkIndex(markIndex, sections[2]);
        assertBoatID(boatID, sections[3]);
        return Double.valueOf(sections[4]);
    }

    @Override
    public double getCurrentBoatSpeedInMetersPerSecond(String raceID, String boatID) throws UnknownHostException, IOException, InterruptedException {
        SailMasterMessage response = sendRequestAndGetResponse(MessageType.CBS, raceID, boatID);
        String[] sections = response.getSections();
        assertResponseType(MessageType.CBS, response);
        assertRaceID(raceID, sections[1]);
        assertBoatID(boatID, sections[2]);
        return Double.valueOf(sections[3]);
    }

    @Override
    public double getDistanceBetweenBoatsInMeters(String raceID, String boatID1, String boatID2) throws UnknownHostException, IOException, InterruptedException {
        SailMasterMessage response = sendRequestAndGetResponse(MessageType.DBB, raceID, boatID1, boatID2);
        String[] sections = response.getSections();
        assertResponseType(MessageType.DBB, response);
        assertRaceID(raceID, sections[1]);
        assertBoatID(boatID1, sections[2]);
        assertBoatID(boatID2, sections[3]);
        return Double.valueOf(sections[4]);
    }

    @Override
    public double getAverageBoatSpeedInMetersPerSecond(String raceID, String leg, String boatID) throws UnknownHostException, IOException, InterruptedException {
        SailMasterMessage response = sendRequestAndGetResponse(MessageType.ABS, raceID, leg, boatID);
        String[] sections = response.getSections();
        assertResponseType(MessageType.ABS, response);
        assertRaceID(raceID, sections[1]);
        assertLeg(leg, sections[2]);
        assertBoatID(boatID, sections[3]);
        return Double.valueOf(sections[4]);
    }
    
    @Override
    public Map<Integer, Pair<Integer, Long>> getMarkPassingTimesInMillisecondsSinceRaceStart(String raceID, String boatID)
            throws UnknownHostException, IOException, InterruptedException {
        SailMasterMessage response = sendRequestAndGetResponse(MessageType.TMD, raceID, boatID);
        String[] sections = response.getSections();
        assertResponseType(MessageType.TMD, response);
        assertRaceID(raceID, sections[1]);
        assertBoatID(boatID, sections[2]);
        int count = Integer.valueOf(sections[3]);
        Map<Integer, Pair<Integer, Long>> result = new HashMap<Integer, Pair<Integer, Long>>();
        for (int i=0; i<count; i++) {
            String[] markTimeDetail = sections[4+i].split(";");
            long millisecondsSinceStart = parseHHMMSSToMilliseconds(markTimeDetail[2]);
            result.put(Integer.valueOf(markTimeDetail[0]), new Pair<Integer, Long>(
                    Integer.valueOf(markTimeDetail[1]), millisecondsSinceStart));
        }
        return result;
    }

    private long parseHHMMSSToMilliseconds(String hhmmss) {
        String[] timeDetail = hhmmss.split(":");
        long millisecondsSinceStart = 1000 * (Integer.valueOf(timeDetail[2]) + 60 * Integer.valueOf(timeDetail[1]) + 3600 * Integer
                .valueOf(timeDetail[0]));
        return millisecondsSinceStart;
    }
    
}
