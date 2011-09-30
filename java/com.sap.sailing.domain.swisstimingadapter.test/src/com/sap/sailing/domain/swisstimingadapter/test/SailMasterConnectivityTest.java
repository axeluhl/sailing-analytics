package com.sap.sailing.domain.swisstimingadapter.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.swisstimingadapter.Race;
import com.sap.sailing.domain.swisstimingadapter.SailMasterConnector;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.util.Util;

public class SailMasterConnectivityTest {
    private SailMasterDummy sailMaster;
    private static final int port = 50002;
    
    private SailMasterConnector connector;
    private Thread dummyServerThread;
    
    @Before
    public void setUp() throws InterruptedException {
        // create a SailMaster dummy on port 50002 and launch it in a separate thread
        sailMaster = new SailMasterDummy(port);
        dummyServerThread = new Thread(sailMaster, "SailMasterDummy on port "+port);
        dummyServerThread.start();
        Thread.sleep(100); // give dummy sail master server a change to start listening on its socket
        connector = SwissTimingFactory.INSTANCE.createSailMasterConnector("localhost", port);
    }
    
    @After
    public void tearDown() throws IOException, InterruptedException {
        connector.sendRequestAndGetResponse("StopServer");
        dummyServerThread.join();
    }
    
    @Test
    public void testRaceId() throws UnknownHostException, IOException {
        SailMasterMessage response = connector.sendRequestAndGetResponse("RaceId");
        assertEquals("RaceId|4711,A wonderful test race|4712,Not such a wonderful race", response.getMessage());
        assertArrayEquals(new String[] { "RaceId", "4711,A wonderful test race",
                "4712,Not such a wonderful race" }, response.getSections());
        assertArrayEquals(new Object[] { "4711", "A wonderful test race" }, response.getSections()[1].split(","));
        assertArrayEquals(new Object[] { "4712", "Not such a wonderful race" }, response.getSections()[2].split(","));
    }
    
    @Test
    public void testStructuredRaceId() throws UnknownHostException, IOException {
        Iterable<Race> races = connector.getRaces();
        assertEquals(2, Util.size(races));
        Iterator<Race> i = races.iterator();
        Race r1 = i.next();
        assertEquals("4711", r1.getRaceID());
        assertEquals("A wonderful test race", r1.getDescription());
        Race r2 = i.next();
        assertEquals("4712", r2.getRaceID());
        assertEquals("Not such a wonderful race", r2.getDescription());
    }
}
