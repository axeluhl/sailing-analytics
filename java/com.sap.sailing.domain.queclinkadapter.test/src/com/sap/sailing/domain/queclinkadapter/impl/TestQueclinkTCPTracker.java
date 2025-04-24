package com.sap.sailing.domain.queclinkadapter.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoException;
import com.sap.sailing.domain.queclinkadapter.tracker.QueclinkTCPTracker;

public class TestQueclinkTCPTracker extends AbstractQueclinkTrackerTest {
    private QueclinkTCPTracker tracker;

    @Override
    @Before
    public void setUp() throws MongoException, IOException {
        super.setUp();
        tracker = new QueclinkTCPTracker(/* pick a port */ 0, store);
    }

    @Override
    @After
    public void after() throws IOException {
        super.after();
        tracker.stop();
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
