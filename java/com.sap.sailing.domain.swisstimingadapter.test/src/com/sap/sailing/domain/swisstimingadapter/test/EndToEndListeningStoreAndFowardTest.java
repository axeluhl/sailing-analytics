package com.sap.sailing.domain.swisstimingadapter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.persistence.media.MediaDB;
import com.sap.sailing.domain.racelog.impl.EmptyRaceLogStore;
import com.sap.sailing.domain.swisstimingadapter.MessageType;
import com.sap.sailing.domain.swisstimingadapter.SailMasterConnector;
import com.sap.sailing.domain.swisstimingadapter.SailMasterTransceiver;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingAdapter;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingadapter.impl.SwissTimingAdapterFactoryImpl;
import com.sap.sailing.domain.swisstimingadapter.persistence.StoreAndForward;
import com.sap.sailing.domain.swisstimingadapter.persistence.SwissTimingAdapterPersistence;
import com.sap.sailing.domain.swisstimingadapter.persistence.impl.CollectionNames;
import com.sap.sailing.domain.swisstimingadapter.persistence.impl.FieldNames;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tracking.impl.TrackedRaceStatusImpl;
import com.sap.sailing.mongodb.MongoDBService;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.impl.RacingEventServiceImpl;

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

    private EmptyRaceLogStore emptyRaceLogStore;
    private RacingEventService racingEventService;
    private SwissTimingAdapter swissTimingAdapter;
    private List<RacesHandle> raceHandles;
    
    @Rule public Timeout AbstractTracTracLiveTestTimeout = new Timeout(5 * 60 * 1000); // timeout after 5 minutes

    @Before
    public void setUp() throws UnknownHostException, IOException, InterruptedException {
        logger.info("EndToEndListeningStoreAndFowardTest.setUp");
        MongoDBService mongoDBService = MongoDBService.INSTANCE;
        db = mongoDBService.getDB();
        db.getCollection(com.sap.sailing.domain.persistence.impl.CollectionNames.REGATTAS.name()).drop();
        swissTimingAdapterPersistence = SwissTimingAdapterPersistence.INSTANCE;
        swissTimingAdapterPersistence.dropAllMessageData();
        swissTimingAdapterPersistence.dropAllRaceMasterData();
        storeAndForward = null;
        for (int numberOfTries = 0; numberOfTries < 3 && storeAndForward == null; numberOfTries++) {
            try {
                storeAndForward = new StoreAndForward(RECEIVE_PORT, CLIENT_PORT, SwissTimingFactory.INSTANCE,
                        swissTimingAdapterPersistence, mongoDBService);
            } catch (BindException be) {
                logger.log(Level.INFO, "Couldn't bind server socket in StoreAndForward"+
                   (numberOfTries<2?". Trying again...":""), be);
                if (numberOfTries == 2) {
                    throw be;
                }
                Thread.sleep(100);
            }
        }
        sendingSocket = new Socket("localhost", RECEIVE_PORT);
        sendingStream = sendingSocket.getOutputStream();
        swissTimingFactory = SwissTimingFactory.INSTANCE;
        EmptyWindStore emptyWindStore = EmptyWindStore.INSTANCE;
        emptyRaceLogStore = EmptyRaceLogStore.INSTANCE;
        transceiver = swissTimingFactory.createSailMasterTransceiver();
        DBCollection lastMessageCountCollection = db.getCollection(CollectionNames.LAST_MESSAGE_COUNT.name());
        lastMessageCountCollection.update(new BasicDBObject(),
                new BasicDBObject().append(FieldNames.LAST_MESSAGE_COUNT.name(), 0l),
                /* upsert */true, /* multi */false);
        // important: construct a new domain factory each time to make sure the competitor cache starts out empty
        final com.sap.sailing.domain.base.impl.DomainFactoryImpl baseDomainFactory = new com.sap.sailing.domain.base.impl.DomainFactoryImpl();
        swissTimingAdapter = new SwissTimingAdapterFactoryImpl().getOrCreateSwissTimingAdapter(baseDomainFactory, swissTimingAdapterPersistence);
        racingEventService = new RacingEventServiceImpl(PersistenceFactory.INSTANCE.getDomainObjectFactory(
                mongoDBService, baseDomainFactory), PersistenceFactory.INSTANCE.getMongoObjectFactory(mongoDBService), MediaDB.TEST_STUB, emptyWindStore);
        raceHandles = new ArrayList<RacesHandle>();
    }

    @After
    public void tearDown() throws InterruptedException, IOException {
        logger.entering(getClass().getName(), "tearDown");
        for (RacesHandle raceHandle : raceHandles) {
            racingEventService.stopTracking(raceHandle.getRegatta());
        }
        logger.info("Calling StoreAndForward.stop() in tearDown");
        if (storeAndForward != null) {
            storeAndForward.stop();
        }
        logger.exiting(getClass().getName(), "tearDown");
    }

     @Test
    public void testEndToEndScenarioWithInitMessages() throws Exception {
        String[] racesToTrack = new String[] { "4711", "4712" };
        String scriptName = "/InitMessagesScript.txt";
        setUpUsingScript(racesToTrack, scriptName);

        Set<TrackedRace> allTrackedRaces = getAllTrackedRaces();
        assertEquals(2, Util.size(allTrackedRaces));
        Set<String> raceIDs = new HashSet<String>();
        for (TrackedRace trackedRace : allTrackedRaces) {
            RaceDefinition race = trackedRace.getRace();
            raceIDs.add(race.getName());
        }
        Set<String> expectedRaceIDs = new HashSet<String>();
        for (String raceIDToTrack : new String[] { "4711", "4712" }) {
            expectedRaceIDs.add(raceIDToTrack);
        }
        assertEquals(expectedRaceIDs, raceIDs);
    }

    @Test
    public void testLongRaceLog() throws Exception {
        String[] racesToTrack = new String[] { "W4702" };
        String scriptName1 = "/SailMasterDataInterfaceRACandSTL.txt";
        String scriptName2 = "/SailMasterDataInterface-ExampleAsText.txt";
        setUpUsingScript(racesToTrack, scriptName1, scriptName2);
        coreOfTestLongRaceLog();
    }

    @Test
    public void testLongRaceLogNewVersion() throws Exception {
        String[] racesToTrack = new String[] { "W4702" };
        String scriptName1 = "/W4702RACandSTLandCCG.txt";
        String scriptName2 = "/W4702AsText.txt";
        setUpUsingScript(racesToTrack, scriptName1, scriptName2);
        coreOfTestLongRaceLog();
    }

    private void coreOfTestLongRaceLog() {
        Set<TrackedRace> allTrackedRaces = getAllTrackedRaces();
        assertEquals(1, Util.size(allTrackedRaces));
        Set<RaceDefinition> races = raceHandles.iterator().next().getRaceTracker().getRaces();
        assertEquals(1, races.size());
        RaceDefinition raceFromTracker = races.iterator().next();
        assertNotNull(raceFromTracker);
        Set<String> raceIDs = new HashSet<String>();
        for (TrackedRace trackedRace : allTrackedRaces) {
            RaceDefinition race = trackedRace.getRace();
            raceIDs.add(race.getName());
            assertEquals(46, Util.size(race.getCompetitors()));
            assertEquals(7, Util.size(race.getCourse().getWaypoints()));
            assertEquals(6, Util.size(race.getCourse().getLegs()));
            for (Competitor competitor : race.getCompetitors()) {
                if (!competitor.getName().equals("Competitor 35") && !competitor.getName().equals("Competitor 20")
                        && !competitor.getName().equals("GBR 831") && !competitor.getName().equals("NED 24")) {
                    final GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
                    track.lockForRead();
                    try {
                        assertTrue("Track of competitor " + competitor + " empty", !Util.isEmpty(track.getRawFixes()));
                    } finally {
                        track.unlockAfterRead();
                    }
                }
            }
            Set<Mark> marks = new HashSet<Mark>();
            for (Waypoint waypoint : race.getCourse().getWaypoints()) {
                for (Mark mark : waypoint.getMarks()) {
                    marks.add(mark);
                }
            }
            for (Mark mark : marks) {
                final GPSFixTrack<Mark, GPSFix> track = trackedRace.getOrCreateTrack(mark);
                track.lockForRead();
                try {
                    assertTrue("Track of mark " + mark + " empty",
                            !Util.isEmpty(track.getRawFixes()));
                } finally {
                    track.unlockAfterRead();
                }
            }
        }
        Set<String> expectedRaceIDs = new HashSet<String>();
        for (String raceIDToTrack : new String[] { "W4702" }) {
            expectedRaceIDs.add(raceIDToTrack);
        }
        assertEquals(expectedRaceIDs, raceIDs);
    }
    
    @Test
    public void testLongLogRaceNewConfig() throws Exception {
        String[] racesToTrack = new String[] { "W4702" };
        String scriptName1 = "/SailMasterDataInterfaceRACandSTL.txt";
        String scriptName2 = "/SailMasterDataInterface-ExampleAsText.txt";
        String scriptNewCourseConfig = "/SailMasterDataInterfaceNewCourseConfig.txt";
        setUpUsingScript(racesToTrack, scriptName1, scriptName2, scriptNewCourseConfig);
        Set<TrackedRace> allNewTrackedRaces = getAllTrackedRaces();
        assertEquals(1, Util.size(allNewTrackedRaces));
        Set<RaceDefinition> races = raceHandles.iterator().next().getRaceTracker().getRaces();
        assertEquals(1, races.size());
        RaceDefinition raceFromTracker = races.iterator().next();
        assertNotNull(raceFromTracker);
        Set<String> raceIDs = new HashSet<String>();
        for (TrackedRace trackedRace : allNewTrackedRaces) {
            RaceDefinition race = trackedRace.getRace();
            raceIDs.add(race.getName());
            assertEquals(46, Util.size(race.getCompetitors()));
            assertEquals(3, Util.size(race.getCourse().getWaypoints()));
            assertEquals(2, Util.size(race.getCourse().getLegs()));
            for (Competitor competitor : race.getCompetitors()) {
                if (!competitor.getName().equals("NED 24") && !competitor.getName().equals("Competitor 35")
                        && !competitor.getName().equals("Competitor 20") && !competitor.getName().equals("GBR 831")) {
                    final GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
                    track.lockForRead();
                    try {
                        assertTrue("Track of competitor " + competitor + " empty", !Util.isEmpty(track.getRawFixes()));
                    } finally {
                        track.unlockAfterRead();
                    }
                }
            }
            Set<Mark> marks = new HashSet<Mark>();
            for (Waypoint waypoint : race.getCourse().getWaypoints()) {
                for (Mark mark : waypoint.getMarks()) {
                    marks.add(mark);
                }
            }
            for (Mark mark : marks) {
                final GPSFixTrack<Mark, GPSFix> track = trackedRace.getOrCreateTrack(mark);
                track.lockForRead();
                try {
                    assertTrue("Track of mark " + mark + " empty", !Util.isEmpty(track.getRawFixes()));
                } finally {
                    track.unlockAfterRead();
                }
            }
        }
        Set<String> expectedRaceIDs = new HashSet<String>();
        for (String raceIDToTrack : new String[] { "W4702" }) {
            expectedRaceIDs.add(raceIDToTrack);
        }
        assertEquals(expectedRaceIDs, raceIDs);
    }

    @Test
    public void testDuplicateCCGMessageAndWaypointUniqueness() throws Exception {
        String[] racesToTrack = new String[] { "W4702" };
        setUpUsingScript(racesToTrack, "/DuplicateCCG.txt");

        Set<TrackedRace> allTrackedRaces = getAllTrackedRaces();
        assertEquals(1, allTrackedRaces.size());
        TrackedRace trackedRace = allTrackedRaces.iterator().next();
        List<Waypoint> waypoints = new ArrayList<Waypoint>();
        for (Waypoint waypoint : trackedRace.getRace().getCourse().getWaypoints()) {
            waypoints.add(waypoint);
        }
        assertEquals(7, Util.size(waypoints));
    }
    
    @Test
    public void testTMDMessageBeforeReceivingStartTime() throws Exception {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        String[] racesToTrack = new String[] { "SAM005923" };
        setUpUsingScript(racesToTrack, "/TMDBeforeStartTimeExample.txt");
        Set<TrackedRace> allTrackedRaces = getAllTrackedRaces();
        assertEquals(1, allTrackedRaces.size());
        TrackedRace trackedRace = allTrackedRaces.iterator().next();
        assertEquals("SAM005923", trackedRace.getRace().getName());
        assertEquals(dateFormat.parse("2013-04-04T13:45:00+0200"), trackedRace.getStartOfRace().asDate());
        for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
            if (competitor.getBoat().getSailID().equals("GBR-828")) {
                NavigableSet<MarkPassing> markPassings = trackedRace.getMarkPassings(competitor);
                assertEquals(1, markPassings.size());
                assertNotNull(markPassings.iterator().next().getTimePoint());
            }
        }
    }

    private Set<TrackedRace> getAllTrackedRaces() {
        Set<TrackedRace> allTrackedRaces = new HashSet<TrackedRace>();
        Iterable<Regatta> allEvents = racingEventService.getAllRegattas();
        for (Regatta event : allEvents) {
            DynamicTrackedRegatta trackedRegatta = racingEventService.getTrackedRegatta(event);
            Iterable<TrackedRace> trackedRaces = trackedRegatta.getTrackedRaces();
            for (TrackedRace trackedRace : trackedRaces) {
                allTrackedRaces.add(trackedRace);
            }
        }
        return allTrackedRaces;
    }
    
    @Test
    public void testTMDMessageBeforeReceivingStartTimeWithManyTMDs() throws Exception {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        String[] racesToTrack = new String[] { "SAM005923" };
        setUpUsingScript(racesToTrack, "/TMDBeforeStartTimeExample_ManyTMDs.txt");
        Set<TrackedRace> allTrackedRaces = getAllTrackedRaces();
        assertEquals(1, allTrackedRaces.size());
        TrackedRace trackedRace = allTrackedRaces.iterator().next();
        assertEquals("SAM005923", trackedRace.getRace().getName());
        assertEquals(dateFormat.parse("2013-04-04T13:45:00+0200"), trackedRace.getStartOfRace().asDate());
        for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
            NavigableSet<MarkPassing> markPassings = trackedRace.getMarkPassings(competitor);
            if (competitor.getBoat().getSailID().equals("GBR-828")) {
                assertEquals(7, markPassings.size());
            }
            assertNotNull(markPassings.iterator().next().getTimePoint());
            assertEquals(trackedRace.getStartOfRace(), markPassings.iterator().next().getTimePoint());
        }
    }
    
    @Test
    public void testRongRaceLogRACZero() throws Exception{
        String[] racesToTrack = new String[] { "W4702" };
        String scriptName2 = "/SailMasterDataInterfaceRACZero.txt";
        setUpUsingScript(racesToTrack, scriptName2);
        Set<TrackedRace> allNewTrackedRaces = getAllTrackedRaces();
        assertEquals(0, Util.size(allNewTrackedRaces));
    }

    @Test
    public void testEndToEndWithSwissTimingData() throws Exception {
        String[] racesToTrack = new String[] { "W4702" };
        String scriptName1 = "/SailMasterDataInterfaceRACandSTL.txt";
        String scriptName2 = "/SailMasterDataInterface-ExampleAsText.txt";
        setUpUsingScript(racesToTrack, scriptName1, scriptName2);
        assertFalse(getAllTrackedRaces().isEmpty());
    }

    private void setUpUsingScript(String[] racesToTrack, String... scriptNames) throws Exception {
        for (String raceToTrack : racesToTrack) {
            RacesHandle raceHandle = swissTimingAdapter.addSwissTimingRace(racingEventService,
                    /* regattaToAddTo */ null /* use a default regatta */, raceToTrack, /* canSendRequests */
                    "localhost", CLIENT_PORT, false, emptyRaceLogStore, -1);
            raceHandles.add(raceHandle);
            if (connector == null) {
                connector = swissTimingAdapter.getSwissTimingFactory().getOrCreateSailMasterConnector("localhost",
                        CLIENT_PORT, swissTimingAdapterPersistence, /* canSendRequests */false);
            }
        }
        ScriptedMessagesReader scriptedMessagesReader = new ScriptedMessagesReader();
        for (String scriptName : scriptNames) {
            InputStream is = getClass().getResourceAsStream(scriptName);
            scriptedMessagesReader.addMessagesFromTextFile(is);
        }
        Set<TrackedRace> trackedRacesSoFar = new HashSet<>();
        int i=0;
        int numberOfMessages = scriptedMessagesReader.getMessages().size();
        for (String msg : scriptedMessagesReader.getMessages()) {
            transceiver.sendMessage(msg, sendingStream);
            i++;
            for (TrackedRace trackedRace : getAllTrackedRaces()) {
                if (!trackedRacesSoFar.contains(trackedRace)) {
                    trackedRacesSoFar.add(trackedRace);
                    ((DynamicTrackedRace) trackedRace).setStatus(new TrackedRaceStatusImpl(TrackedRaceStatusEnum.LOADING, ((double) i) / (double) numberOfMessages));
                }
            }
        }
        for (TrackedRace trackedRace : getAllTrackedRaces()) {
            ((DynamicTrackedRace) trackedRace).setStatus(new TrackedRaceStatusImpl(TrackedRaceStatusEnum.FINISHED, 1.0));
        }
        transceiver.sendMessage(swissTimingFactory.createMessage(MessageType._STOPSERVER.name(), null), sendingStream);
        synchronized (connector) {
            while (!connector.isStopped()) {
                connector.wait();
            }
        }
    }

}
