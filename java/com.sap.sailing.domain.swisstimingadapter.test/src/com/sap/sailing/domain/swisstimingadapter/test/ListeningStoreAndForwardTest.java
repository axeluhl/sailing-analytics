package com.sap.sailing.domain.swisstimingadapter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

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

public class ListeningStoreAndForwardTest {
    private static final Logger logger = Logger.getLogger(ListeningStoreAndForwardTest.class.getName());
    
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

    @Rule public Timeout AbstractTracTracLiveTestTimeout = new Timeout(5 * 60 * 1000); // timeout after 5 minutes

    @Before
    public void setUp() throws UnknownHostException, IOException, InterruptedException {
        logger.info("ListeningStoreAndForwardTest.setUp");
        MongoDBService mongoDBService = MongoDBService.INSTANCE;
        db = mongoDBService.getDB();
        swissTimingAdapterPersistence = SwissTimingAdapterPersistence.INSTANCE;
        swissTimingAdapterPersistence.dropAllMessageData();
        storeAndForward = new StoreAndForward(RECEIVE_PORT, CLIENT_PORT, SwissTimingFactory.INSTANCE, swissTimingAdapterPersistence, mongoDBService);
        sendingSocket = new Socket("localhost", RECEIVE_PORT);
        sendingStream = sendingSocket.getOutputStream();
        swissTimingFactory = SwissTimingFactory.INSTANCE;
        transceiver = swissTimingFactory.createSailMasterTransceiver();
        connector = swissTimingFactory.getOrCreateSailMasterConnector("localhost", CLIENT_PORT, null, /* canSendRequests */ false);
        DBCollection lastMessageCountCollection = db.getCollection(CollectionNames.LAST_MESSAGE_COUNT.name());
        lastMessageCountCollection.update(new BasicDBObject(), new BasicDBObject().append(FieldNames.LAST_MESSAGE_COUNT.name(), 0l),
                /* upsert */ true, /* multi */ false);
    }
    
    @After
    public void tearDown() throws InterruptedException, IOException {
        logger.entering(getClass().getName(), "tearDown");
        storeAndForward.stop();
        connector.stop();
        logger.exiting(getClass().getName(), "tearDown");
    }
    
    @Test
    public void testSimpleRACMessage() throws IOException, InterruptedException {
        logger.info("Starting ListeningStoreAndForwardTest.testSimpleRACMessage");
        final List<Race> racesReceived = new ArrayList<Race>();
        final boolean[] receivedSomething = new boolean[1];
        connector.addSailMasterListener(new SailMasterAdapter() {
            @Override
            public void receivedAvailableRaces(Iterable<Race> races) {
                for (Race race : races) {
                    racesReceived.add(race);
                }
                synchronized (ListeningStoreAndForwardTest.this) {
                    receivedSomething[0] = true;
                    ListeningStoreAndForwardTest.this.notifyAll();
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
        List<SailMasterMessage> rawMessages = swissTimingAdapterPersistence.loadCommandMessages(0);
        assertEquals(1, rawMessages.size());
        assertEquals(rawMessage, rawMessages.get(0).getMessage());
        assertEquals(0l, (long) rawMessages.get(0).getSequenceNumber());
    }
    
}
