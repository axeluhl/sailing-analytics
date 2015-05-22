package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventRestoreFactory;
import com.sap.sailing.domain.abstractlog.race.RaceLogFlagEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPassChangeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDefineMarkEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDenoteForTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogRegisterCompetitorEvent;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.FieldNames;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.tracking.test.mock.MockSmartphoneImeiServiceFinderFactory;
import com.sap.sailing.domain.racelog.tracking.test.mock.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class StoreAndLoadRaceLogEventsTest extends AbstractMongoDBTest {

    protected MongoObjectFactoryImpl mongoFactory = (MongoObjectFactoryImpl) PersistenceFactory.INSTANCE
            .getMongoObjectFactory(getMongoService(), new MockSmartphoneImeiServiceFinderFactory());
    protected DomainObjectFactoryImpl domainFactory = (DomainObjectFactoryImpl) PersistenceFactory.INSTANCE
            .getDomainObjectFactory(getMongoService(), DomainFactory.INSTANCE,
            		new MockSmartphoneImeiServiceFinderFactory());
    protected RaceLogEventRestoreFactory eventFactory = RaceLogEventRestoreFactory.INSTANCE;

    protected RaceLogIdentifier logIdentifier;

    protected TimePoint expectedEventTime = new MillisecondsTimePoint(42);
    protected Serializable expectedId = UUID.randomUUID();
    protected List<Competitor> expectedInvolvedBoats = Collections.emptyList();
    protected int expectedPassId = 42;
    private AbstractLogEventAuthor author = new LogEventAuthorImpl("Test Author", 1);

    public StoreAndLoadRaceLogEventsTest() throws UnknownHostException, MongoException {
        super();
    }

    @Before
    public void setUp() {
        logIdentifier = mock(RaceLogIdentifier.class);
        when(logIdentifier.getIdentifier()).thenReturn(new com.sap.sse.common.Util.Triple<String, String, String>("a", "b", UUID.randomUUID().toString()));
    }

    public void assertBaseFields(RaceLogEvent expectedEvent, RaceLogEvent actualEvent) {
        assertNotNull(actualEvent);
        assertEquals(expectedEvent.getCreatedAt(), actualEvent.getCreatedAt());
        assertEquals(expectedEvent.getLogicalTimePoint(), actualEvent.getLogicalTimePoint());
        assertEquals(expectedEvent.getId(), actualEvent.getId());
        assertEquals(expectedEvent.getInvolvedBoats().size(), Util.size(actualEvent.getInvolvedBoats()));
        assertEquals(expectedEvent.getPassId(), actualEvent.getPassId());
    }
    
    @Test 
    public void testStoreEventWithoutAuthorLoadsCompatibilityAuthor() {
        RaceLogFlagEvent expectedEvent = eventFactory.createFlagEvent(expectedEventTime, 
                null, expectedId, expectedInvolvedBoats, expectedPassId, Flags.NONE, Flags.NONE, true);

        DBObject dbObject = mongoFactory.storeRaceLogEntry(logIdentifier, expectedEvent);
        RaceLogFlagEvent actualEvent = loadEvent(dbObject);
        
        assertNull(expectedEvent.getAuthor());
        assertNotNull(actualEvent.getAuthor());
        assertEquals(AbstractLogEventAuthor.PRIORITY_COMPATIBILITY, actualEvent.getAuthor().getPriority());
        assertEquals(AbstractLogEventAuthor.NAME_COMPATIBILITY, actualEvent.getAuthor().getName());
    }

    @Test
    public void testStoreAndLoadFlagEvent() {
        Flags upperFlag = Flags.ALPHA;
        Flags lowerFlag = Flags.BRAVO;
        boolean isDisplayed = true;

        RaceLogFlagEvent expectedEvent = eventFactory.createFlagEvent(expectedEventTime, 
                author, expectedId, expectedInvolvedBoats, expectedPassId, upperFlag, lowerFlag, isDisplayed);

        DBObject dbObject = mongoFactory.storeRaceLogEntry(logIdentifier, expectedEvent);
        RaceLogFlagEvent actualEvent = loadEvent(dbObject);

        assertBaseFields(expectedEvent, actualEvent);
        assertEquals(upperFlag, actualEvent.getUpperFlag());
        assertEquals(lowerFlag, actualEvent.getLowerFlag());
        assertEquals(isDisplayed, actualEvent.isDisplayed());
    }

    @Test
    public void testStoreAndLoadCourseAreaChangedEvent() {
        Serializable courseAreaId = UUID.randomUUID();

        RaceLogCourseAreaChangedEvent expectedEvent = eventFactory.createCourseAreaChangedEvent(
                expectedEventTime, author, expectedId, expectedInvolvedBoats, expectedPassId, courseAreaId);

        DBObject dbObject = mongoFactory.storeRaceLogEntry(logIdentifier, expectedEvent);
        RaceLogCourseAreaChangedEvent actualEvent = loadEvent(dbObject);

        assertBaseFields(expectedEvent, actualEvent);
        assertEquals(courseAreaId, actualEvent.getCourseAreaId());
    }

    @Test
    public void testStoreAndLoadPassChangeEvent() {
        RaceLogPassChangeEvent expectedEvent = eventFactory.createPassChangeEvent(expectedEventTime,
                author, expectedId, expectedInvolvedBoats, expectedPassId);

        DBObject dbObject = mongoFactory.storeRaceLogEntry(logIdentifier, expectedEvent);
        RaceLogPassChangeEvent actualEvent = loadEvent(dbObject);

        assertBaseFields(expectedEvent, actualEvent);
    }

    @Test
    public void testStoreAndLoadRaceStatusEvent() {
        RaceLogRaceStatus status = RaceLogRaceStatus.SCHEDULED;
        RaceLogRaceStatusEvent expectedEvent = eventFactory.createRaceStatusEvent(expectedEventTime,
                author, expectedId, expectedInvolvedBoats, expectedPassId, status);

        DBObject dbObject = mongoFactory.storeRaceLogEntry(logIdentifier, expectedEvent);
        RaceLogRaceStatusEvent actualEvent = loadEvent(dbObject);

        assertBaseFields(expectedEvent, actualEvent);
        assertEquals(status, actualEvent.getNextStatus());
    }

    @Test
    public void testStoreAndLoadStartTimeEvent() {
        TimePoint startTime = new MillisecondsTimePoint(1337);
        RaceLogStartTimeEvent expectedEvent = eventFactory.createStartTimeEvent(expectedEventTime,
                author, expectedId, expectedInvolvedBoats, expectedPassId, startTime);

        DBObject dbObject = mongoFactory.storeRaceLogEntry(logIdentifier, expectedEvent);
        RaceLogStartTimeEvent actualEvent = loadEvent(dbObject);

        assertBaseFields(expectedEvent, actualEvent);
        assertEquals(startTime, actualEvent.getStartTime());
    }

    @Test
    public void testStoreAndLoadDeviceCompetitorMappingEvent() {
        DeviceIdentifier device = new SmartphoneImeiIdentifier("a");
        Competitor mappedTo = DomainFactory.INSTANCE.getOrCreateCompetitor("abc", "abc", null, null, null, null, null);
        TimePoint from = new MillisecondsTimePoint(20);
        TimePoint to = new MillisecondsTimePoint(30);
        RaceLogDeviceCompetitorMappingEvent expectedEvent = eventFactory.createDeviceCompetitorMappingEvent(
        		expectedEventTime, author, expectedEventTime, expectedId, device, mappedTo, expectedPassId, from, to);

        DBObject dbObject = mongoFactory.storeRaceLogEntry(logIdentifier, expectedEvent);
        RaceLogDeviceCompetitorMappingEvent actualEvent = loadEvent(dbObject);

        assertBaseFields(expectedEvent, actualEvent);
        assertEquals(device, actualEvent.getDevice());
        assertEquals(from, actualEvent.getFrom());
        assertEquals(to, actualEvent.getTo());
        assertEquals(mappedTo, actualEvent.getMappedTo());
    }

    @Test
    public void testStoreAndLoadDeviceMarkMappingEvent() {
        DeviceIdentifier device = new SmartphoneImeiIdentifier("a");
        Mark mappedTo = DomainFactory.INSTANCE.getOrCreateMark("abc", "abc");
        TimePoint from = new MillisecondsTimePoint(20);
        TimePoint to = new MillisecondsTimePoint(30);
        RaceLogDeviceMarkMappingEvent expectedEvent = eventFactory.createDeviceMarkMappingEvent(
        		expectedEventTime, author, expectedEventTime, expectedId, device, mappedTo, expectedPassId, from, to);

        DBObject dbObject = mongoFactory.storeRaceLogEntry(logIdentifier, expectedEvent);
        RaceLogDeviceMarkMappingEvent actualEvent = loadEvent(dbObject);

        assertBaseFields(expectedEvent, actualEvent);
        assertEquals(device, actualEvent.getDevice());
        assertEquals(from, actualEvent.getFrom());
        assertEquals(to, actualEvent.getTo());
        assertEquals(mappedTo, actualEvent.getMappedTo());
    }

    @Test
    public void testStoreAndLoadDenoteForTrackingEvent() {
    	String raceName = "race";
    	BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("49er");
    	UUID raceId = UUID.randomUUID();
        RaceLogDenoteForTrackingEvent expectedEvent = eventFactory.createDenoteForTrackingEvent(
        		expectedEventTime, author, expectedEventTime, expectedId, expectedPassId, raceName, boatClass,
        		raceId);

        DBObject dbObject = mongoFactory.storeRaceLogEntry(logIdentifier, expectedEvent);
        RaceLogDenoteForTrackingEvent actualEvent = loadEvent(dbObject);

        assertBaseFields(expectedEvent, actualEvent);
        assertEquals(boatClass, actualEvent.getBoatClass());
        assertEquals(raceName, actualEvent.getRaceName());
        assertEquals(raceId, actualEvent.getRaceId());
    }

    @Test
    public void testStoreAndLoadRevokeEvent() {
        UUID revokedEventId = UUID.randomUUID();
        String revokedEventType = "type";
        String revokedEventShortInfo = "short info";
        String reason = "reason";
        RaceLogRevokeEvent expectedEvent = eventFactory.createRevokeEvent(
        		expectedEventTime, author, expectedEventTime, expectedId, expectedPassId, revokedEventId,
        		revokedEventType, revokedEventShortInfo, reason);

        DBObject dbObject = mongoFactory.storeRaceLogEntry(logIdentifier, expectedEvent);
        RaceLogRevokeEvent actualEvent = loadEvent(dbObject);

        assertBaseFields(expectedEvent, actualEvent);
        assertEquals(revokedEventId, actualEvent.getRevokedEventId());
        assertEquals(revokedEventType, actualEvent.getRevokedEventType());
        assertEquals(revokedEventShortInfo, actualEvent.getRevokedEventShortInfo());
        assertEquals(reason, actualEvent.getReason());
    }

    @Test
    public void testStoreAndLoadRegisterCompetitorEvent() {
        RaceLogRegisterCompetitorEvent expectedEvent = eventFactory.createRegisterCompetitorEvent(
        		expectedEventTime, author, expectedEventTime, expectedId, expectedPassId,
        		DomainFactory.INSTANCE.getOrCreateCompetitor("comp", "comp", null, null, null, null, null));

        DBObject dbObject = mongoFactory.storeRaceLogEntry(logIdentifier, expectedEvent);
        RaceLogRegisterCompetitorEvent actualEvent = loadEvent(dbObject);

        assertBaseFields(expectedEvent, actualEvent);
        assertEquals(expectedEvent.getCompetitor(), actualEvent.getCompetitor());
    }

    @Test
    public void testStoreAndLoadDefineMarkEvent() {
        RaceLogDefineMarkEvent expectedEvent = eventFactory.createDefineMarkEvent(
                        expectedEventTime, author, expectedEventTime, expectedId, expectedPassId,
                        DomainFactory.INSTANCE.getOrCreateMark("mytestmark"));

        DBObject dbObject = mongoFactory.storeRaceLogEntry(logIdentifier, expectedEvent);
        RaceLogDefineMarkEvent actualEvent = loadEvent(dbObject);

        assertBaseFields(expectedEvent, actualEvent);
        assertEquals(expectedEvent.getMark(), actualEvent.getMark());
    }

    @Test
    public void testStoreAndLoadCloseOpenDeviceMappingEvent() {
        RaceLogCloseOpenEndedDeviceMappingEvent expectedEvent = eventFactory.createCloseOpenEndedDeviceMappingEvent(
                        expectedEventTime, author, expectedEventTime, expectedId, expectedPassId,
                        UUID.randomUUID(), new MillisecondsTimePoint(49));

        DBObject dbObject = mongoFactory.storeRaceLogEntry(logIdentifier, expectedEvent);
        RaceLogCloseOpenEndedDeviceMappingEvent actualEvent = loadEvent(dbObject);

        assertBaseFields(expectedEvent, actualEvent);
        assertEquals(expectedEvent.getDeviceMappingEventId(), actualEvent.getDeviceMappingEventId());
        assertEquals(expectedEvent.getClosingTimePoint(), actualEvent.getClosingTimePoint());
    }

    /**
     * Will always wait a couple of milliseconds to ensure that {@link RaceLogEvent#getCreatedAt()} has passed.
     */
    @SuppressWarnings("unchecked")
    private <T extends RaceLogEvent> T loadEvent(DBObject dbObject) {
        try {
            Thread.sleep(2);
        } catch (InterruptedException ie) {
            fail(ie.toString());
        }
        RaceLogEvent dbEvent = domainFactory
                .loadRaceLogEvent((DBObject) dbObject.get(FieldNames.RACE_LOG_EVENT.name()));
        T actualEvent = (T) dbEvent;
        return actualEvent;
    }
}
