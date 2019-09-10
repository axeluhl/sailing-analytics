package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bson.Document;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoException;
import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.orc.RaceLogORCCertificateAssignmentEvent;
import com.sap.sailing.domain.abstractlog.orc.impl.RaceLogORCCertificateAssignmentEventImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.orc.ORCCertificateImporter;
import com.sap.sailing.domain.orc.impl.ORCCertificateImporterJSON;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.FieldNames;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.tracking.test.mock.MockSmartphoneImeiServiceFinderFactory;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * @author Daniel Lisunkin (i505543)
 */
public class TestStoringAndLoadingRegattaORCCertificateAssignmengtEvent extends AbstractMongoDBTest {

    public TestStoringAndLoadingRegattaORCCertificateAssignmengtEvent() throws UnknownHostException, MongoException {
        super();
    }

    protected TimePoint expectedEventTime = new MillisecondsTimePoint(750);
    protected Serializable expectedId = UUID.randomUUID();
    protected List<Competitor> expectedInvolvedBoats = Collections.emptyList();
    protected int expectedPassId = 42;
    private AbstractLogEventAuthor author = new LogEventAuthorImpl("Test Author", 1);
    private final Competitor competitor = new CompetitorImpl("comp", "Comp", "KYC", null, null, null, null, /* timeOnTimeFactor */ null, /* timeOnDistanceAllowancePerNauticalMile */ null, null);
    
    protected MongoObjectFactoryImpl mongoFactory = (MongoObjectFactoryImpl) PersistenceFactory.INSTANCE
            .getMongoObjectFactory(getMongoService(), new MockSmartphoneImeiServiceFinderFactory());
    protected DomainObjectFactoryImpl domainFactory = (DomainObjectFactoryImpl) PersistenceFactory.INSTANCE
            .getDomainObjectFactory(getMongoService(), DomainFactory.INSTANCE,
                    new MockSmartphoneImeiServiceFinderFactory());

    protected RaceLogIdentifier logIdentifier;
    private ORCCertificateImporter importer;

    @Before
    public void setUp() throws FileNotFoundException, IOException, ParseException {
        logIdentifier = mock(RaceLogIdentifier.class);
        when(logIdentifier.getIdentifier()).thenReturn(
                new com.sap.sse.common.Util.Triple<String, String, String>("a", "b", UUID.randomUUID().toString()));
        DomainFactory.INSTANCE.getCompetitorAndBoatStore().clearCompetitors();
        importer = new ORCCertificateImporterJSON(new FileInputStream(new File("resources/GER2019.json")));
    }

    @Test
    public void test() throws JsonDeserializationException, ParseException {
        RaceLogORCCertificateAssignmentEvent expectedEvent = new RaceLogORCCertificateAssignmentEventImpl(MillisecondsTimePoint.now(), expectedEventTime, author,
                expectedId, expectedPassId, importer.getCertificate("GER 5549"), competitor);

        Document dbObject = mongoFactory.storeRaceLogEntry(logIdentifier, expectedEvent);
        RaceLogORCCertificateAssignmentEvent actualEvent = loadEvent(dbObject);

        assertBaseFields(expectedEvent, actualEvent);
        assertEquals(expectedEvent.getCompetitorID(), actualEvent.getCompetitorID());
        assertEquals(expectedEvent.getCertificate(), actualEvent.getCertificate());
    }

    public void assertBaseFields(RaceLogEvent expectedEvent, RaceLogEvent actualEvent) {
        assertNotNull(actualEvent);
        assertEquals(expectedEvent.getCreatedAt(), actualEvent.getCreatedAt());
        assertEquals(expectedEvent.getLogicalTimePoint(), actualEvent.getLogicalTimePoint());
        assertEquals(expectedEvent.getId(), actualEvent.getId());
        assertEquals(expectedEvent.getInvolvedCompetitors().size(), Util.size(actualEvent.getInvolvedCompetitors()));
        assertEquals(expectedEvent.getPassId(), actualEvent.getPassId());
    }

    /**
     * Will always wait a couple of milliseconds to ensure that {@link RaceLogEvent#getCreatedAt()} has passed.
     * @throws ParseException 
     * @throws JsonDeserializationException 
     */
    @SuppressWarnings("unchecked")
    private <T extends RaceLogEvent> T loadEvent(Document dbObject) throws JsonDeserializationException, ParseException {
        try {
            Thread.sleep(2);
        } catch (InterruptedException ie) {
            fail(ie.toString());
        }
        RaceLogEvent dbEvent = domainFactory.loadRaceLogEvent((Document) dbObject.get(FieldNames.RACE_LOG_EVENT.name())).getA();
        T actualEvent = (T) dbEvent;
        return actualEvent;
    }

}
