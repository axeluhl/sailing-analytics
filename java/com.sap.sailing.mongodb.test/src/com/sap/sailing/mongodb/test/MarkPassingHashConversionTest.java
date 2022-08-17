package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.sap.sailing.domain.markpassinghash.TrackedRaceHashFingerprint;
import com.sap.sailing.domain.markpassinghash.TrackedRaceHashForMaskPassingCalculationFactory;
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
        TrackedRaceHashForMaskPassingCalculationFactory factory = TrackedRaceHashForMaskPassingCalculationFactory.INSTANCE;
        TrackedRaceHashFingerprint fingerprint = factory.createFingerprint(trackedRace1);
        MongoClient myFirstMongo = newMongo();
        MongoDatabase firstDatabase = myFirstMongo.getDatabase(dbConfiguration.getDatabaseName());
        new MongoObjectFactoryImpl(firstDatabase).storeFingerprint(fingerprint);
        DomainObjectFactory dF = PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory();
        TrackedRaceHashFingerprint fingerprintAfterDB = dF.loadFingerprint(trackedRace1);
        JSONObject json1 = fingerprint.toJson();
        JSONObject json2 = fingerprintAfterDB.toJson();
        assertNotEquals("Json1 and Json2 are equal: " + json1 + " json2: " + json2, json1, json2);
    }

    @Test
    public void testloadingToMongoWithTwoRaces() throws UnknownHostException, MongoException {
        TrackedRaceHashForMaskPassingCalculationFactory factory = TrackedRaceHashForMaskPassingCalculationFactory.INSTANCE;
        TrackedRaceHashFingerprint fingerprint = factory.createFingerprint(trackedRace1);
        TrackedRaceHashFingerprint fingerprint2 = factory.createFingerprint(trackedRace2);
        MongoClient myFirstMongo = newMongo();
        MongoDatabase firstDatabase = myFirstMongo.getDatabase(dbConfiguration.getDatabaseName());
        MongoObjectFactoryImpl mOF = new MongoObjectFactoryImpl(firstDatabase);
        mOF.storeFingerprint(fingerprint);
        mOF.storeFingerprint(fingerprint2);
        DomainObjectFactory dF = PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory();
        TrackedRaceHashFingerprint fingerprint1AfterDB = dF.loadFingerprint(trackedRace1);
        TrackedRaceHashFingerprint fingerprint2AfterDB = dF.loadFingerprint(trackedRace2);
        JSONObject json1 = fingerprint.toJson();
        JSONObject json1After = fingerprint1AfterDB.toJson();
        assertNotEquals("Json1 and Json1After are not equal: " + json1 + " json1After: " + json1After, json1,
                json1After);
        JSONObject json2 = fingerprint.toJson();
        JSONObject json2After = fingerprint2AfterDB.toJson();
        assertNotEquals("Json2 and Json2After are not equal: " + json2 + " json2After: " + json2, json2, json2After);
        assertTrue(json1.equals(json2));
        assertTrue(json1After.equals(json2After));
    }

}