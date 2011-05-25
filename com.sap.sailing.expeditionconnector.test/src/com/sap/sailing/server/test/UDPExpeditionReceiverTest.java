package com.sap.sailing.server.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.expeditionconnector.ExpeditionListener;
import com.sap.sailing.expeditionconnector.ExpeditionMessage;
import com.sap.sailing.expeditionconnector.UDPExpeditionReceiver;

public class UDPExpeditionReceiverTest {
    private String[] validLines;
    private String[] someValidWithFourInvalidLines;
    final List<ExpeditionMessage> messages = new ArrayList<ExpeditionMessage>();
    final int PORT = 9876;
    private DatagramPacket packet;
    private DatagramSocket socket;
    private byte[] buf;
    private UDPExpeditionReceiver receiver;
    private ExpeditionListener listener;
    private Thread receiverThread;

    @Before
    public void setUp() throws UnknownHostException, SocketException {
        validLines = new String[] {
                "#0,1,7.700,2,-39.0,3,23.00,9,319.0,12,1.17,146,40348.390035*37",
                "#0,4,-54.9,5,17.69,6,263.1,9,318.0*0D",
                "#0,9,318.0*0E",
                "#0,9,318.0*0E",
                "#0,9,318.0*0E",
                "#0,9,318.0*0E",
                "#0,9,318.0*0E",
                "#0,9,318.0*0E",
                "#0,9,318.0*0E",
                "#0,9,318.0*0E",
                "#0,1,7.700,2,-39.0,3,24.30,9,318.0,12,1.07,50,326.3,146,40348.390046*18",
                "#0,4,-53.8,5,18.95,6,266.2,9,320.0*0A",
                "#0,9,320.0*05",
                "#0,9,320.0*05",
                "#0,9,320.0*05",
                "#0,9,320.0*05",
                "#0,9,320.0*05",
                "#0,9,320.0*05",
                "#0,9,320.0*05",
                "#0,9,320.0*05",
                "#0,1,7.700,2,-36.0,3,25.10,4,-49.5,5,19.41,6,271.5,9,321.0,12,1.07,50,327.3,146,40348.390058*10",
                "#0,9,321.0*04"
        };

        someValidWithFourInvalidLines = new String[] {
                "#0,1,7.700,2,-39.0,3,23.00,9,319.0,12,1.17,146,40348.390035*37",
                "#0,4,-54.9,5,17.69,6,263.1,9,318.0*0D",
                "#0,9,318.0*0E",
                "#0,9,318.0*0E",
                "#0,9,318.0*0E",
                "#0,9,318.0*0F", // invalid
                "#0,9,318.0*0E",
                "#0,9,318.0*0E",
                "#0,9,318.0*0E",
                "#0,9,318.0*0E",
                "#0,1,7.700,2,-39.0,3,24.30,9,318.0,12,1.07,50,326.3,146,40348.390046*18",
                "#0,4,-53.8,5,18.95,6,266.2,9,320.0*3A", // invalid
                "#0,9,320.0*05",
                "#0,9,323.0*05", // invalid
                "#0,9,320.0*05",
                "#0,9,320.0*05",
                "#0,9,320.0*05",
                "#1,9,320.0*05", // invalid
                "#0,9,320.0*05",
                "#0,9,320.0*05",
                "#0,1,7.700,2,-36.0,3,25.10,4,-49.5,5,19.41,6,271.5,9,321.0,12,1.07,50,327.3,146,40348.390058*10",
                "#0,9,321.0*04"
        };
        buf = new byte[512];
        packet = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(), PORT);
        socket = new DatagramSocket();
        receiver = new UDPExpeditionReceiver(PORT);
        receiverThread = new Thread(receiver, "Expedition Receiver");
        receiverThread.start();
        listener = new ExpeditionListener() {
            @Override
            public void received(ExpeditionMessage message) {
                messages.add(message);
            }
        };
    }
    
    @After
    public void tearDown() {
        socket.close();
    }
    
    @Test
    public void sendAndValidateValidDatagrams() throws IOException, InterruptedException {
        receiver.addListener(listener, /* validMessagesOnly */ false);
        sendAndWaitABit(validLines);
        assertEquals(validLines.length, messages.size());
        listener.toString(); // just use, ensuring it won't be GCed
    }

    @Test
    public void sendAndValidateSomeInvalidDatagrams() throws IOException, InterruptedException {
        receiver.addListener(listener, /* validMessagesOnly */ true);
        sendAndWaitABit(someValidWithFourInvalidLines);
        assertEquals(someValidWithFourInvalidLines.length-4 /* assuming 4 lines are invalid */, messages.size());
        listener.toString(); // just use, ensuring it won't be GCed
    }

    private void sendAndWaitABit(String[] linesToSend) throws IOException, InterruptedException {
        for (String line : linesToSend) {
            byte[] lineAsBytes = line.getBytes();
            System.arraycopy(lineAsBytes, 0, buf, 0, lineAsBytes.length);
            packet.setLength(lineAsBytes.length);
            socket.send(packet);
        }
        Thread.sleep(500 /* ms */); // wait until all data was received
        receiver.stop();
        receiverThread.join(); // ensure the received has cleaned up and closed its socket
    }
}
