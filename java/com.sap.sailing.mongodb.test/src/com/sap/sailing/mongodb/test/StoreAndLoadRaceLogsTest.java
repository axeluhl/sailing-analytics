package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventFactory;
import com.sap.sailing.domain.abstractlog.race.RaceLogFlagEvent;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventAuthorImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.FieldNames;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.TripleSerializer;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogIdentifierTemplate;
import com.sap.sailing.domain.racelog.impl.RaceLogIdentifierImpl;
import com.sap.sse.common.Util;

public class StoreAndLoadRaceLogsTest extends AbstractMongoDBTest {

    protected MongoObjectFactoryImpl mongoFactory = (MongoObjectFactoryImpl) PersistenceFactory.INSTANCE
            .getMongoObjectFactory(getMongoService());
    protected DomainObjectFactoryImpl domainFactory = (DomainObjectFactoryImpl) PersistenceFactory.INSTANCE
            .getDomainObjectFactory(getMongoService(), DomainFactory.INSTANCE);
    protected RaceLogEventFactory eventFactory = RaceLogEventFactory.INSTANCE;

    public StoreAndLoadRaceLogsTest() throws UnknownHostException, MongoException {
        super();
    }

    @Test
    public void testStoreAndLoadSingleRaceLog() {
        RaceLogIdentifier identifier = createIdentifier("A.A", "B.B", "C.C");

        RaceLogFlagEvent event1 = createRaceLogFlagEvent(UUID.randomUUID());
        RaceLogFlagEvent event2 = createRaceLogFlagEvent(UUID.randomUUID());
        DBObject dbEvent1 = mongoFactory.storeRaceLogEntry(identifier, event1);
        mongoFactory.getRaceLogCollection().insert(dbEvent1);
        DBObject dbEvent2 = mongoFactory.storeRaceLogEntry(identifier, event2);
        mongoFactory.getRaceLogCollection().insert(dbEvent2);

        RaceLog raceLog = domainFactory.loadRaceLog(identifier);
        try {
            raceLog.lockForRead();
            assertEquals(2, Util.size(raceLog.getRawFixes()));
        } finally {
            raceLog.unlockAfterRead();
        }
    }

    @Test
    public void testStoreAndLoadCollidingRaceLogs() {
        // RaceLog A.A B.B C.C
        RaceLogIdentifier identifier = createIdentifier("A.A", "B.B", "C.C");

        RaceLogFlagEvent event1 = createRaceLogFlagEvent(UUID.randomUUID());
        RaceLogFlagEvent event2 = createRaceLogFlagEvent(UUID.randomUUID());
        DBObject dbEvent1 = mongoFactory.storeRaceLogEntry(identifier, event1);
        mongoFactory.getRaceLogCollection().insert(dbEvent1);
        DBObject dbEvent2 = mongoFactory.storeRaceLogEntry(identifier, event2);
        mongoFactory.getRaceLogCollection().insert(dbEvent2);

        // RaceLog A.A.B B.C C
        RaceLogIdentifier collidingIdentifier = createIdentifier("A.A.B", "B.C", "C");

        RaceLogFlagEvent event3 = createRaceLogFlagEvent(UUID.randomUUID());
        DBObject dbEvent3 = mongoFactory.storeRaceLogEntry(collidingIdentifier, event3);
        mongoFactory.getRaceLogCollection().insert(dbEvent3);

        RaceLog raceLog = domainFactory.loadRaceLog(identifier);
        try {
            raceLog.lockForRead();
            assertEquals(2, Util.size(raceLog.getRawFixes()));
        } finally {
            raceLog.unlockAfterRead();
        }
    }

    @Test
    public void testdLoadObsoleteIdentifier() {
        // Identifier is A.A B.B C.C

        DBObject dbEvent1 = (DBObject) com.mongodb.util.JSON.parse(createOldFormatRaceLogEventJson(UUID.randomUUID()));
        mongoFactory.getRaceLogCollection().insert(dbEvent1);
        DBObject dbEvent2 = (DBObject) com.mongodb.util.JSON.parse(createOldFormatRaceLogEventJson(UUID.randomUUID()));
        mongoFactory.getRaceLogCollection().insert(dbEvent2);

        RaceLog raceLog = domainFactory.loadRaceLog(createIdentifier("A.A", "B.B", "C.C"));
        try {
            raceLog.lockForRead();
            assertEquals(2, Util.size(raceLog.getRawFixes()));
        } finally {
            raceLog.unlockAfterRead();
        }
    }

    @Test
    public void testLoadObsoleteAndNewIdentifier() {
        // event1 stored with obsolete identifier
        DBObject dbEvent1 = (DBObject) com.mongodb.util.JSON.parse(createOldFormatRaceLogEventJson(UUID.randomUUID()));
        mongoFactory.getRaceLogCollection().insert(dbEvent1);

        // event2 stored with new identifier
        RaceLogIdentifier identifier = createIdentifier("A.A", "B.B", "C.C");
        RaceLogFlagEvent event2 = createRaceLogFlagEvent(UUID.randomUUID());
        DBObject dbEvent2 = mongoFactory.storeRaceLogEntry(identifier, event2);
        mongoFactory.getRaceLogCollection().insert(dbEvent2);

        RaceLog raceLog = domainFactory.loadRaceLog(identifier);
        try {
            raceLog.lockForRead();
            assertEquals(2, Util.size(raceLog.getRawFixes()));
        } finally {
            raceLog.unlockAfterRead();
        }
    }
    
    @Test
    public void testdMigrateObsoleteIdentifier() {
        DBCollection collection = mongoFactory.getRaceLogCollection();
        RaceLogIdentifier identifier = createIdentifier("A.A", "B.B", "C.C");
        
        // Insert new style event (which should be untouched by migration)...
        RaceLogFlagEvent newStyleEvent = createRaceLogFlagEvent(UUID.randomUUID());
        DBObject newStyleEventObject = mongoFactory.storeRaceLogEntry(identifier, newStyleEvent);
        mongoFactory.getRaceLogCollection().insert(newStyleEventObject);
        
        // ... insert old style event...
        DBObject dbEvent1 = (DBObject) com.mongodb.util.JSON.parse(createOldFormatRaceLogEventJson(UUID.randomUUID()));
        collection.insert(dbEvent1);
        
        // ... ensure that it is in the database...
        BasicDBObject query = new BasicDBObject();
        query.put(FieldNames.RACE_LOG_IDENTIFIER.name(), OldFormatRaceLogIdentifier);
        assertTrue(collection.find(query).hasNext());

        // ... loading it should trigger the migration...
        domainFactory.loadRaceLog(identifier);
        
        // ... check that old event is gone...
        assertFalse("Event with old identifier should be gone after migration", collection.find(query).hasNext());
        
        // ... and new event is stored...
        query.put(FieldNames.RACE_LOG_IDENTIFIER.name(), TripleSerializer.serialize(identifier.getIdentifier()));
        assertTrue("After migration the event should be found with new identifier", collection.find(query).hasNext());
        
        // ... finally loading this racelog should yield both our events
        RaceLog raceLog = domainFactory.loadRaceLog(identifier);
        try {
            raceLog.lockForRead();
            assertEquals(2, Util.size(raceLog.getRawFixes()));
        } finally {
            raceLog.unlockAfterRead();
        }
    }
    
    private static final String OldFormatRaceLogIdentifier = "A%2EA%2EB%2EB%2EC%2EC";

    private String createOldFormatRaceLogEventJson(Serializable id) {
        return "{ \"_id\" : { \"$oid\" : \""+ new ObjectId().toStringMongod() + "\"} , \"RACE_LOG_IDENTIFIER\" : \"" + OldFormatRaceLogIdentifier + "\" , \"RACE_LOG_EVENT\" : "
                + "{ \"TIME_AS_MILLIS\" : 42 , \"RACE_LOG_EVENT_CREATED_AT\" : 1376058090051 , \"RACE_LOG_EVENT_ID\" : { \"$uuid\" : \"" + id.toString() + "\"} , "
                + "\"RACE_LOG_EVENT_PASS_ID\" : 42 , \"RACE_LOG_EVENT_INVOLVED_BOATS\" : [ ] , \"RACE_LOG_EVENT_CLASS\" : \"RaceLogFlagEvent\" , \"RACE_LOG_EVENT_FLAG_UPPER\" : \"ALPHA\" , "
                + "\"RACE_LOG_EVENT_FLAG_LOWER\" : \"BRAVO\" , \"RACE_LOG_EVENT_FLAG_DISPLAYED\" : \"true\"}}";
    }

    private RaceLogIdentifier createIdentifier(String groupName, String raceColumnName, String fleetName) {
        RaceLogIdentifierTemplate template = mock(RaceLogIdentifierTemplate.class);
        when(template.getParentObjectName()).thenReturn(groupName);
        when(template.getRaceColumnName()).thenReturn(raceColumnName);
        Fleet fleet = mock(Fleet.class);
        when(fleet.getName()).thenReturn(fleetName);
        return new RaceLogIdentifierImpl(template, raceColumnName, fleet);
    }

    private RaceLogFlagEvent createRaceLogFlagEvent(Serializable id) {
        return eventFactory.createFlagEvent(new MillisecondsTimePoint(42), new RaceLogEventAuthorImpl("Test author", /* priority */ 1), id, Collections.<Competitor> emptyList(),
                42, Flags.ALPHA, Flags.BRAVO, true);
    }

}
