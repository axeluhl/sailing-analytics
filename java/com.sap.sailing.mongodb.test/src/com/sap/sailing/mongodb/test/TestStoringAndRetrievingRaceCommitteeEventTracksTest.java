package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEvent;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEventFactory;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEventTrack;
import com.sap.sailing.domain.racecommittee.RaceCommitteeStartTimeEvent;
import com.sap.sailing.domain.racecommittee.impl.EmptyRaceCommitteeStore;
import com.sap.sailing.domain.test.AbstractTracTracLiveTest;
import com.sap.sailing.domain.tracking.DynamicRaceDefinitionSet;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.Receiver;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sailing.mongodb.MongoDBConfiguration;
import com.sap.sailing.server.impl.RacingEventServiceImpl;

public class TestStoringAndRetrievingRaceCommitteeEventTracksTest extends AbstractTracTracLiveTest {

    private Mongo mongo;
    private DB db;
    
    private final MongoDBConfiguration dbConfiguration;

    public TestStoringAndRetrievingRaceCommitteeEventTracksTest() throws URISyntaxException, MalformedURLException {
        super();
        dbConfiguration = MongoDBConfiguration.getDefaultTestConfiguration();
    }
    
    private Mongo newMongo() throws UnknownHostException, MongoException {
        return new Mongo(System.getProperty("mongo.host", "127.0.0.1"),
                dbConfiguration.getPort());
    }
    
    @Before
    public void dropTestDB() throws UnknownHostException, MongoException {
        mongo = newMongo();
        assertNotNull(mongo);
        mongo.dropDatabase(dbConfiguration.getDatabaseName());
        db = mongo.getDB(dbConfiguration.getDatabaseName());
        assertNotNull(db);
    }
    
    @Test
    public void testStoreAFewRCEventEntries() throws UnknownHostException, MongoException, InterruptedException {
        DomainFactory domainFactory = DomainFactory.INSTANCE;
        Regatta domainEvent = domainFactory.getOrCreateDefaultRegatta(getTracTracEvent(), /* trackedRegattaRegistry */ null);
        DynamicTrackedRegatta trackedRegatta = new RacingEventServiceImpl().getOrCreateTrackedRegatta(domainEvent);
        Iterable<Receiver> typeControllers = domainFactory.getUpdateReceivers(trackedRegatta, getTracTracEvent(),
                EmptyWindStore.INSTANCE, EmptyRaceCommitteeStore.INSTANCE,
                /* startOfTracking */null, /* endOfTracking */null, /* delayToLiveInMillis */
                0l, /* simulator */ null, new DynamicRaceDefinitionSet() {
                    @Override
                    public void addRaceDefinition(RaceDefinition race) {
                    }
                }, /* trackedRegattaRegistry */ null, ReceiverType.RACECOURSE);
        addListenersForStoredDataAndStartController(typeControllers);
        RaceDefinition race = domainFactory.getAndWaitForRaceDefinition(getTracTracEvent().getRaceList().iterator().next());
        DynamicTrackedRace trackedRace = trackedRegatta.createTrackedRace(race, EmptyWindStore.INSTANCE, 
                    /* delayToLiveInMillis */ 0l, /* millisecondsOverWhichToAverageWind */ 30000, /* millisecondsOverWhichToAverageSpeed */ 10000, new DynamicRaceDefinitionSet() {
                    @Override
                    public void addRaceDefinition(RaceDefinition race) {
                    }
                }, EmptyRaceCommitteeStore.INSTANCE);
        
        Mongo myFirstMongo = newMongo();
        DB firstDatabase = myFirstMongo.getDB(dbConfiguration.getDatabaseName());
        new MongoObjectFactoryImpl(firstDatabase).addRaceCommitteeEventTrackDumper(trackedRegatta, trackedRace);
        RaceCommitteeEventTrack raceCommitteeEventTrack = trackedRace.getOrCreateRaceCommitteeEventTrack();
        
        TimePoint timePoint = MillisecondsTimePoint.now();
        TimePoint timePointStartTime = new MillisecondsTimePoint(timePoint.asMillis() + 60000);
        TimePoint initialTimePointStartTime = timePointStartTime;
        for (int passId = 0; passId < 100; passId++) {
        	raceCommitteeEventTrack.add(RaceCommitteeEventFactory.INSTANCE.createStartTimeEvent(timePoint, passId, timePointStartTime));
            timePoint = new MillisecondsTimePoint(timePoint.asMillis() + 100);
            timePointStartTime = new MillisecondsTimePoint(timePointStartTime.asMillis() + 100);
        }
        
        Thread.sleep(2000); // give MongoDB some time to make written data available to other connections
        
        Mongo mySecondMongo = newMongo();
        DB secondDatabase = mySecondMongo.getDB(dbConfiguration.getDatabaseName());
        RaceCommitteeEventTrack result = new DomainObjectFactoryImpl(secondDatabase).loadRaceCommitteeEventTrack(domainEvent, race);
        int resultingPassId = 0;
        timePointStartTime = initialTimePointStartTime;
        
        result.lockForRead();
        try {
            for (RaceCommitteeEvent event : result.getRawFixes()) {
            	RaceCommitteeStartTimeEvent startTimeEvent = (RaceCommitteeStartTimeEvent) event;
            	
            	assertEquals(resultingPassId, startTimeEvent.getPassId());
            	assertEquals(timePointStartTime, startTimeEvent.getStartTime());
                
            	resultingPassId++;
            	timePointStartTime = new MillisecondsTimePoint(timePointStartTime.asMillis() + 100);
            }
        } finally {
            result.unlockAfterRead();
        }
    }
}
