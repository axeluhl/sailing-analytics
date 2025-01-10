package com.sap.sailing.domain.queclinkadapter.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoException;
import com.sap.sailing.domain.queclinkadapter.tracker.QueclinkUDPTracker;

public class TestQueclinkUDPTracker extends AbstractQueclinkTrackerTest {
    private QueclinkUDPTracker tracker;

    @Override
    @Before
    public void setUp() throws MongoException, IOException {
        super.setUp();
        tracker = new QueclinkUDPTracker(/* pick a port */ 0, store);
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
        final int DATAGRAM_SIZE = 1400;
        final byte[] buf = new byte[DATAGRAM_SIZE];
        assertTrue(port > 0);
        final DatagramSocket socket = new DatagramSocket();
        final InputStream inputStreamForTestData = getClass().getResourceAsStream("/queclink_stream");
        int b;
        int indexInBuf = 0;
        while ((b=inputStreamForTestData.read()) != -1) {
            buf[indexInBuf++] = (byte) b;
            if (indexInBuf == DATAGRAM_SIZE) {
                send(buf, indexInBuf, port, socket);
                indexInBuf = 0;
            }
        }
        if (indexInBuf > 0) {
            send(buf, indexInBuf, port, socket);
        }
        socket.close();
        inputStreamForTestData.close();
        Thread.sleep(5000); // wait for all data to have arrived...
        assertEquals(807, store.getNumberOfFixes(deviceIdentifier));
    }

    private void send(byte[] buf, int length, int port, DatagramSocket socket) throws IOException {
        final DatagramPacket packet = new DatagramPacket(buf, length, new InetSocketAddress("127.0.0.1", port));
        socket.send(packet);
    }
}
