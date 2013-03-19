package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.persistence.MongoFactory;
import com.sap.sailing.domain.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.FieldNames;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.racelog.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogPassChangeEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;

public class StoreRaceLogEventsTest extends AbstractMongoDBTest {
    
    protected MongoObjectFactoryImpl mongoFactory = (MongoObjectFactoryImpl) MongoFactory.INSTANCE.getMongoObjectFactory(getMongoService());
    protected DomainObjectFactoryImpl domainFactory = (DomainObjectFactoryImpl) MongoFactory.INSTANCE.getDomainObjectFactory(getMongoService());
    protected RaceLogEventFactory eventFactory = RaceLogEventFactory.INSTANCE;
    
    protected RaceLogIdentifier logIdentifier;
    
    protected TimePoint expectedEventTime = new MillisecondsTimePoint(42);
    protected Serializable expectedId = UUID.randomUUID();
    protected List<Competitor> expectedInvolvedBoats = Collections.emptyList();
    protected int expectedPassId = 42;
    
    public StoreRaceLogEventsTest() throws UnknownHostException, MongoException {
        super();
    }
    
    @Before
    public void setUp() {
        logIdentifier = mock(RaceLogIdentifier.class);
        when(logIdentifier.getIdentifier()).thenReturn(UUID.randomUUID());
    }
    
    public void assertBaseFields(RaceLogEvent event) {
        assertNotNull(event);
        assertEquals(expectedEventTime, event.getTimePoint());
        assertEquals(expectedId, event.getId());
        assertEquals(expectedInvolvedBoats.size(), Util.size(event.getInvolvedBoats()));
        assertEquals(expectedPassId, event.getPassId());
    }
    
    @Test
    public void testStoreAndLoadFlagEvent() {
        Flags upperFlag = Flags.ALPHA;
        Flags lowerFlag = Flags.BRAVO;
        boolean isDisplayed = true;
        
        RaceLogFlagEvent expectedEvent = eventFactory.createFlagEvent(expectedEventTime, expectedId, expectedInvolvedBoats, expectedPassId, 
                upperFlag, lowerFlag, isDisplayed);
        
        DBObject dbObject = mongoFactory.storeRaceLogEntry(logIdentifier, expectedEvent);
        RaceLogFlagEvent actualEvent = loadEvent(dbObject);
        
        assertBaseFields(actualEvent);
        assertEquals(upperFlag, actualEvent.getUpperFlag());
        assertEquals(lowerFlag, actualEvent.getLowerFlag());
        assertEquals(isDisplayed, actualEvent.isDisplayed());
    }
    
    @Test
    public void testStoreAndLoadCourseAreaChangedEvent() {
        Serializable courseAreaId = UUID.randomUUID();
        
        RaceLogCourseAreaChangedEvent expectedEvent = eventFactory.createRaceLogCourseAreaChangedEvent(expectedEventTime, expectedId, expectedInvolvedBoats, expectedPassId, 
                courseAreaId);
        
        DBObject dbObject = mongoFactory.storeRaceLogEntry(logIdentifier, expectedEvent);
        RaceLogCourseAreaChangedEvent actualEvent = loadEvent(dbObject);
        
        assertBaseFields(actualEvent);
        assertEquals(courseAreaId, actualEvent.getCourseAreaId());
    }
    
    @Test
    public void testStoreAndLoadPassChangeEvent() {
        RaceLogPassChangeEvent expectedEvent = eventFactory.createRaceLogPassChangeEvent(expectedEventTime, expectedId, expectedInvolvedBoats, expectedPassId);
        
        DBObject dbObject = mongoFactory.storeRaceLogEntry(logIdentifier, expectedEvent);
        RaceLogPassChangeEvent actualEvent = loadEvent(dbObject);
        
        assertBaseFields(actualEvent);
    }
    
    @Test
    public void testStoreAndLoadRaceStatusEvent() {
        RaceLogRaceStatus status = RaceLogRaceStatus.SCHEDULED;
        RaceLogRaceStatusEvent expectedEvent = eventFactory.createRaceStatusEvent(expectedEventTime, expectedId, expectedInvolvedBoats, expectedPassId,
                status);
        
        DBObject dbObject = mongoFactory.storeRaceLogEntry(logIdentifier, expectedEvent);
        RaceLogRaceStatusEvent actualEvent = loadEvent(dbObject);
        
        assertBaseFields(actualEvent);
        assertEquals(status, actualEvent.getNextStatus());
    }
    
    @Test
    public void testStoreAndLoadStartTimeEvent() {
        TimePoint startTime = new MillisecondsTimePoint(1337);
        RaceLogStartTimeEvent expectedEvent = eventFactory.createStartTimeEvent(expectedEventTime, expectedId, expectedInvolvedBoats, expectedPassId,
                startTime);
        
        DBObject dbObject = mongoFactory.storeRaceLogEntry(logIdentifier, expectedEvent);
        RaceLogStartTimeEvent actualEvent = loadEvent(dbObject);
        
        assertBaseFields(actualEvent);
        assertEquals(startTime, actualEvent.getStartTime());
    }

    @SuppressWarnings("unchecked")
    private <T extends RaceLogEvent> T loadEvent(DBObject dbObject) {
        RaceLogEvent dbEvent = domainFactory.loadRaceLogEvent((DBObject) dbObject.get(FieldNames.RACE_LOG_EVENT.name()));
        T actualEvent = (T) dbEvent;
        return actualEvent;
    }
}
