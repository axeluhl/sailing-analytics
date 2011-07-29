package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.test.AbstractTracTracLiveTest;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.Receiver;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sailing.mongodb.impl.DomainObjectFactoryImpl;
import com.sap.sailing.mongodb.impl.MongoObjectFactoryImpl;
import com.sap.sailing.mongodb.impl.MongoWindStoreFactoryImpl;

public class TestStoringAndRetrievingWindTracks extends AbstractTracTracLiveTest implements MongoDBTest {

    private Mongo mongo;
    private DB db;

    public TestStoringAndRetrievingWindTracks() throws URISyntaxException, MalformedURLException {
        super();
    }
    
    private Mongo newMongo() throws UnknownHostException, MongoException {
        return new Mongo(System.getProperty("mongo.host", "127.0.0.1"),
                ((MongoWindStoreFactoryImpl) MongoWindStoreFactoryImpl.getDefaultInstance()).getDefaultPort());
    }
    
    @Before
    public void dropTestDB() throws UnknownHostException, MongoException {
        mongo = newMongo();
        assertNotNull(mongo);
        mongo.dropDatabase(WIND_TEST_DB);
        db = mongo.getDB(WIND_TEST_DB);
        assertNotNull(db);
    }
    
    @Test
    public void testStoreAFewWindEntries() throws UnknownHostException, MongoException, InterruptedException {
        DomainFactory domainFactory = DomainFactory.INSTANCE;
        Event domainEvent = domainFactory.createEvent(getEvent());
        DynamicTrackedEvent trackedEvent = domainFactory.getOrCreateTrackedEvent(domainEvent);
        Iterable<Receiver> typeControllers = domainFactory.getUpdateReceivers(trackedEvent, getEvent(),
                EmptyWindStore.INSTANCE, ReceiverType.RACECOURSE);
        addListenersForStoredDataAndStartController(typeControllers);
        RaceDefinition race = domainFactory.getRaceDefinition(getEvent().getRaceList().iterator().next());
        DynamicTrackedRace trackedRace = domainFactory.trackRace(trackedEvent, race, /* millisecondsOverWhichToAverageWind */
                EmptyWindStore.INSTANCE, /* millisecondsOverWhichToAverageSpeed */
                30000, 10000, getEvent(), this);
        WindSource windSource = WindSource.WEB;
        Mongo myFirstMongo = newMongo();
        DB firstDatabase = myFirstMongo.getDB(WIND_TEST_DB);
        new MongoObjectFactoryImpl(firstDatabase).addWindTrackDumper(trackedEvent, trackedRace, windSource);
        WindTrack windTrack = trackedRace.getWindTrack(windSource);
        Position pos = new DegreePosition(54, 9);
        for (double bearingDeg = 123.4; bearingDeg<140; bearingDeg += 1.1) {
            windTrack.add(new WindImpl(pos, MillisecondsTimePoint.now(), new KnotSpeedWithBearingImpl(10., new DegreeBearingImpl(bearingDeg))));
            Thread.sleep(1); // ensure that the next now() call is distinguishably later
        }
        Thread.sleep(1000); // give MongoDB some time to make written data available to other connections
        
        Mongo mySecondMongo = newMongo();
        DB secondDatabase = mySecondMongo.getDB(WIND_TEST_DB);
        WindTrack result = new DomainObjectFactoryImpl(secondDatabase).loadWindTrack(domainEvent, race, windSource, /* millisecondsOverWhichToAverage */
                30000);
        double myBearingDeg = 123.4;
        for (Wind wind : result.getRawFixes()) {
            assertEquals(pos, wind.getPosition());
            assertEquals(10., wind.getKnots(), 0.000000000001);
            assertEquals(myBearingDeg, wind.getBearing().getDegrees(), 0.000000001);
            myBearingDeg += 1.1;
        }
        assertTrue("Expected myBeaaringDeg to be >= 139.999999999 but was "+myBearingDeg, myBearingDeg >= 139.999999999);
    }
}
