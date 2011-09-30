package com.sap.sailing.domain.swisstimingadapter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.swisstimingadapter.SailMasterConnector;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;

public class SailMasterConnectivityTest {
    private SailMasterDummy sailMaster;
    private static final int port = 50002;
    
    private SailMasterConnector connector;
    
    @Before
    public void setUp() throws InterruptedException {
        // create a SailMaster dummy on port 50002 and launch it in a separate thread
        sailMaster = new SailMasterDummy(port);
        new Thread(sailMaster, "SailMasterDummy on port "+port).start();
        Thread.sleep(100); // give dummy sail master server a change to start listening on its socket
        connector = SwissTimingFactory.INSTANCE.createSailMasterConnector("localhost", port);
    }
    
    @After
    public void tearDown() throws IOException {
        connector.sendRequestAndGetResponse("StopServer");
    }
    
    @Test
    public void testRaceId() throws UnknownHostException, IOException {
        SailMasterMessage response = connector.sendRequestAndGetResponse("RaceId");
        assertEquals("RaceId|4711,A wonderful test race|4712,Not such a wonderful race", response.getMessage());
        assertArrayEquals(new String[] { "RaceId", "4711,A wonderful test race",
                "4712,Not such a wonderful race" }, response.getSections());
    }
}
