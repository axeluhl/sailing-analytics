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
import com.sap.sailing.domain.swisstimingadapter.Course;
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
import com.sap.sailing.mongodb.MongoDBService;

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
        MongoDBService mongoDBService = MongoDBService.INSTANCE;
        db = mongoDBService.getDB();
        swissTimingAdapterPersistence = SwissTimingAdapterPersistence.INSTANCE;
        swissTimingAdapterPersistence.dropAllRaceMasterData();
        swissTimingAdapterPersistence.dropAllMessageData();

        storeAndForward = new StoreAndForward(RECEIVE_PORT, CLIENT_PORT, SwissTimingFactory.INSTANCE,
                swissTimingAdapterPersistence, mongoDBService);
        sendingSocket = new Socket("localhost", RECEIVE_PORT);
        sendingStream = sendingSocket.getOutputStream();
        swissTimingFactory = SwissTimingFactory.INSTANCE;
        transceiver = swissTimingFactory.createSailMasterTransceiver();
        connector = swissTimingFactory.getOrCreateSailMasterConnector("localhost", CLIENT_PORT, swissTimingAdapterPersistence, /* canSendRequests */ false);
        DBCollection lastMessageCountCollection = db.getCollection(CollectionNames.LAST_MESSAGE_COUNT.name());
        lastMessageCountCollection.update(new BasicDBObject(),
                new BasicDBObject().append(FieldNames.LAST_MESSAGE_COUNT.name(), 0l),
                /* upsert */true, /* multi */false);

    }

    @After
    public void tearDown() throws InterruptedException, IOException {
        logger.entering(getClass().getName(), "tearDown");
        storeAndForward.stop();
        connector.stop();
        logger.exiting(getClass().getName(), "tearDown");
    }

    @Test
    public void testInitMessages() throws IOException, InterruptedException, ParseException {
        String[] racesToTrack = new String[] { "4711", "4712" };

        for (String raceToTrack : racesToTrack)
            connector.trackRace(raceToTrack);

        InputStream is = getClass().getResourceAsStream("/InitMessagesScript.txt");

        ScriptedMessages scriptedMessages = new ScriptedMessages(is);

        final int messageCount = scriptedMessages.getMessages().size();

        final int[] receivedMessagesCount = new int[] { 0 };
        final List<Race> racesReceived = new ArrayList<Race>();
        final boolean[] receivedAll = new boolean[1];
        final List<Competitor> receivedCompetitors = new ArrayList<Competitor>();
        final List<Course> receivedCourses  = new ArrayList<Course>();

        connector.addSailMasterListener(new SailMasterAdapter() {
            @Override
            public void receivedStartList(String raceID, StartList startList) {

                for (Competitor competitor : startList.getCompetitors())
                    receivedCompetitors.add(competitor);

                receivedMessagesCount[0] = receivedMessagesCount[0] + 1;
            }
            
            @Override
            public void receivedCourseConfiguration(String raceID, Course course) {

                receivedCourses.add(course);

                receivedMessagesCount[0] = receivedMessagesCount[0] + 1;

                if (messageCount == receivedMessagesCount[0]) {
                    synchronized (ScriptedStoreAndForwardTest.this) {
                        receivedAll[0] = true;
                        ScriptedStoreAndForwardTest.this.notifyAll();
                    }
                }

            }
            
            @Override
            public void receivedAvailableRaces(Iterable<Race> races) {

                for (Race race : races) {
                    racesReceived.add(race);
                }
                receivedMessagesCount[0] = receivedMessagesCount[0] + 1;
            }
        });

        for(String msg: scriptedMessages.getMessages()) {
            transceiver.sendMessage(msg, sendingStream);
        }
        synchronized (this) {
            while (!receivedAll[0]) {
                wait(2000l); // wait for two seconds to receive the messages
            }
        }
        assertEquals(2, racesReceived.size());
        assertEquals(5, receivedCompetitors.size());
        assertEquals(2, receivedCourses.size());
        
        for(String raceToTrack: racesToTrack)
            connector.stopTrackingRace(raceToTrack);
    }
    
}
