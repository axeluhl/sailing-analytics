package com.sap.sailing.domain.queclinkadapter.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.MongoException;
import com.mongodb.ReadConcern;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoDatabase;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.persistence.impl.CollectionNames;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.MongoSensorFixStoreImpl;
import com.sap.sailing.domain.queclinkadapter.tracker.QueclinkTCPTracker;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.racelog.tracking.test.mock.MockSmartphoneImeiServiceFinderFactory;
import com.sap.sailing.domain.racelogtracking.impl.SmartphoneImeiIdentifierImpl;
import com.sap.sse.mongodb.MongoDBService;

public class TestQueclinkTracker {
    protected final MockSmartphoneImeiServiceFinderFactory serviceFinderFactory = new MockSmartphoneImeiServiceFinderFactory();
    private final DeviceIdentifier deviceIdentifier = new SmartphoneImeiIdentifierImpl("860599002480051");
    private SensorFixStore store;
    private static ClientSession clientSession;
    private QueclinkTCPTracker tracker;

    @BeforeClass
    public static void setUpClass() {
        clientSession = MongoDBService.INSTANCE.startCausallyConsistentSession();
    }
    
    @Before
    public void setUp() throws MongoException, IOException {
        dropPersistedData();
        newStore();
        tracker = new QueclinkTCPTracker(/* pick a port */ 0, Charset.forName("UTF-8"), store);
    }

    private void newStore() {
        store = new MongoSensorFixStoreImpl(PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory(),
                PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory(), serviceFinderFactory, ReadConcern.MAJORITY,
                WriteConcern.MAJORITY, clientSession, clientSession);
    }

    @After
    public void after() throws IOException {
        dropPersistedData();
        tracker.stop();
    }

    private void dropPersistedData() {
        MongoDatabase db = PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory().getDatabase();
        db.getCollection(CollectionNames.GPS_FIXES.name()).withWriteConcern(WriteConcern.MAJORITY).drop(clientSession);
        db.getCollection(CollectionNames.GPS_FIXES_METADATA.name()).withWriteConcern(WriteConcern.MAJORITY).drop(clientSession);
    }
    
    @Test
    public void testSendingLogToSocket() throws IOException, InterruptedException {
        final int port = tracker.getPort();
        assertTrue(port > 0);
        final Socket socket = new Socket("127.0.0.1", port);
        final OutputStream outputStream = socket.getOutputStream();
        final InputStream inputStreamForTestData = getClass().getResourceAsStream("/queclink_stream");
        int b;
        while ((b=inputStreamForTestData.read()) != -1) {
            outputStream.write(b);
        }
        socket.close();
        inputStreamForTestData.close();
        Thread.sleep(5000); // wait for all data to have arrived...
        assertEquals(807, store.getNumberOfFixes(deviceIdentifier));
    }
}
