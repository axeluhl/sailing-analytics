package com.sap.sailing.domain.swisstimingadapter.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.swisstimingadapter.impl.SailMasterTransceiver;

public class SailMasterConnectivityTest {
    private SailMasterDummy sailMaster;
    private static final int port = 50002;
    
    private SailMasterTransceiver transceiver;
    
    @Before
    public void setUp() {
        // create a SailMaster dummy on port 50002 and launch it in a separate thread
        sailMaster = new SailMasterDummy(port);
        new Thread(sailMaster, "SailMasterDummy on port "+port).start();
        transceiver = new SailMasterTransceiver();
    }
    
    @After
    public void tearDown() throws IOException {
        Socket s = connect();
        transceiver.sendMessage("StopServer", s.getOutputStream());
    }
    
    @Test
    public void testRaceId() throws UnknownHostException, IOException {
        Socket s = connect();
        transceiver.sendMessage("RaceId", s.getOutputStream());
        String response = transceiver.receiveMessage(s.getInputStream());
        assertEquals("RaceId|4711,A wonderful test race|4712,Not such a wonderful race", response);
    }

    private Socket connect() throws UnknownHostException, IOException {
        Socket s = new Socket("localhost", port);
        return s;
    }
}
