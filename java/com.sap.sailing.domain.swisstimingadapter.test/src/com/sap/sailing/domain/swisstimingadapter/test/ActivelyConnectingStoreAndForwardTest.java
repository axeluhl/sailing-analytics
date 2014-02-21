package com.sap.sailing.domain.swisstimingadapter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.sap.sailing.domain.swisstimingadapter.Race;
import com.sap.sailing.domain.swisstimingadapter.SailMasterAdapter;
import com.sap.sailing.domain.swisstimingadapter.SailMasterConnector;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;
import com.sap.sailing.domain.swisstimingadapter.SailMasterTransceiver;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingadapter.persistence.StoreAndForward;
import com.sap.sailing.domain.swisstimingadapter.persistence.SwissTimingAdapterPersistence;
import com.sap.sailing.domain.swisstimingadapter.persistence.impl.CollectionNames;
import com.sap.sailing.domain.swisstimingadapter.persistence.impl.FieldNames;
import com.sap.sailing.mongodb.MongoDBService;

public class ActivelyConnectingStoreAndForwardTest {
    private static final Logger logger = Logger.getLogger(ActivelyConnectingStoreAndForwardTest.class.getName());
    
    private static final int RECEIVE_PORT = 6543;
    private static final int CLIENT_PORT = 6544;
    
    private DB db;
    private StoreAndForward storeAndForward;
    private Socket sendingSocket;
    private OutputStream sendingStream;
    private SailMasterTransceiver transceiver;
    private SailMasterConnector connector;
    private SwissTimingAdapterPersistence swissTimingAdapterPersistence;
    private SwissTimingFactory swissTimingFactory;

    private Thread sailMasterDummyListenerThread;

    @Before
    public void setUp() throws UnknownHostException, IOException, InterruptedException {
        MongoDBService mongoDBService = MongoDBService.INSTANCE;
        db = mongoDBService.getDB();
        sailMasterDummyListenerThread = new Thread("ActivelyConnectingStoreAndForwardTest-Listener") {
            public void run() {
                try {
                    ServerSocket ss = null;
                    for (int numberOfTries=0; numberOfTries<3 && ss == null; numberOfTries++) {
                        try {
                            ss = new ServerSocket(RECEIVE_PORT);
                        } catch (BindException be) {
                            logger.log(Level.INFO, "Couldn't bind server socket in StoreAndForward"+
                               (numberOfTries<2?". Trying again...":""), be);
                            if (numberOfTries == 2) {
                                throw be;
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    synchronized (ActivelyConnectingStoreAndForwardTest.this) {
                        sendingSocket = ss.accept();
                        logger.info("ActivelyConnectingStoreAndForwardTest-Listener accepted socket connect request on port "+RECEIVE_PORT);
                        sendingStream = sendingSocket.getOutputStream();
                        ActivelyConnectingStoreAndForwardTest.this.notifyAll();
                    }
                    logger.info("ActivelyConnectingStoreAndForwardTest-Listener closing server socket on port "+RECEIVE_PORT);
                    ss.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        sailMasterDummyListenerThread.start();
        swissTimingAdapterPersistence = SwissTimingAdapterPersistence.INSTANCE;
        swissTimingAdapterPersistence.dropAllMessageData();
        storeAndForward = new StoreAndForward("localhost", RECEIVE_PORT, CLIENT_PORT, SwissTimingFactory.INSTANCE, 
                swissTimingAdapterPersistence, mongoDBService);
        synchronized (this) {
            while (sendingStream == null) {
                wait();
            }
        }
        swissTimingFactory = SwissTimingFactory.INSTANCE;
        transceiver = swissTimingFactory.createSailMasterTransceiver();
        connector = swissTimingFactory.getOrCreateSailMasterConnector("localhost", CLIENT_PORT, null, /* canSendRequests */ true); // will connect to RECEIVE_PORT
        DBCollection lastMessageCountCollection = db.getCollection(CollectionNames.LAST_MESSAGE_COUNT.name());
        lastMessageCountCollection.update(new BasicDBObject(), new BasicDBObject().append(FieldNames.LAST_MESSAGE_COUNT.name(), 0l),
                /* upsert */ true, /* multi */ false);
    }
    
    @After
    public void tearDown() throws InterruptedException, IOException {
        logger.entering(getClass().getName(), "tearDown");
        storeAndForward.stop();
        sailMasterDummyListenerThread.join();
        connector.stop();
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
                synchronized (ActivelyConnectingStoreAndForwardTest.this) {
                    receivedSomething[0] = true;
                    ActivelyConnectingStoreAndForwardTest.this.notifyAll();
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
        db.getLastError(); // synchronize on DB and wait for last write to have completed
        Thread.sleep(2000); // the synchronization doesn't seem to help
        DBCollection lastMessageCountCollection = db.getCollection(CollectionNames.LAST_MESSAGE_COUNT.name());
        Long lastMessageCount = (Long) lastMessageCountCollection.findOne().get(FieldNames.LAST_MESSAGE_COUNT.name());
        assertEquals((Long) 1l, lastMessageCount);
        List<SailMasterMessage> rawMessages = swissTimingAdapterPersistence.loadCommandMessages(0);
        assertEquals(1, rawMessages.size());
        assertEquals(rawMessage, rawMessages.get(0).getMessage());
        assertEquals(0l, (long) rawMessages.get(0).getSequenceNumber());
    }
    
}
