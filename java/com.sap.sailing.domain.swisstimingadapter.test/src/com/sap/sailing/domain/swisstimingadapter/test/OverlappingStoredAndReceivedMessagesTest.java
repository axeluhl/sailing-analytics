package com.sap.sailing.domain.swisstimingadapter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.swisstimingadapter.Course;
import com.sap.sailing.domain.swisstimingadapter.Mark;
import com.sap.sailing.domain.swisstimingadapter.Race;
import com.sap.sailing.domain.swisstimingadapter.RaceSpecificMessageLoader;
import com.sap.sailing.domain.swisstimingadapter.SailMasterAdapter;
import com.sap.sailing.domain.swisstimingadapter.SailMasterConnector;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;
import com.sap.sailing.domain.swisstimingadapter.SailMasterTransceiver;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;

public class OverlappingStoredAndReceivedMessagesTest implements RaceSpecificMessageLoader {
    private static final Logger logger = Logger.getLogger(OverlappingStoredAndReceivedMessagesTest.class.getName());

    private static final int PORT = 6543;

    final private int COUNT = 15;
    final private int STORED = 5;
    final private int OVERLAP = 2;
    final private int UNBUFFERED = 5;

    private ArrayList<SailMasterMessage> messagesToLoad;
    private boolean loadMessagesCalled;
    private int messagesSent;
    private Thread bufferingMessageSenderThread;

    private ServerSocket serverSocket;
    private Socket sendingSocket;
    private OutputStream sendingStream;
    private SailMasterTransceiver transceiver;
    private SailMasterConnector connector;
    private SwissTimingFactory swissTimingFactory;

    @Before
    public void setUp() throws UnknownHostException, IOException, InterruptedException {
        messagesToLoad = new ArrayList<SailMasterMessage>();
        new Thread("OverlappingStoredAndReceivedMessages-Connector") {

            public void run() {
                try {
                    serverSocket = new ServerSocket(PORT);
                    sendingSocket = serverSocket.accept();
                    sendingStream = sendingSocket.getOutputStream();
                    synchronized (OverlappingStoredAndReceivedMessagesTest.this) {
                        OverlappingStoredAndReceivedMessagesTest.this.notifyAll();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.start();
        swissTimingFactory = SwissTimingFactory.INSTANCE;
        transceiver = swissTimingFactory.createSailMasterTransceiver();
        connector = swissTimingFactory.getOrCreateSailMasterConnector("localhost", PORT, this, /* canSendRequests */ false);
        synchronized (this) {
            while (sendingStream == null) {
                wait();
            }
        }
    }
    
    @After
    public void tearDown() throws IOException {
        serverSocket.close();
        connector.stop();
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
                synchronized (OverlappingStoredAndReceivedMessagesTest.this) {
                    receivedSomething[0] = true;
                    OverlappingStoredAndReceivedMessagesTest.this.notifyAll();
                }
            }
        });
        assert STORED < COUNT-UNBUFFERED && STORED-OVERLAP >= 0;
        final String[] rawMessage = new String[COUNT];
        for (int i=0; i<COUNT; i++) {
            rawMessage[i] = "CCG|4711|2|1;Lee Gate;LG1;LG2|"+i+";Windward;WW1";
            if (i<STORED) {
                messagesToLoad.add(swissTimingFactory.createMessage(rawMessage[i], (long) i));
            }
        }
        logger.info("starting StoreAndForwardTest-testBufferingMessageSender thread");
        messagesSent = 0;
        bufferingMessageSenderThread = new Thread("OverlappingStoredAndReceivedMessages-Sender") {
            public void run() {
                try {
                    // first wait for loadMessages to be called; that tells us that the trackRace
                    // call has happened and buffering has been activated
                    synchronized (OverlappingStoredAndReceivedMessagesTest.this) {
                        while (!loadMessagesCalled) {
                            OverlappingStoredAndReceivedMessagesTest.this.wait();
                        }
                    }
                    // now transmit all messages; buffering will continue until loadMessages is
                    // unblocked by us sending the STORED-OVERLAPth message
                    for (int i = 0; i < COUNT-UNBUFFERED; i++) {
                        if (i >= STORED-OVERLAP) {
                            transceiver.sendMessage(swissTimingFactory.createMessage(rawMessage[i], (long) i), sendingStream);
                        }
                        synchronized (OverlappingStoredAndReceivedMessagesTest.this) {
                            messagesSent++;
                            OverlappingStoredAndReceivedMessagesTest.this.notifyAll();
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
        // send more messages after buffering
        for (int i=COUNT-UNBUFFERED; i<COUNT; i++) {
            transceiver.sendMessage(swissTimingFactory.createMessage(rawMessage[i], (long) i), sendingStream);
        }
        synchronized (this) {
            int attempts = 0;
            while (coursesReceived.size() < COUNT && attempts++ < 2 * COUNT) {
                wait(2000l); // wait for two seconds to receive the message
            }
        }
        synchronized (this) {
            wait(1000l); // wait another half second for spurious extra messages to be received
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
            loadMessagesCalled = true;
            notifyAll();
            // Wait until STORED-OVERLAP messages were sent; only then return the first STORED messages.
            // This will produce an overlap of OVERLAP messages
            while (messagesSent < STORED-OVERLAP) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
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

    @Override
    public Iterable<Race> getRaces() {
        return null;
    }

    @Override
    public Race getRace(String raceID) {
        return null;
    }

    @Override
    public boolean hasRaceStartlist(String raceID) {
        return false;
    }

    @Override
    public boolean hasRaceCourse(String raceID) {
        return false;
    }

    @Override
    public void storeSailMasterMessage(SailMasterMessage message) {
    }
}
