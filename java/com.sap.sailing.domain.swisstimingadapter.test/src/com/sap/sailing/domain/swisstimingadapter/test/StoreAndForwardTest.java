package com.sap.sailing.domain.swisstimingadapter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.sap.sailing.domain.swisstimingadapter.Course;
import com.sap.sailing.domain.swisstimingadapter.Mark;
import com.sap.sailing.domain.swisstimingadapter.Race;
import com.sap.sailing.domain.swisstimingadapter.RaceSpecificMessageLoader;
import com.sap.sailing.domain.swisstimingadapter.SailMasterAdapter;
import com.sap.sailing.domain.swisstimingadapter.SailMasterConnector;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;
import com.sap.sailing.domain.swisstimingadapter.SailMasterTransceiver;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingadapter.persistence.DomainObjectFactory;
import com.sap.sailing.domain.swisstimingadapter.persistence.MongoObjectFactory;
import com.sap.sailing.domain.swisstimingadapter.persistence.StoreAndForward;
import com.sap.sailing.domain.swisstimingadapter.persistence.impl.CollectionNames;
import com.sap.sailing.domain.swisstimingadapter.persistence.impl.FieldNames;
import com.sap.sailing.mongodb.Activator;

public class StoreAndForwardTest implements RaceSpecificMessageLoader {
    private static final Logger logger = Logger.getLogger(StoreAndForwardTest.class.getName());
    
    private static final int RECEIVE_PORT = 6543;
    private static final int CLIENT_PORT = 6544;
    
    final int COUNT = 10;
    final int STORED = 5;
    final int OVERLAP = 2;

    private DB db;
    private StoreAndForward storeAndForward;
    private Socket sendingSocket;
    private OutputStream sendingStream;
    private SailMasterTransceiver transceiver;
    private SailMasterConnector connector;
    private DomainObjectFactory domainObjectFactory;
    private ArrayList<SailMasterMessage> messagesToLoad;
    private SwissTimingFactory swissTimingFactory;
    private boolean loadMessagesCalled;
    private int messagesSent;

    private Thread bufferingMessageSenderThread;
    
    @Before
    public void setUp() throws UnknownHostException, IOException, InterruptedException {
        db = Activator.getDefaultInstance().getDB();
        storeAndForward = new StoreAndForward(RECEIVE_PORT, CLIENT_PORT, MongoObjectFactory.INSTANCE, SwissTimingFactory.INSTANCE);
        sendingSocket = new Socket("localhost", RECEIVE_PORT);
        sendingStream = sendingSocket.getOutputStream();
        swissTimingFactory = SwissTimingFactory.INSTANCE;
        transceiver = swissTimingFactory.createSailMasterTransceiver();
        connector = swissTimingFactory.createSailMasterConnector("localhost", CLIENT_PORT, this);
        DBCollection lastMessageCountCollection = db.getCollection(CollectionNames.LAST_MESSAGE_COUNT.name());
        lastMessageCountCollection.update(new BasicDBObject(), new BasicDBObject().append(FieldNames.LAST_MESSAGE_COUNT.name(), 0l),
                /* upsert */ true, /* multi */ false);
        DBCollection rawMessages = db.getCollection(CollectionNames.RAW_MESSAGES.name());
        rawMessages.drop();
        domainObjectFactory = DomainObjectFactory.INSTANCE;
        messagesToLoad = new ArrayList<SailMasterMessage>();
    }
    
    @After
    public void tearDown() throws InterruptedException, IOException {
        logger.entering(getClass().getName(), "tearDown");
        storeAndForward.stop();
        logger.exiting(getClass().getName(), "tearDown");
    }
    
    @Test
    public void testSimpleRACMessage() throws IOException, InterruptedException {
        final List<Race> racesReceived = new ArrayList<Race>();
        final boolean[] receivedSomething = new boolean[1];
        connector.addSailMasterListener(new SailMasterAdapter() {
            @Override
            public void receivedAvailableRaces(Iterable<Race> races) {
                for (Race race : races) {
                    racesReceived.add(race);
                }
                synchronized (StoreAndForwardTest.this) {
                    receivedSomething[0] = true;
                    StoreAndForwardTest.this.notifyAll();
                }
            }
        });
        String rawMessage = "RAC|2|4711;A wonderful test race|4712;Not such a wonderful race";
        transceiver.sendMessage(rawMessage, sendingStream);
        synchronized (this) {
            if (!receivedSomething[0]) {
                wait(2000l); // wait for two seconds to receive the message
            }
        }
        assertTrue(receivedSomething[0]);
        assertEquals(2, racesReceived.size());
        DBCollection lastMessageCountCollection = db.getCollection(CollectionNames.LAST_MESSAGE_COUNT.name());
        Long lastMessageCount = (Long) lastMessageCountCollection.findOne().get(FieldNames.LAST_MESSAGE_COUNT.name());
        assertEquals((Long) 1l, lastMessageCount);
        List<SailMasterMessage> rawMessages = domainObjectFactory.loadMessages(0);
        assertEquals(1, rawMessages.size());
        assertEquals(rawMessage, rawMessages.get(0).getMessage());
        assertEquals(0l, (long) rawMessages.get(0).getSequenceNumber());
    }
    
    /**
     * A {@link SailMasterConnector} can load messages from a {@link RaceSpecificMessageLoader} object when the
     * {@link SailMasterConnector#trackRace(String)} method is called. While it does so, messages received for the same
     * tracked race are buffered. When the loading of messages has finished, both, the loaded and buffered received
     * messages are parsed and notified to the listeners.<p>
     * 
     * This test asserts that the general buffering mechanism works and that both, stored and sent messages are
     * notified properly.
     */
    @Test
    public void testBuffering() throws UnknownHostException, IOException, InterruptedException, ParseException {
        final List<Course> coursesReceived = new ArrayList<Course>();
        final boolean[] receivedSomething = new boolean[1];
        connector.addSailMasterListener(new SailMasterAdapter() {
            @Override
            public void receivedCourseConfiguration(String raceID, Course course) {
                coursesReceived.add(course);
                synchronized (StoreAndForwardTest.this) {
                    receivedSomething[0] = true;
                    StoreAndForwardTest.this.notifyAll();
                }
            }
        });
        assert COUNT <= 10 && STORED < COUNT && STORED-OVERLAP >= 0;
        final String[] rawMessage = new String[COUNT];
        for (int i=0; i<COUNT; i++) {
            rawMessage[i] = "CCG|4711|2|1;Lee Gate;LG1;LG2|"+i+";Windward;WW1";
            if (i<STORED) {
                messagesToLoad.add(swissTimingFactory.createMessage(rawMessage[i], (long) i));
            }
        }
        logger.info("starting StoreAndForwardTest-testBufferingMessageSender thread");
        messagesSent = 0;
        bufferingMessageSenderThread = new Thread("StoreAndForwardTest-testBufferingMessageSender") {
            public void run() {
                try {
                    for (int i = 0; i < COUNT; i++) {
                        if (i >= STORED) {
                            // now wait for loadMessages to be called; that tells us that the trackRace
                            // call has happened and buffering should be activated
                            synchronized (StoreAndForwardTest.this) {
                                while (!loadMessagesCalled) {
                                    StoreAndForwardTest.this.wait();
                                }
                            }
                        }
                        transceiver.sendMessage(rawMessage[i], sendingStream);
                        synchronized (StoreAndForwardTest.this) {
                            messagesSent++;
                            StoreAndForwardTest.this.notifyAll();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        bufferingMessageSenderThread.start();
        connector.trackRace("4711"); // this should transitively invoke loadMessages which blocks until the first STORED messages have been sent
        assertTrue(loadMessagesCalled);
        synchronized (this) {
            int attempts = 0;
            while (coursesReceived.size() < COUNT && attempts++ < 2 * COUNT) {
                wait(2000l); // wait for two seconds to receive the message
            }
        }
        synchronized (this) {
            wait(500l); // wait another half second for spurious extra messages to be received
        }
        assertTrue(receivedSomething[0]);
        assertEquals(COUNT, coursesReceived.size());
        for (int i=0; i<COUNT; i++) {
            Course course = coursesReceived.get(i);
            Iterable<Mark> marks = course.getMarks();
            Mark lastMark = null;
            for (Mark mark : marks) {
                lastMark = mark;
            }
            assertEquals(i, lastMark.getIndex());
        }
    }

    @Override
    public List<SailMasterMessage> loadRaceMessages(String raceID) {
        synchronized (this) {
            // Wait until STORED-OVERLAP messages were transmitted; only then return the first STORED messages.
            // This should produce an overlap of OVERLAP messages
            while (messagesSent < STORED-OVERLAP) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            loadMessagesCalled = true;
            notifyAll();
        }
        // now wait until all messages have been transmitted to the receiver so that we know
        // that an overlap exists
        try {
            bufferingMessageSenderThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return messagesToLoad;
    }
}
