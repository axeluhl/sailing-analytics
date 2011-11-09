package com.sap.sailing.domain.swisstimingadapter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.sap.sailing.domain.swisstimingadapter.MessageType;
import com.sap.sailing.domain.swisstimingadapter.Race;
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

public class StoreAndForwardTest {
    private static final int RECEIVE_PORT = 6543;
    private static final int CLIENT_PORT = 6544;
    
    private DB db;
    private StoreAndForward storeAndForward;
    private Thread storeAndForwardThread;
    private Socket sendingSocket;
    private OutputStream sendingStream;
    private SailMasterTransceiver transceiver;
    private SailMasterConnector connector;
    private DomainObjectFactory domainObjectFactory;
    
    @Before
    public void setUp() throws UnknownHostException, IOException, InterruptedException {
        db = Activator.getDefaultInstance().getDB();
        storeAndForward = new StoreAndForward(RECEIVE_PORT, CLIENT_PORT, MongoObjectFactory.INSTANCE, SwissTimingFactory.INSTANCE);
        storeAndForwardThread = new Thread(storeAndForward, "StoreAndForward");
        storeAndForwardThread.start();
        sendingSocket = new Socket("localhost", RECEIVE_PORT);
        sendingStream = sendingSocket.getOutputStream();
        transceiver = SwissTimingFactory.INSTANCE.createSailMasterTransceiver();
        connector = SwissTimingFactory.INSTANCE.createSailMasterConnector("localhost", CLIENT_PORT);
        DBCollection lastMessageCountCollection = db.getCollection(CollectionNames.LAST_MESSAGE_COUNT.name());
        lastMessageCountCollection.update(new BasicDBObject(), new BasicDBObject().append(FieldNames.LAST_MESSAGE_COUNT.name(), 0l),
                /* upsert */ true, /* multi */ false);
        DBCollection rawMessages = db.getCollection(CollectionNames.RAW_MESSAGES.name());
        rawMessages.drop();
        domainObjectFactory = DomainObjectFactory.INSTANCE;
    }
    
    @After
    public void tearDown() throws InterruptedException, IOException {
        storeAndForward.stop();
        transceiver.sendMessage(MessageType._STOPSERVER.name(), sendingStream); // forwards to connector and hence stops it
        storeAndForwardThread.join();
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
}
