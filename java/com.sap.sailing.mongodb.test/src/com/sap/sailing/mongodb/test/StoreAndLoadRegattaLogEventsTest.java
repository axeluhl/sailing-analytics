package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.UUID;

import org.bson.Document;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoException;
import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterBoatEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMileEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogSetCompetitorTimeOnTimeFactorEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogRegisterBoatEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMileEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogSetCompetitorTimeOnTimeFactorEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.racelog.tracking.test.mock.MockSmartphoneImeiServiceFinderFactory;
import com.sap.sailing.domain.regattalike.RegattaLikeIdentifier;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sse.common.Color;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class StoreAndLoadRegattaLogEventsTest extends AbstractMongoDBTest {

    protected final MongoObjectFactoryImpl mongoFactory = (MongoObjectFactoryImpl) PersistenceFactory.INSTANCE
            .getMongoObjectFactory(getMongoService(), new MockSmartphoneImeiServiceFinderFactory());
    protected final DomainObjectFactoryImpl domainFactory = (DomainObjectFactoryImpl) PersistenceFactory.INSTANCE
            .getDomainObjectFactory(getMongoService(), DomainFactory.INSTANCE,
                    new MockSmartphoneImeiServiceFinderFactory());

    protected final TimePoint expectedEventTime = new MillisecondsTimePoint(42);
    protected final Serializable expectedId = UUID.randomUUID();
    protected final Serializable competitorId = UUID.randomUUID();
    protected final AbstractLogEventAuthor author = new LogEventAuthorImpl("Test Author", 1);

    protected RegattaLikeIdentifier regattaIdentifier;

    public StoreAndLoadRegattaLogEventsTest() throws UnknownHostException, MongoException {
        super();
    }

    @Before
    public void setUp() {
        regattaIdentifier = mock(RegattaLikeIdentifier.class);
        when(regattaIdentifier.getName()).thenReturn("testRegatta");
    }
    
    public Competitor createCompetitor() {
        return new CompetitorImpl(competitorId, "CompetitorName", "KYC", Color.RED, null, null, null,
                        /* timeOnTimeFactor */ null, /* timeOnDistanceAllowancePerNauticalMile */ null, null);
    }

    public void assertBaseFields(RegattaLogEvent expectedEvent, RegattaLogEvent actualEvent) {
        assertNotNull(actualEvent);
        assertEquals(expectedEvent.getCreatedAt(), actualEvent.getCreatedAt());
        assertEquals(expectedEvent.getTimePoint(), actualEvent.getTimePoint());
        assertEquals(expectedEvent.getLogicalTimePoint(), actualEvent.getLogicalTimePoint());
        assertEquals(expectedEvent.getId(), actualEvent.getId());
        assertEquals(expectedEvent.getAuthor(), actualEvent.getAuthor());
        assertEquals(expectedEvent.getShortInfo(), actualEvent.getShortInfo());
    }

    @Test
    public void testStoreEventWithoutAuthorLoadsCompatibilityAuthor() {
        RegattaLogRegisterBoatEvent expectedEvent = new RegattaLogRegisterBoatEventImpl(
                MillisecondsTimePoint.now(), expectedEventTime, null, expectedId, null);
        Document dbObject = mongoFactory.storeRegattaLogEvent(regattaIdentifier, expectedEvent);
        final RegattaLogEvent actualEvent = loadEvent(dbObject);
        assertNull(expectedEvent.getAuthor());
        assertNotNull(actualEvent.getAuthor());
        assertEquals(AbstractLogEventAuthor.PRIORITY_COMPATIBILITY, actualEvent.getAuthor().getPriority());
        assertEquals(AbstractLogEventAuthor.NAME_COMPATIBILITY, actualEvent.getAuthor().getName());
    }

    @Test
    public void testStoreAndLoadSetCompetitorTimeOnTimeFactorEvent() {
        final double timeOnTimeFactor = 1.5;
        final RegattaLogSetCompetitorTimeOnTimeFactorEvent expectedEvent = new RegattaLogSetCompetitorTimeOnTimeFactorEventImpl(MillisecondsTimePoint.now(), expectedEventTime,
                author, expectedId, createCompetitor(), timeOnTimeFactor);
        final Document dbObject = mongoFactory.storeRegattaLogEvent(regattaIdentifier, expectedEvent);
        final RegattaLogSetCompetitorTimeOnTimeFactorEvent actualEvent = loadEvent(dbObject);
        assertBaseFields(expectedEvent, actualEvent);
        assertEquals(expectedEvent.getCompetitor().getId(), actualEvent.getCompetitor().getId());
        assertEquals(expectedEvent.getTimeOnTimeFactor(), actualEvent.getTimeOnTimeFactor());
    }

    @Test
    public void testStoreAndLoadPassChangeEvent() {
        final Duration duration= new MillisecondsDurationImpl(10);
        RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMileEvent expectedEvent = new RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMileEventImpl(MillisecondsTimePoint.now(),
                expectedEventTime, author, expectedId, createCompetitor(), duration);
        Document dbObject = mongoFactory.storeRegattaLogEvent(regattaIdentifier, expectedEvent);
        RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMileEvent actualEvent = loadEvent(dbObject);
        assertBaseFields(expectedEvent, actualEvent);
        assertEquals(expectedEvent.getCompetitor().getId(), actualEvent.getCompetitor().getId());
        assertEquals(expectedEvent.getTimeOnDistanceAllowancePerNauticalMile(), actualEvent.getTimeOnDistanceAllowancePerNauticalMile());
    }

    /**
     * Will always wait a couple of milliseconds to ensure that {@link RegattaLogEvent#getCreatedAt()} has passed.
     */
    @SuppressWarnings("unchecked")
    private <T extends RegattaLogEvent> T loadEvent(Document dbObject) {
        try {
            Thread.sleep(2);
        } catch (InterruptedException ie) {
            fail(ie.toString());
        }
        RegattaLogEvent dbEvent;
        try {
            dbEvent = domainFactory.loadRegattaLogEvent(dbObject, regattaIdentifier);
        } catch (JsonDeserializationException | ParseException e) {
            throw new RuntimeException(e);
        }
        T actualEvent = (T) dbEvent;
        return actualEvent;
    }
}
