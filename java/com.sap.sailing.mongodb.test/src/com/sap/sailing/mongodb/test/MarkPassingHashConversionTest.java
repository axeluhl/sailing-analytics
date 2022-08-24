package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.markpassingcalculation.MarkPassingCalculator;
import com.sap.sailing.domain.markpassinghash.MarkPassingHashFingerprint;
import com.sap.sailing.domain.markpassinghash.MarkPassingHashCalculationFactory;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.test.OnlineTracTracBasedTest;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sse.mongodb.MongoDBConfiguration;

public class MarkPassingHashConversionTest extends OnlineTracTracBasedTest {
    DynamicTrackedRaceImpl trackedRace1;
    DynamicTrackedRaceImpl trackedRace2;
    private final MongoDBConfiguration dbConfiguration;

    public MarkPassingHashConversionTest() throws MalformedURLException, URISyntaxException {
        super();
        dbConfiguration = MongoDBConfiguration.getDefaultTestConfiguration();
    }

    private MongoClient newMongo() throws UnknownHostException, MongoException {
        return MongoClients.create(dbConfiguration.getMongoClientURI());
    }

    @Override
    protected String getExpectedEventName() {
        return "Academy Tracking 2011";
    }

    @Before
    public void setUp() throws Exception {
        super.setUp("event_20110505_SailingTea", // Semifinale
                /* raceId */ "01ea3604-02ef-11e1-9efc-406186cbf87c", /* liveUri */ null, /* storedUri */ null,
                new ReceiverType[] { ReceiverType.MARKPASSINGS, ReceiverType.MARKPOSITIONS, ReceiverType.RACECOURSE,
                        ReceiverType.RACESTARTFINISH, ReceiverType.RAWPOSITIONS, ReceiverType.SENSORDATA });
        trackedRace1 = getTrackedRace();
        super.setUp();
        super.setUp("event_20110505_SailingTea", // Semifinale
                /* raceId */ "01ea3604-02ef-11e1-9efc-406186cbf87c", /* liveUri */ null, /* storedUri */ null,
                new ReceiverType[] { ReceiverType.MARKPASSINGS, ReceiverType.MARKPOSITIONS, ReceiverType.RACECOURSE,
                        ReceiverType.RACESTARTFINISH, ReceiverType.RAWPOSITIONS, ReceiverType.SENSORDATA });
        trackedRace2 = getTrackedRace();
    }

    @Test
    public void testLoadingToMongo() throws UnknownHostException, MongoException {
        MarkPassingHashCalculationFactory factory = MarkPassingHashCalculationFactory.INSTANCE;
        MarkPassingCalculator calculator = new MarkPassingCalculator(trackedRace1, false, false);
        MarkPassingHashFingerprint fingerprint = factory.createFingerprint(trackedRace1);
        MongoClient myFirstMongo = newMongo();
        MongoDatabase firstDatabase = myFirstMongo.getDatabase(dbConfiguration.getDatabaseName());
        RaceIdentifier raceIdentifier = trackedRace1.getRaceIdentifier();
        new MongoObjectFactoryImpl(firstDatabase).storeFingerprintForMarkPassingHash(fingerprint, raceIdentifier);
        DomainObjectFactory dF = PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory();
        HashMap<RaceIdentifier, MarkPassingHashFingerprint> fingerprintHashMap = dF.loadFingerprintsForMarkPassingHashes();
        MarkPassingHashFingerprint fingerprintAfterDB = fingerprintHashMap.get(trackedRace1.getRaceIdentifier());
        assertTrue("Original and de-serialized copy are equal", fingerprintAfterDB.matches(trackedRace1));
    }
}