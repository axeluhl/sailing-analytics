package com.sap.sailing.domain.swisstimingadapter.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
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
import com.sap.sailing.domain.base.impl.KilometersPerHourSpeedImpl;
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
import com.sap.sailing.domain.swisstimingadapter.RaceSpecificMessageLoader;
import com.sap.sailing.domain.swisstimingadapter.RaceStatus;
import com.sap.sailing.domain.swisstimingadapter.SailMasterConnector;
import com.sap.sailing.domain.swisstimingadapter.SailMasterListener;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;
import com.sap.sailing.domain.swisstimingadapter.StartList;
import com.sap.sailing.domain.swisstimingadapter.TrackerType;
import com.sap.sailing.util.Util.Pair;
import com.sap.sailing.util.Util.Triple;

/**
 * Implements the connector to the SwissTiming Sail Master system. It uses a hostname and port number to establish the
 * connecting via TCP. The connector offers a number of explicit service request methods. Additionally, the connector
 * can receive "spontaneous" events sent by the sail master system. Clients can register for those spontaneous events
 * (see {@link #addSailMasterListener}).
 * <p>
 * 
 * When the connector is used with SailMaster instances hidden behind a "bridge" / firewall, no explicit requests are
 * possible, and the connector has to rely solely on the events it receives. It may, though, load recorded race-specific
 * messages through a {@link RaceSpecificMessageLoader} object.
 * <p>
 * 
 * Generally, the connector needs to be instructed for which races it shall handle events using calls to the
 * {@link #trackRace} and {@link #stopTrackingRace} operations. {@link MessageType#isRaceSpecific() Race-specific
 * messages} for other races are then ignored and not forwarded to any listener.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class SailMasterConnectorImpl extends SailMasterTransceiverImpl implements SailMasterConnector, Runnable {
    private static final Logger logger = Logger.getLogger(SailMasterConnectorImpl.class.getName());
    
    private final String host;
    private final int port;
    private Socket socket;
    private final DateFormat dateFormat;
    private final Set<SailMasterListener> listeners;
    private final Thread receiverThread;
    private boolean stopped;
    private boolean connected;
    
    private final Set<String> idsOfTrackedRaces;
    
    /**
     * Currently the SwissTiming SailMaster protocol only transmits time zone information when sending
     * an {@link MessageType#RPD RPD} event. Other events, such as the {@link MessageType#STT STT} or
     * {@link MessageType#CAM CAM} events/responses also carry time stamps but in hh:mm:ss format without
     * any hint as to the time zone relative to which they are given.<p>
     * 
     * The only way known so far for how to find out the time zone relative to which the other time stamps
     * are to be interpreted is to start with the current default time zone's offset and wait for an
     * {@link MessageType#RPD RPD} event to be received. From this event, the time zone offset can be extracted
     * and applied to all other time stamps.
     */
    private String lastTimeZoneSuffix;
    
    /**
     * Used for the {@link #rendevouz(SailMasterMessage)} pattern. For each {@link MessageType} there
     * is a queue to which response messages of that type are offered so that {@link #receiveMessage(MessageType)}
     * can take them from there.
     */
    private final Map<MessageType, BlockingQueue<SailMasterMessage>> unprocessedMessagesByType;
    
    private final Map<String, List<SailMasterMessage>> raceSpecificMessageBuffers;
    
    private final Map<String, Long> sequenceNumberOfLastMessageForRaceID;
    
    private final RaceSpecificMessageLoader messageLoader;
    
    public SailMasterConnectorImpl(String host, int port, RaceSpecificMessageLoader messageLoader) throws InterruptedException {
        super();
        this.messageLoader = messageLoader;
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
        idsOfTrackedRaces = new HashSet<String>();
        this.host = host;
        this.port = port;
        this.listeners = new HashSet<SailMasterListener>();
        this.unprocessedMessagesByType = new HashMap<MessageType, BlockingQueue<SailMasterMessage>>();
        raceSpecificMessageBuffers = new HashMap<String, List<SailMasterMessage>>();
        sequenceNumberOfLastMessageForRaceID = new HashMap<String, Long>();
        int offset = TimeZone.getDefault().getOffset(System.currentTimeMillis())/1000/3600;
        lastTimeZoneSuffix = (offset<0?"-":"+") + new DecimalFormat("00").format(offset)+"00";
        receiverThread = new Thread(this, "SwissTiming SailMaster Receiver");
        receiverThread.start();
        synchronized (this) {
            while (!connected) {
                wait();
            }
        }
    }
    
    /**
     * If a non-<code>null</code> {@link #messageLoader} was passed to the constructor, buffering of messages for
     * <code>raceID</code> is activated by creating a buffer list in {@link #raceSpecificMessageBuffers} and the stored
     * messages for <code>raceID</code> are loaded using {@link #messageLoader}. When done with loading, the messages
     * loaded are "replayed" by notifying listeners about them; then, all messages from the buffer in
     * {@link #raceSpecificMessageBuffers} are notified to listeners. Messages notified to listeners are removed from
     * the buffer. This happens while owning this object's lock. The receiving thread has to obtain the lock in order to
     * check if buffering for <code>raceID</code> is active and to add a new message to the buffer. If the last message
     * is taken from the buffer here, while owning the lock the buffer is removed which will cause the receiving thread
     * to no longer buffer but forward to listeners immediately.
     * @throws ParseException 
     */
    @Override
    public void trackRace(String raceID) throws ParseException {
        List<SailMasterMessage> buffer = null;
        synchronized (this) {
            if (messageLoader != null) {
                buffer = new LinkedList<SailMasterMessage>();
                sequenceNumberOfLastMessageForRaceID.put(raceID, -1l);
                raceSpecificMessageBuffers.put(raceID, buffer);
            }
        }
        idsOfTrackedRaces.add(raceID); // from this time on, the connector interprets messages for raceID
        if (messageLoader != null) {
            List<SailMasterMessage> messages = messageLoader.loadRaceMessages(raceID);
            long maxSequenceNumber = -1;
            for (SailMasterMessage message : messages) {
                logger.fine("notifying loaded message "+message);
                notifyListeners(message);
                assert message.getSequenceNumber() == null || message.getSequenceNumber() > maxSequenceNumber;
                if (message.getSequenceNumber() != null) {
                    maxSequenceNumber = message.getSequenceNumber();
                }
            }
            // now process the buffered messages one by one:
            SailMasterMessage bufferedMessage;
            do {
                synchronized (this) {
                    if (buffer.size() > 0) {
                        bufferedMessage = buffer.remove(0);
                        if (bufferedMessage.getSequenceNumber() != null && bufferedMessage.getSequenceNumber() > maxSequenceNumber) {
                            maxSequenceNumber = bufferedMessage.getSequenceNumber();
                        } else {
                            logger.info("discarding already loaded buffered message " + bufferedMessage);
                            bufferedMessage = null;
                        }
                    } else {
                        bufferedMessage = null;
                    }
                    if (buffer.isEmpty()) {
                        // buffer is empty; stop buffering
                        sequenceNumberOfLastMessageForRaceID.put(raceID, maxSequenceNumber);
                        raceSpecificMessageBuffers.remove(raceID);
                    }
                }
                if (bufferedMessage != null) {
                    logger.fine("notifying buffered message " + bufferedMessage);
                    notifyListeners(bufferedMessage);
                }
            } while (raceSpecificMessageBuffers.containsKey(raceID));
        }
    }
    
    @Override
    public void stopTrackingRace(String raceID) {
        idsOfTrackedRaces.remove(raceID);
    }
    
    public void run() {
        try {
            while (!stopped) {
                ensureSocketIsOpen();
                try {
                    Pair<String, Long> receivedMessageAndOptionalSequenceNumber = receiveMessage(socket.getInputStream());
                    SailMasterMessage message = new SailMasterMessageImpl(
                            receivedMessageAndOptionalSequenceNumber.getA(),
                            receivedMessageAndOptionalSequenceNumber.getB());
                    // drop race-specific messages for non-tracked races
                    if (!message.getType().isRaceSpecific() || idsOfTrackedRaces.contains(message.getRaceID())) {
                        boolean messageProcessed = false;
                        synchronized (this) {
                            if (message.getType().isRaceSpecific() && isCurrentlyBuffering(message.getRaceID())) {
                                buffer(message);
                                messageProcessed = true;
                            }
                        }
                        if (!messageProcessed) {
                            if (message.isResponse()) {
                                // this is a response for an explicit request
                                rendevouz(message);
                            } else if (message.isEvent()) {
                                // only notify if it hasn't been loaded from a store yet
                                if (!message.getType().isRaceSpecific() || message.getSequenceNumber() == null ||
                                        message.getSequenceNumber() > sequenceNumberOfLastMessageForRaceID.get(message.getRaceID())) {
                                    // a spontaneous event
                                    logger.fine("notifying message " + message);
                                    notifyListeners(message);
                                } else {
                                    logger.info("discarding already notified message " + message);
                                }
                            }
                        }
                    }
                    if (message.getType() == MessageType._STOPSERVER) {
                        stop();
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
    
    private synchronized void buffer(SailMasterMessage message) {
        logger.find("buffering message "+message);
        assert message.getType().isRaceSpecific();
        List<SailMasterMessage> buffer = raceSpecificMessageBuffers.get(message.getRaceID());
        buffer.add(message);
    }

    private synchronized boolean isCurrentlyBuffering(String raceID) {
        return raceSpecificMessageBuffers.containsKey(raceID);
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
        case RAC:
            notifyListenersRAC(message);
            break;
        case CCG:
            notifyListenersCCG(message);
            break;
        case STL:
            notifyListenersSTL(message);
            break;
        case CAM:
            notifyListenersCAM(message);
            break;
        case TMD:
            notifyListenersTMD(message);
            break;
        }
    }

    private void notifyListenersTMD(SailMasterMessage message) {
        // example message: TMD|W4702|NZL 75|1|4;;00:49:43
        String raceID = message.getSections()[1];
        String boatID = message.getSections()[2];
        int count = Integer.valueOf(message.getSections()[3]);
        List<Triple<Integer, Integer, Long>> markIndicesRanksAndTimesSinceStartInMilliseconds = new ArrayList<Triple<Integer,Integer,Long>>();
        for (int i = 0; i < count; i++) {
            String[] details = message.getSections()[4+i].split(";");
            Integer markIndex = details.length <= 0 || details[0].trim().length() == 0 ? null : Integer.valueOf(details[0]); 
            Integer rank = details.length <= 1 || details[1].trim().length() == 0 ? null : Integer.valueOf(details[1]); 
            Long timeSinceStartInMilliseconds = details.length <= 2 || details[2].trim().length() == 0 ? null :
                parseHHMMSSToMilliseconds(details[2]);
            markIndicesRanksAndTimesSinceStartInMilliseconds.add(new Triple<Integer, Integer, Long>(markIndex, rank, timeSinceStartInMilliseconds));
        }
        for (SailMasterListener listener : listeners) {
            listener.receivedTimingData(raceID, boatID, markIndicesRanksAndTimesSinceStartInMilliseconds);
        }
    }

    private void notifyListenersCAM(SailMasterMessage message) throws ParseException {
        List<Triple<Integer, TimePoint, String>> clockAtMarkResults = parseClockAtMarkMessage(message);
        for (SailMasterListener listener : listeners) {
            listener.receivedClockAtMark(message.getSections()[1], clockAtMarkResults);
        }
    }

    private void notifyListenersSTL(SailMasterMessage message) {
        StartList startListMessage = parseStartListMessage(message);
        for (SailMasterListener listener : listeners) {
            listener.receivedStartList(message.getSections()[1], startListMessage);
        }
    }

    private void notifyListenersCCG(SailMasterMessage message) {
        Course course = parseCourseConfigurationMessage(message);
        for (SailMasterListener listener : listeners) {
            listener.receivedCourseConfiguration(message.getSections()[1], course);
        }
    }

    private void notifyListenersRAC(SailMasterMessage message) {
        Iterable<Race> races = parseAvailableRacesMessage(message);
        for (SailMasterListener listener : listeners) {
            listener.receivedAvailableRaces(races);
        }
    }

    private void notifyListenersRPD(SailMasterMessage message) throws ParseException {
        assert message.getType() == MessageType.RPD;
        String[] sections = message.getSections();
        String raceID = sections[1];
        RaceStatus status = RaceStatus.values()[Integer.valueOf(sections[2])];
        TimePoint timePoint = new MillisecondsTimePoint(parseTimeAndDateISO(sections[3]));
        TimePoint startTimeEstimatedStartTime = sections[4].trim().length() == 0 ? null : new MillisecondsTimePoint(
                parseTimePrefixedWithISOToday(sections[4]));
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
                Speed averageSpeedOverGround = fixSections[fixDetailIndex].trim().length() == 0 ? null
                            : new KnotSpeedImpl(Double.valueOf(fixSections[fixDetailIndex]));
                fixDetailIndex++;
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

    @Override
    public void stop() throws IOException {
        stopped = true;
        socket.close();
        socket = null;
    }
    
    @Override
    public void addSailMasterListener(SailMasterListener listener) throws UnknownHostException, IOException {
        ensureSocketIsOpen();
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
            synchronized (this) {
                connected = true;
                notifyAll();
            }
        }
    }

    @Override
    public Iterable<Race> getRaces() throws UnknownHostException, IOException, InterruptedException {
        SailMasterMessage response = sendRequestAndGetResponse(MessageType.RAC);
        assertResponseType(MessageType.RAC, response);
        List<Race> result = parseAvailableRacesMessage(response);
        return result;
    }

    private List<Race> parseAvailableRacesMessage(SailMasterMessage availableRacesMessage) {
        assertMessageType(MessageType.RAC, availableRacesMessage);
        int count = Integer.valueOf(availableRacesMessage.getSections()[1]);
        List<Race> result = new ArrayList<Race>();
        for (int i=0; i<count; i++) {
            String[] idAndDescription = availableRacesMessage.getSections()[2+i].split(";");
            result.add(new RaceImpl(idAndDescription[0], idAndDescription[1]));
        }
        return result;
    }

    @Override
    public Course getCourse(String raceID) throws UnknownHostException, IOException, InterruptedException {
        SailMasterMessage response = sendRequestAndGetResponse(MessageType.CCG, raceID);
        String[] sections = response.getSections();
        assertResponseType(MessageType.CCG, response);
        assertRaceID(raceID, sections[1]);
        return parseCourseConfigurationMessage(response);
    }

    private Course parseCourseConfigurationMessage(SailMasterMessage courseConfigurationMessage) {
        assertMessageType(MessageType.CCG, courseConfigurationMessage);
        int count = Integer.valueOf(courseConfigurationMessage.getSections()[2]);
        List<Mark> marks = new ArrayList<Mark>();
        for (int i=0; i<count; i++) {
            String[] markDetails = courseConfigurationMessage.getSections()[3+i].split(";");
            marks.add(new MarkImpl(markDetails[1], Integer.valueOf(markDetails[0]), Arrays.asList(markDetails).subList(2, markDetails.length)));
        }
        return new CourseImpl(courseConfigurationMessage.getSections()[1], marks);
    }
    
    private String prefixTimeWithISOTodayAndSuffixWithTimezoneIndicator(String time) {
        synchronized (dateFormat) {
            return dateFormat.format(new Date()).substring(0, "yyyy-mm-ddT".length())+time+lastTimeZoneSuffix;
        }
    }

    private Date parseTimeAndDateISO(String timeAndDateISO) throws ParseException {
        char timeZoneIndicator = timeAndDateISO.charAt(timeAndDateISO.length()-6);
        if ((timeZoneIndicator == '+' || timeZoneIndicator == '-') && timeAndDateISO.charAt(timeAndDateISO.length()-3) == ':') {
            timeAndDateISO = timeAndDateISO.substring(0, timeAndDateISO.length()-3)+timeAndDateISO.substring(timeAndDateISO.length()-2);
            lastTimeZoneSuffix = timeAndDateISO.substring(timeAndDateISO.length()-5);
        }
        synchronized(dateFormat) {
            return dateFormat.parse(timeAndDateISO);
        }
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
    
    private void assertMessageType(MessageType expectedMessageType, SailMasterMessage message) {
        if (message.getType() != expectedMessageType) {
            throw new RuntimeException("Expected a "+expectedMessageType+" message type but got "+message.getType());
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
        return parseStartListMessage(response);
    }

    private StartList parseStartListMessage(SailMasterMessage startListMessage) {
        assertMessageType(MessageType.STL, startListMessage);
        ArrayList<Competitor> competitors = new ArrayList<Competitor>();
        int count = Integer.valueOf(startListMessage.getSections()[2]);
        for (int i=0; i<count; i++) {
            String[] competitorDetails = startListMessage.getSections()[3+i].split(";");
            competitors.add(new CompetitorImpl(competitorDetails[0], competitorDetails[1], competitorDetails[2]));
        }
        return new StartListImpl(startListMessage.getSections()[1], competitors);
    }

    @Override
    public TimePoint getStartTime(String raceID) throws UnknownHostException, IOException, ParseException, InterruptedException {
        SailMasterMessage response = sendRequestAndGetResponse(MessageType.STT, raceID);
        String[] sections = response.getSections();
        assertResponseType(MessageType.STT, response);
        assertRaceID(raceID, sections[1]);
        return new MillisecondsTimePoint(parseTimePrefixedWithISOToday(sections[2]));
    }

    private Date parseTimePrefixedWithISOToday(String timeHHMMSS) throws ParseException {
        synchronized(dateFormat) {
            return dateFormat.parse(prefixTimeWithISOTodayAndSuffixWithTimezoneIndicator(timeHHMMSS));
        }
    }

    @Override
    public Distance getDistanceToMark(String raceID, int markIndex, String boatID) throws UnknownHostException, IOException, InterruptedException {
        SailMasterMessage response = sendRequestAndGetResponse(MessageType.DTM, raceID, ""+markIndex, boatID);
        String[] sections = response.getSections();
        assertResponseType(MessageType.DTM, response);
        assertRaceID(raceID, sections[1]);
        assertMarkIndex(markIndex, sections[2]);
        assertBoatID(boatID, sections[3]);
        return sections.length <= 4 || sections[4].trim().length() == 0 ? null : new MeterDistance(Double.valueOf(sections[4]));
    }

    @Override
    public Speed getCurrentBoatSpeed(String raceID, String boatID) throws UnknownHostException, IOException, InterruptedException {
        SailMasterMessage response = sendRequestAndGetResponse(MessageType.CBS, raceID, boatID);
        String[] sections = response.getSections();
        assertResponseType(MessageType.CBS, response);
        assertRaceID(raceID, sections[1]);
        assertBoatID(boatID, sections[2]);
        return new KilometersPerHourSpeedImpl(3.6*Double.valueOf(sections[3]));
    }

    @Override
    public Distance getDistanceBetweenBoats(String raceID, String boatID1, String boatID2) throws UnknownHostException, IOException, InterruptedException {
        Distance result;
        if (boatID1.equals(boatID2)) {
            result = Distance.NULL;
        } else {
            SailMasterMessage response = sendRequestAndGetResponse(MessageType.DBB, raceID, boatID1, boatID2);
            String[] sections = response.getSections();
            assertResponseType(MessageType.DBB, response);
            assertRaceID(raceID, sections[1]);
            assertBoatID(boatID1, sections[2]);
            assertBoatID(boatID2, sections[3]);
            result = sections.length <= 4 || sections[4].trim().length() == 0 ? null : new MeterDistance(Double.valueOf(sections[4]));
        }
        return result;
    }

    @Override
    public Speed getAverageBoatSpeed(String raceID, String leg, String boatID) throws UnknownHostException, IOException, InterruptedException {
        SailMasterMessage response = sendRequestAndGetResponse(MessageType.ABS, raceID, leg, boatID);
        String[] sections = response.getSections();
        assertResponseType(MessageType.ABS, response);
        assertRaceID(raceID, sections[1]);
        assertLeg(leg, sections[2]);
        assertBoatID(boatID, sections[3]);
        return new KilometersPerHourSpeedImpl(3.6*Double.valueOf(sections[4]));
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
            Long millisecondsSinceStart = markTimeDetail.length <= 2 || markTimeDetail[2].trim().length() == 0 ? null :
                parseHHMMSSToMilliseconds(markTimeDetail[2]);
            result.put(Integer.valueOf(markTimeDetail[0]), new Pair<Integer, Long>(
                    markTimeDetail.length <= 1 || markTimeDetail[1].trim().length() == 0 ? null :
                        Integer.valueOf(markTimeDetail[1]), millisecondsSinceStart));
        }
        return result;
    }

    @Override
    public List<Triple<Integer, TimePoint, String>> getClockAtMark(String raceID) throws ParseException, UnknownHostException, IOException, InterruptedException {
        SailMasterMessage response = sendRequestAndGetResponse(MessageType.CAM, raceID);
        String[] sections = response.getSections();
        assertResponseType(MessageType.CAM, response);
        assertRaceID(raceID, sections[1]);
        List<Triple<Integer, TimePoint, String>> result = parseClockAtMarkMessage(response);
        return result;
    }

    private List<Triple<Integer, TimePoint, String>> parseClockAtMarkMessage(SailMasterMessage clockAtMarkMessage) throws ParseException {
        assertMessageType(MessageType.CAM, clockAtMarkMessage);
        List<Triple<Integer, TimePoint, String>> result = new ArrayList<Triple<Integer,TimePoint,String>>();
        int count = Integer.valueOf(clockAtMarkMessage.getSections()[2]);
        for (int i=0; i<count; i++) {
            String[] clockAtMarkDetail = clockAtMarkMessage.getSections()[3+i].split(";");
            int markIndex = Integer.valueOf(clockAtMarkDetail[0]);
            TimePoint timePoint = clockAtMarkDetail.length <= 1 || clockAtMarkDetail[1].trim().length() == 0 ? null :
                new MillisecondsTimePoint(parseTimePrefixedWithISOToday(clockAtMarkDetail[1]));
            result.add(new Triple<Integer, TimePoint, String>(
                    markIndex, timePoint, clockAtMarkDetail.length <= 2 ? null : clockAtMarkDetail[2]));
        }
        return result;
    }

    private long parseHHMMSSToMilliseconds(String hhmmss) {
        String[] timeDetail = hhmmss.split(":");
        long millisecondsSinceStart = 1000 * (Long.valueOf(timeDetail[2]) + 60 * Long.valueOf(timeDetail[1]) + 3600 * Long
                .valueOf(timeDetail[0]));
        return millisecondsSinceStart;
    }

    @Override
    public void enableRacePositionData() throws UnknownHostException, IOException, InterruptedException {
        sendRequestAndGetResponse(MessageType.RPD, "1");
    }

    @Override
    public void disableRacePositionData() throws UnknownHostException, IOException, InterruptedException {
        sendRequestAndGetResponse(MessageType.RPD, "0");
    }
    
}
