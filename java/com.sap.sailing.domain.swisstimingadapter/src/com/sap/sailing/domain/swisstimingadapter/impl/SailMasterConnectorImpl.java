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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KilometersPerHourSpeedImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.swisstimingadapter.Competitor;
import com.sap.sailing.domain.swisstimingadapter.Course;
import com.sap.sailing.domain.swisstimingadapter.Fix;
import com.sap.sailing.domain.swisstimingadapter.Mark;
import com.sap.sailing.domain.swisstimingadapter.Mark.MarkType;
import com.sap.sailing.domain.swisstimingadapter.MessageType;
import com.sap.sailing.domain.swisstimingadapter.Race;
import com.sap.sailing.domain.swisstimingadapter.RaceSpecificMessageLoader;
import com.sap.sailing.domain.swisstimingadapter.RaceStatus;
import com.sap.sailing.domain.swisstimingadapter.SailMasterConnector;
import com.sap.sailing.domain.swisstimingadapter.SailMasterListener;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;
import com.sap.sailing.domain.swisstimingadapter.StartList;
import com.sap.sailing.domain.swisstimingadapter.TrackerType;

/**
 * Implements the connector to the SwissTiming Sail Master system. It uses a host name and port number to establish the
 * connection via TCP. The connector offers a number of explicit service request methods. Additionally, the connector
 * can receive "spontaneous" events sent by the sail master system. Clients can register for those spontaneous events
 * (see {@link #addSailMasterListener}).
 * <p>
 * 
 * When the connector is used with SailMaster instances hidden behind a "bridge" / firewall, no explicit requests are
 * possible, and the connector has to rely solely on the events it receives. It may, though, load recorded race-specific
 * messages through a {@link RaceSpecificMessageLoader} object. If a non-<code>null</code> {@link RaceSpecificMessageLoader}
 * is provided to the constructor, the connector will fetch the {@link #getRaces() list of races} from that loader.
 * Additionally, the connector will use the loader upon each {@link #trackRace(String)} to load all messages recorded
 * by the loader for the race requested so far.
 * <p>
 * 
 * Generally, the connector needs to be instructed for which races it shall handle events using calls to the
 * {@link #trackRace} and {@link #stopTrackingRace} operations. {@link MessageType#isRaceSpecific() Race-specific
 * messages} for other races are ignored and not forwarded to any listener.<p>
 * 
 * Clients that want to wait until the connector changes to {@link #isStopped()} can {@link Object#wait()} on this
 * object because it notifies all waiters when changing from !{@link #isStopped()} to {@link #isStopped()}. 
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
    private final Map<String, Set<SailMasterListener>> raceSpecificListeners;
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
     * and applied to all other time stamps. It is stored using the race ID as key.
     */
    private final Map<String, String> lastTimeZoneSuffixPerRaceID;
    
    private final Map<String, TimePoint> startTimePerRaceID;
    
    /**
     * Used for the {@link #rendevouz(SailMasterMessage)} pattern. For each {@link MessageType} there
     * is a queue to which response messages of that type are offered so that {@link #receiveMessage(MessageType)}
     * can take them from there.
     */
    private final Map<MessageType, BlockingQueue<SailMasterMessage>> unprocessedMessagesByType;
    
    private final Map<String, List<SailMasterMessage>> raceSpecificMessageBuffers;
    
    private final Map<String, Long> sequenceNumberOfLastMessageForRaceID;
    
    private final RaceSpecificMessageLoader messageLoader;

    private final boolean canSendRequests;

    public SailMasterConnectorImpl(String host, int port, RaceSpecificMessageLoader messageLoader, boolean canSendRequests) throws InterruptedException {
        super();
        this.messageLoader = messageLoader;
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        idsOfTrackedRaces = new HashSet<String>();
        this.host = host;
        this.port = port;
        this.canSendRequests = canSendRequests;
        this.listeners = new HashSet<SailMasterListener>();
        this.raceSpecificListeners = new HashMap<String, Set<SailMasterListener>>();
        this.unprocessedMessagesByType = new HashMap<MessageType, BlockingQueue<SailMasterMessage>>();
        raceSpecificMessageBuffers = new HashMap<String, List<SailMasterMessage>>();
        sequenceNumberOfLastMessageForRaceID = new HashMap<String, Long>();
        lastTimeZoneSuffixPerRaceID = new HashMap<String, String>();
        startTimePerRaceID = new HashMap<String, TimePoint>();
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
            notifyListenersStoredDataProgress(raceID, 0.0);
            List<SailMasterMessage> messages = messageLoader.loadRaceMessages(raceID);
            long maxSequenceNumber = -1;
            int i = 0;
            for (SailMasterMessage message : messages) {
                logger.fine("notifying loaded message "+message);
                notifyListeners(message);
                assert message.getSequenceNumber() == null || message.getSequenceNumber() > maxSequenceNumber;
                if (message.getSequenceNumber() != null) {
                    maxSequenceNumber = message.getSequenceNumber();
                }
                i++;
                notifyListenersStoredDataProgress(raceID, ((double) i)/(double) messages.size());
            }
            notifyListenersStoredDataProgress(raceID, 1.0);
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
        notifyListenersStoredDataProgress(raceID, 1.0);
    }
    
    @Override
    public void stopTrackingRace(String raceID) {
        idsOfTrackedRaces.remove(raceID);
    }
    
    public void run() {
        try {
            while (!stopped) {
                try {
                    ensureSocketIsOpen();
                    Pair<String, Long> receivedMessageAndOptionalSequenceNumber = receiveMessage(socket.getInputStream());
                    if (receivedMessageAndOptionalSequenceNumber == null) {
                        // reached EOF; this means the socket is or can be closed
                        if (socket != null && !socket.isClosed()) {
                            socket.close();
                        }
                        socket = null;
                    } else {
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
                                    if (!message.getType().isRaceSpecific()
                                            || message.getSequenceNumber() == null
                                            || (sequenceNumberOfLastMessageForRaceID.containsKey(message.getRaceID()) && message
                                                    .getSequenceNumber() > sequenceNumberOfLastMessageForRaceID
                                                    .get(message.getRaceID()))) {
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
                            logger.info("SailMasterConnector received " + MessageType._STOPSERVER.name());
                            stop();
                        }
                    }
                } catch (SocketException se) {
                    // This occurs if the socket was closed which may mean the connector was stopped. Check in while
                    logger.info("Caught exception "+se+" during socket operation; setting socket to null");
                    socket = null;
                    Thread.sleep(1000); // try again in 1s
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception in sail master connector "+SailMasterConnectorImpl.class.getName()+".run", e);
        }
        logger.info("Stopping Sail Master connector thread");
        stopped = true;
    }
    
    private synchronized void buffer(SailMasterMessage message) {
        logger.fine("buffering message "+message);
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

    protected void notifyListeners(SailMasterMessage message) {
        try {
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
            default:
                // ignore all other messages because there are no notification patterns for those
            }
        } catch (Exception e) {
            // broken messages are ignored
            logger.warning("Exception caught during parsing of message '" + message.getMessage() + "' : " + e.getMessage());
        }
    }
    
    private void notifyListenersStoredDataProgress(String raceID, double progress) {
        for (SailMasterListener listener : getGeneralAndRaceSpecificListeners(raceID)) {
            try {
                listener.storedDataProgress(raceID, progress);
            } catch (Exception e) {
                logger.info("Exception occurred trying to notify listener "+listener+" about progress "+progress);
                logger.throwing(SailMasterConnectorImpl.class.getName(), "notifyStoredDataProgress", e);
            }
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
        for (SailMasterListener listener : getGeneralAndRaceSpecificListeners(message.getRaceID())) {
            try {
                listener.receivedTimingData(raceID, boatID, markIndicesRanksAndTimesSinceStartInMilliseconds);
            } catch (Exception e) {
                logger.info("Exception occurred trying to notify listener "+listener+" about "+message+": "+e.getMessage());
                logger.throwing(SailMasterConnectorImpl.class.getName(), "notifyListenersTMD", e);
            }
        }
    }

    private void notifyListenersCAM(SailMasterMessage message) throws ParseException {
        List<Triple<Integer, TimePoint, String>> clockAtMarkResults = parseClockAtMarkMessage(message);
        for (SailMasterListener listener : getGeneralAndRaceSpecificListeners(message.getRaceID())) {
            try {
                listener.receivedClockAtMark(message.getSections()[1], clockAtMarkResults);
            } catch (Exception e) {
                logger.info("Exception occurred trying to notify listener "+listener+" about "+message+": "+e.getMessage());
                logger.throwing(SailMasterConnectorImpl.class.getName(), "notifyListenersCAM", e);
            }
        }
    }

    private void notifyListenersSTL(SailMasterMessage message) {
        StartList startListMessage = parseStartListMessage(message);
        for (SailMasterListener listener : getGeneralAndRaceSpecificListeners(message.getRaceID())) {
            try {
                listener.receivedStartList(message.getSections()[1], startListMessage);
            } catch (Exception e) {
                logger.info("Exception occurred trying to notify listener "+listener+" about "+message+": "+e.getMessage());
                logger.throwing(SailMasterConnectorImpl.class.getName(), "notifyListenersSTL", e);
            }
        }
    }

    private void notifyListenersCCG(SailMasterMessage message) {
        Course course = parseCourseConfigurationMessage(message);
        for (SailMasterListener listener : getGeneralAndRaceSpecificListeners(message.getRaceID())) {
            try {
                listener.receivedCourseConfiguration(message.getSections()[1], course);
            } catch (Exception e) {
                logger.info("Exception occurred trying to notify listener "+listener+" about "+message+": "+e.getMessage());
                logger.throwing(SailMasterConnectorImpl.class.getName(), "notifyListenersCCG", e);
            }
        }
    }

    private void notifyListenersRAC(SailMasterMessage message) {
        Iterable<Race> races = parseAvailableRacesMessage(message);
        for (SailMasterListener listener : listeners) {
            try {
                listener.receivedAvailableRaces(races);
            } catch (Exception e) {
                logger.info("Exception occurred trying to notify listener "+listener+" about "+message+": "+e.getMessage());
                logger.throwing(SailMasterConnectorImpl.class.getName(), "notifyListenersRAC", e);
            }
        }
        // not race specific; no need to notify any listener from raceSpecificListeners
    }

    private void notifyListenersRPD(SailMasterMessage message) throws ParseException {
        assert message.getType() == MessageType.RPD;
        String[] sections = message.getSections();
        String raceID = sections[1];
        RaceStatus status = RaceStatus.values()[Integer.valueOf(sections[2])];
        TimePoint timePoint = new MillisecondsTimePoint(parseTimeAndDateISO(sections[3], raceID));
        String dateISO = sections[3].substring(0, sections[3].indexOf('T'));
        String startTimeEstimatedStartTimeISO = dateISO+"T"+sections[4]+lastTimeZoneSuffixPerRaceID.get(raceID);
        TimePoint startTimeEstimatedStartTime = sections[4].trim().length() == 0 ? null : new MillisecondsTimePoint(
                parseTimeAndDateISO(startTimeEstimatedStartTimeISO, raceID));
        if (startTimeEstimatedStartTime != null) {
            startTimePerRaceID.put(raceID, startTimeEstimatedStartTime);
        }
        Long millisecondsSinceRaceStart = sections[5].trim().length() == 0 ? null : parseHHMMSSToMilliseconds(sections[5]);
        Integer nextMarkIndexForLeader = sections[6].trim().length() == 0 ? null : Integer.valueOf(sections[6]);
        Distance distanceToNextMarkForLeader = sections[7].trim().length() == 0 ? null : new MeterDistance(Double.valueOf(sections[7]));
        int count = Integer.valueOf(sections[8]);
        Collection<Fix> fixes = new ArrayList<Fix>();
        for (int i=0; i<count; i++) {
            int fixDetailIndex = 0;
            final String[] fixSections = sections[9+i].split(";");
            final boolean postVersion1_0 = sections[9+i].split(";", -1).length >= 14;
            if (fixSections.length > 2) {
                final String boatID = fixSections[fixDetailIndex++];
                final TrackerType trackerType = TrackerType.values()[Integer.valueOf(fixSections[fixDetailIndex++])];
                final Long ageOfDataInMilliseconds = 1000l * Long.valueOf(fixSections[fixDetailIndex++]);
                final Position position = new DegreePosition(Double.valueOf(fixSections[fixDetailIndex++]),
                        Double.valueOf(fixSections[fixDetailIndex++]));
                final Double speedOverGroundInKnots = Double.valueOf(fixSections[fixDetailIndex++]);
                final int alsIndex = postVersion1_0 ? fixDetailIndex+1 : fixDetailIndex;
                final int vmgIndex = postVersion1_0 ? fixDetailIndex : fixDetailIndex+1;
                final Speed averageSpeedOverGround = fixSections[alsIndex].trim().length() == 0 ? null
                            : new KnotSpeedImpl(Double.valueOf(fixSections[alsIndex]));
                final Speed velocityMadeGood = fixSections[vmgIndex].trim().length() == 0 ? null : new KnotSpeedImpl(
                        Double.valueOf(fixSections[vmgIndex]));
                fixDetailIndex += 2;
                final DegreeBearingImpl cog = new DegreeBearingImpl(
                        Double.valueOf(fixSections[fixDetailIndex++]));
                final SpeedWithBearing speed = new KnotSpeedWithBearingImpl(speedOverGroundInKnots, cog);
                final Integer nextMarkIndex = fixSections.length <= fixDetailIndex
                        || fixSections[fixDetailIndex].trim().length() == 0 ? null : Integer
                        .valueOf(fixSections[fixDetailIndex]);
                fixDetailIndex++;
                final Integer rank = fixSections.length <= fixDetailIndex || fixSections[fixDetailIndex].trim().length() == 0 ? null
                        : Integer.valueOf(fixSections[fixDetailIndex]);
                fixDetailIndex++;
                final Distance distanceToLeader = fixSections.length <= fixDetailIndex
                        || fixSections[fixDetailIndex].trim().length() == 0 ? null : new MeterDistance(
                        Double.valueOf(fixSections[fixDetailIndex]));
                fixDetailIndex++;
                final Distance distanceToNextMark = fixSections.length <= fixDetailIndex
                        || fixSections[fixDetailIndex].trim().length() == 0 ? null : new MeterDistance(
                        Double.valueOf(fixSections[fixDetailIndex]));
                fixDetailIndex++;
                final String boatIRM; // the "disqualification" or "MaxPointReason"
                if (postVersion1_0 && fixSections.length > fixDetailIndex) {
                    boatIRM = fixSections[fixDetailIndex++];
                } else {
                    boatIRM = null;
                }
                fixes.add(new FixImpl(boatID, trackerType, ageOfDataInMilliseconds, position, speed, nextMarkIndex,
                        rank, averageSpeedOverGround, velocityMadeGood, distanceToLeader, distanceToNextMark, boatIRM));
            }
        }
        Set<SailMasterListener> allListeners = getGeneralAndRaceSpecificListeners(message.getRaceID());
        for (SailMasterListener listener : allListeners) {
            try {
                listener.receivedRacePositionData(raceID, status, timePoint, startTimeEstimatedStartTime, millisecondsSinceRaceStart,
                        nextMarkIndexForLeader, distanceToNextMarkForLeader, fixes);
            } catch (Exception e) {
                logger.info("Exception occurred trying to notify listener "+listener+" about "+message+": "+e.getMessage());
                logger.throwing(SailMasterConnectorImpl.class.getName(), "notifyListenersRPD", e);
            }
        }
    }

    private Set<SailMasterListener> getGeneralAndRaceSpecificListeners(String raceID) {
        Set<SailMasterListener> allListeners = new HashSet<SailMasterListener>(listeners);
        Set<SailMasterListener> listenersForThisRace = raceSpecificListeners.get(raceID);
        if (listenersForThisRace != null) {
            allListeners.addAll(listenersForThisRace);
        }
        return allListeners;
    }

    @Override
    public synchronized void stop() throws IOException {
        stopped = true;
        socket.close();
        socket = null;
        notifyAll();
    }
    
    @Override
    public boolean isStopped() {
        return stopped;
    }
    
    @Override
    public void addSailMasterListener(SailMasterListener listener) throws UnknownHostException, IOException {
        ensureSocketIsOpen();
        listeners.add(listener);
    }
    
    @Override
    public synchronized void addSailMasterListener(String raceID, SailMasterListener listener) throws UnknownHostException, IOException {
        ensureSocketIsOpen();
        Set<SailMasterListener> set = raceSpecificListeners.get(raceID);
        if (set == null) {
            set = new HashSet<SailMasterListener>();
            raceSpecificListeners.put(raceID, set);
        }
        set.add(listener);
    }
    
    @Override
    public void removeSailMasterListener(SailMasterListener listener) {
        listeners.remove(listener);
    }

    @Override
    public synchronized void removeSailMasterListener(String raceID, SailMasterListener listener) {
        Set<SailMasterListener> set = raceSpecificListeners.get(raceID);
        if (set != null) {
            set.remove(listener);
            if (set.isEmpty()) {
                raceSpecificListeners.remove(raceID);
            }
        }
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

    private boolean canSendRequests() {
        return canSendRequests;
    }
    
    @Override
    public Iterable<Race> getRaces() throws UnknownHostException, IOException, InterruptedException {
        Iterable<Race> result = null;
        if (messageLoader != null) {
            result = messageLoader.getRaces();
        }
        if (result == null && canSendRequests()) {
            SailMasterMessage response = sendRequestAndGetResponse(MessageType.RAC);
            assertResponseType(MessageType.RAC, response);
            result = parseAvailableRacesMessage(response);
        }
        return result;
    }

    @Override
    public boolean hasCourse(String raceID) {
        boolean result = false;
        if (messageLoader != null) {
            result = messageLoader.hasRaceCourse(raceID);
        }
        return result; 
    }

    @Override
    public boolean hasStartlist(String raceID) {
        boolean result = false;
        if (messageLoader != null) {
            result = messageLoader.hasRaceStartlist(raceID);
        }
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
            MarkType markType = null;
            final int devicesNamesStartIndex;
            if (courseConfigurationMessage.getSections()[3+i].split(";", -1).length == 5) {
                // this is the SailMaster protocol version 1.0 (May 2012) or later (see bug 1000), containing
                // a MarkType specification before the two tracker IDs:
                int markTypeIndex = Integer.valueOf(markDetails[2]);
                markType = MarkType.values()[markTypeIndex];
                devicesNamesStartIndex = 3;
            } else {
                devicesNamesStartIndex = 2;
            }
            marks.add(new MarkImpl(markDetails[1], Integer.valueOf(markDetails[0]),
                    Arrays.asList(markDetails).subList(devicesNamesStartIndex, markDetails.length),
                    markType));
        }
        return new CourseImpl(courseConfigurationMessage.getSections()[1], marks);
    }
    
    private String getLastTimeZoneSuffix(String raceID) {
        String result = lastTimeZoneSuffixPerRaceID.get(raceID);
        if (result == null) {
            int offset = TimeZone.getDefault().getOffset(System.currentTimeMillis())/1000/3600;
            result = (offset<0?"-":"+") + new DecimalFormat("00").format(offset)+"00";
            lastTimeZoneSuffixPerRaceID.put(raceID, result);
        }
        return result;
    }
    
    private String prefixTimeWithISOTodayAndSuffixWithTimezoneIndicator(String time, String raceID) {
        synchronized (dateFormat) {
            return dateFormat.format(new Date()).substring(0, "yyyy-mm-ddT".length())+time+getLastTimeZoneSuffix(raceID);
        }
    }

    private Date parseTimeAndDateISO(String timeAndDateISO, String raceID) throws ParseException {
        char timeZoneIndicator = timeAndDateISO.charAt(timeAndDateISO.length()-6);
        if ((timeZoneIndicator == '+' || timeZoneIndicator == '-') && timeAndDateISO.charAt(timeAndDateISO.length()-3) == ':') {
            timeAndDateISO = timeAndDateISO.substring(0, timeAndDateISO.length()-3)+timeAndDateISO.substring(timeAndDateISO.length()-2);
            lastTimeZoneSuffixPerRaceID.put(raceID, timeAndDateISO.substring(timeAndDateISO.length()-5));
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
        TimePoint result = null;
        if (messageLoader != null) {
            result = startTimePerRaceID.get(raceID);
        }
        if (result == null && canSendRequests()) {
            SailMasterMessage response = sendRequestAndGetResponse(MessageType.STT, raceID);
            String[] sections = response.getSections();
            assertResponseType(MessageType.STT, response);
            assertRaceID(raceID, sections[1]);
            result = new MillisecondsTimePoint(parseTimePrefixedWithISOToday(sections[2], raceID));
        }
        return result; 
    }

    private Date parseTimePrefixedWithISOToday(String timeHHMMSS, String raceID) throws ParseException {
        synchronized(dateFormat) {
            return dateFormat.parse(prefixTimeWithISOTodayAndSuffixWithTimezoneIndicator(timeHHMMSS, raceID));
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
                new MillisecondsTimePoint(parseTimePrefixedWithISOToday(clockAtMarkDetail[1], clockAtMarkMessage.getRaceID()));
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
