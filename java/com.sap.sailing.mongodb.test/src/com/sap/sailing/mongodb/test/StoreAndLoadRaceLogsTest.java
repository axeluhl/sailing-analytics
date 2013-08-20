package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.junit.Test;

import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.persistence.MongoFactory;
import com.sap.sailing.domain.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogIdentifierTemplate;
import com.sap.sailing.domain.racelog.impl.RaceLogIdentifierImpl;

public class StoreAndLoadRaceLogsTest extends AbstractMongoDBTest {

    protected MongoObjectFactoryImpl mongoFactory = (MongoObjectFactoryImpl) MongoFactory.INSTANCE
            .getMongoObjectFactory(getMongoService());
    protected DomainObjectFactoryImpl domainFactory = (DomainObjectFactoryImpl) MongoFactory.INSTANCE
            .getDomainObjectFactory(getMongoService());
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
    public void testdLoadObsoleteAndNewIdentifier() {
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

    private String createOldFormatRaceLogEventJson(Serializable id) {
        return "{ \"_id\" : { \"$oid\" : \""+ new ObjectId().toStringMongod() + "\"} , \"RACE_LOG_IDENTIFIER\" : \"A%2EA%2EB%2EB%2EC%2EC\" , \"RACE_LOG_EVENT\" : "
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
        return eventFactory.createFlagEvent(new MillisecondsTimePoint(42), id, Collections.<Competitor> emptyList(),
                42, Flags.ALPHA, Flags.BRAVO, true);
    }

}
