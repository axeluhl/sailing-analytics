package com.sap.sailing.domain.swisstimingadapter.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
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
import com.sap.sailing.domain.swisstimingadapter.Competitor;
import com.sap.sailing.domain.swisstimingadapter.Race;
import com.sap.sailing.domain.swisstimingadapter.SailMasterAdapter;
import com.sap.sailing.domain.swisstimingadapter.SailMasterConnector;
import com.sap.sailing.domain.swisstimingadapter.SailMasterTransceiver;
import com.sap.sailing.domain.swisstimingadapter.StartList;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingadapter.persistence.StoreAndForward;
import com.sap.sailing.domain.swisstimingadapter.persistence.SwissTimingAdapterPersistence;
import com.sap.sailing.domain.swisstimingadapter.persistence.impl.CollectionNames;
import com.sap.sailing.domain.swisstimingadapter.persistence.impl.FieldNames;
import com.sap.sailing.mongodb.Activator;

public class ScriptedStoreAndForwardTest {
    private static final Logger logger = Logger.getLogger(ScriptedStoreAndForwardTest.class.getName());
    
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

    @Before
    public void setUp() throws UnknownHostException, IOException, InterruptedException, ParseException {
        db = Activator.getDefaultInstance().getDB();

        swissTimingAdapterPersistence = SwissTimingAdapterPersistence.INSTANCE;
        
        storeAndForward = new StoreAndForward(RECEIVE_PORT, CLIENT_PORT, SwissTimingFactory.INSTANCE, SwissTimingAdapterPersistence.INSTANCE);
        sendingSocket = new Socket("localhost", RECEIVE_PORT);
        sendingStream = sendingSocket.getOutputStream();
        swissTimingFactory = SwissTimingFactory.INSTANCE;
        transceiver = swissTimingFactory.createSailMasterTransceiver();
        connector = swissTimingFactory.getOrCreateSailMasterConnector("localhost", CLIENT_PORT, swissTimingAdapterPersistence);
        DBCollection lastMessageCountCollection = db.getCollection(CollectionNames.LAST_MESSAGE_COUNT.name());
        lastMessageCountCollection.update(new BasicDBObject(), new BasicDBObject().append(FieldNames.LAST_MESSAGE_COUNT.name(), 0l),
                /* upsert */ true, /* multi */ false);
        
        swissTimingAdapterPersistence.dropAllRaceMasterData();
        swissTimingAdapterPersistence.dropAllMessageData();
        
        connector.trackRace("4711");
        connector.trackRace("4712");
    }
    
    @After
    public void tearDown() throws InterruptedException, IOException {
        logger.entering(getClass().getName(), "tearDown");
        storeAndForward.stop();
        connector.stop();
        logger.exiting(getClass().getName(), "tearDown");
    }
    
    @Test
    public void testInitMessages() throws IOException, InterruptedException {

        InputStream is = getClass().getResourceAsStream("/InitMessagesScript.txt");

        ScriptedMessages scriptedMessages = new ScriptedMessages(is);
        
        final List<Race> racesReceived = new ArrayList<Race>();
        final boolean[] receivedSomething = new boolean[1];
        final List<Competitor> receivedCompetitors  = new ArrayList<Competitor>();

        connector.addSailMasterListener(new SailMasterAdapter() {
            @Override
            public void receivedStartList(String raceID, StartList startList) {

                for(Competitor competitor: startList.getCompetitors())
                    receivedCompetitors.add(competitor);
                
                synchronized (ScriptedStoreAndForwardTest.this) {
                    receivedSomething[0] = true;
                    ScriptedStoreAndForwardTest.this.notifyAll();
                }
            }
            @Override
            public void receivedAvailableRaces(Iterable<Race> races) {
                for (Race race : races) {
                    racesReceived.add(race);
                }
            }
        });

        for(String msg: scriptedMessages.getMessages()) {
            transceiver.sendMessage(msg, sendingStream);
        }

        synchronized (this) {
            while (!receivedSomething[0]) {
                wait(2000l); // wait for two seconds to receive the messages
            }
        }
        assertEquals(2, racesReceived.size());
        assertEquals(4, receivedCompetitors.size());
        
    }
    
}
