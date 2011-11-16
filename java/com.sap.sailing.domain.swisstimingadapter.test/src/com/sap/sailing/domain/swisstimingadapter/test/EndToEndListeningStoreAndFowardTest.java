package com.sap.sailing.domain.swisstimingadapter.test;

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
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
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
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.mongodb.Activator;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceImpl;

public class EndToEndListeningStoreAndFowardTest {
    private static final Logger logger = Logger.getLogger(EndToEndListeningStoreAndFowardTest.class.getName());
    
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
    
    private EmptyWindStore emptyWindStore;
    private RacingEventService racingEventService;

    @Before
    public void setUp() throws UnknownHostException, IOException, InterruptedException {
        logger.info("EndToEndListeningStoreAndFowardTest.setUp");
        
        db = Activator.getDefaultInstance().getDB();

        swissTimingAdapterPersistence = SwissTimingAdapterPersistence.INSTANCE;
        swissTimingAdapterPersistence.dropAllMessageData();
        swissTimingAdapterPersistence.dropAllRaceMasterData();
        
        storeAndForward = new StoreAndForward(RECEIVE_PORT, CLIENT_PORT, SwissTimingFactory.INSTANCE, swissTimingAdapterPersistence);
        sendingSocket = new Socket("localhost", RECEIVE_PORT);
        sendingStream = sendingSocket.getOutputStream();
        swissTimingFactory = SwissTimingFactory.INSTANCE;
        emptyWindStore = EmptyWindStore.INSTANCE;
        transceiver = swissTimingFactory.createSailMasterTransceiver();
//        connector = swissTimingFactory.getOrCreateSailMasterConnector("localhost", CLIENT_PORT, null);
        
        DBCollection lastMessageCountCollection = db.getCollection(CollectionNames.LAST_MESSAGE_COUNT.name());
        lastMessageCountCollection.update(new BasicDBObject(), new BasicDBObject().append(FieldNames.LAST_MESSAGE_COUNT.name(), 0l),
                /* upsert */ true, /* multi */ false);

        racingEventService = new RacingEventServiceImpl();
//        racingEventService.getSwissTimingFactory().getOrCreateSailMasterConnector(hostname, port, messageLoader)
    }
    
    @After
    public void tearDown() throws InterruptedException, IOException {
        logger.entering(getClass().getName(), "tearDown");
        storeAndForward.stop();
        logger.exiting(getClass().getName(), "tearDown");
    }
    
    @Test
    public void testEndToEndScenario() throws IOException, InterruptedException, ParseException {

        String[] racesToTrack = new String[] { "4711", "4712" };
        List<RaceHandle> raceHandles = new ArrayList<RaceHandle>();  
        
        for(String raceToTrack: racesToTrack) {
            RaceHandle raceHandle = racingEventService.addSwissTimingRace(raceToTrack, "localhost", CLIENT_PORT, emptyWindStore, -1);
            raceHandles.add(raceHandle);
            
            if(connector == null) {
                connector = racingEventService.getSwissTimingFactory().getOrCreateSailMasterConnector("localhost", CLIENT_PORT, swissTimingAdapterPersistence);
            }
        }

        InputStream is = getClass().getResourceAsStream("/InitMessagesScript.txt");

        ScriptedMessagesReader scriptedMessagesReader = new ScriptedMessagesReader();
        scriptedMessagesReader.addMessagesFromTextFile(is);

        final int messageCount = scriptedMessagesReader.getMessages().size();
        
        final int[] receivedMessagesCount = new int[] {0};
        final boolean[] receivedAll = new boolean[1];

        connector.addSailMasterListener(new SailMasterAdapter() {
            @Override
            public void receivedStartList(String raceID, StartList startList) {

                receivedMessagesCount[0] = receivedMessagesCount[0] + 1;
            }

            @Override
            public void receivedCourseConfiguration(String raceID, Course course) {
                receivedMessagesCount[0] = receivedMessagesCount[0] + 1;

                if(messageCount == receivedMessagesCount[0]) {
                    synchronized (EndToEndListeningStoreAndFowardTest.this) {
                        receivedAll[0] = true;
                        EndToEndListeningStoreAndFowardTest.this.notifyAll();
                    }
                }
                
            }

            
            @Override
            public void receivedAvailableRaces(Iterable<Race> races) {

                receivedMessagesCount[0] = receivedMessagesCount[0] + 1;
            }
        });

        for(String msg: scriptedMessagesReader.getMessages()) {
            transceiver.sendMessage(msg, sendingStream);
        }

        synchronized (this) {
            while (!receivedAll[0]) {
                wait(2000l); // wait for two seconds to receive the messages
            }
        }

        Iterable<Event> allEvents = racingEventService.getAllEvents();
        for (Event event : allEvents) {
            DynamicTrackedEvent trackedEvent = racingEventService.getTrackedEvent(event);

            Iterable<TrackedRace> trackedRaces = trackedEvent.getTrackedRaces();

            for (TrackedRace trackedRace : trackedRaces) {
                RaceDefinition race = trackedRace.getRace();
                
                System.out.println(race);
            }
        }
        

        
        for(RaceHandle raceHandle: raceHandles)
            racingEventService.stopTracking(raceHandle.getEvent());
        
        //for(String raceToTrack: racesToTrack)
        //    connector.stopTrackingRace(raceToTrack);

    }
    
}
