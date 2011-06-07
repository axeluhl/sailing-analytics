package com.sap.sailing.expeditionconnector.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.declination.Declination;
import com.sap.sailing.declination.DeclinationService;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.expeditionconnector.ExpeditionListener;
import com.sap.sailing.expeditionconnector.ExpeditionMessage;
import com.sap.sailing.expeditionconnector.UDPExpeditionReceiver;
import com.sap.sailing.expeditionconnector.WindTracker;
import com.sap.sailing.util.Util;

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
        receiver.removeListener(listener);
    }
    
    @Test
    public void sendAndValidateValidDatagrams() throws IOException, InterruptedException {
        receiver.addListener(listener, /* validMessagesOnly */ false);
        sendAndWaitABit(validLines);
        assertEquals(validLines.length, messages.size());
        ExpeditionMessage m = messages.get(0);
        assertEquals(0, m.getBoatID());
        assertTrue(m.hasValue(1));
        assertEquals(7.700, m.getValue(1), 0.00000001);
        assertTrue(m.hasValue(2));
        assertEquals(-39.0, m.getValue(2), 0.00000001);
        assertTrue(m.hasValue(3));
        assertEquals(23.00, m.getValue(3), 0.00000001);
        assertTrue(m.hasValue(9));
        assertEquals(319.0, m.getValue(9), 0.00000001);
        assertTrue(m.hasValue(146));
        assertEquals(40348.390035, m.getValue(146), 0.00000001);
    }

    @Test
    public void testTimeStampConversion() throws IOException, InterruptedException {
        receiver.addListener(listener, /* validMessagesOnly */ true);
        sendAndWaitABit(new String[] { "#0,1,7.900,2,-42.0,3,25.90,9,323.0,13,326.0,48,54.511867,49,10.152700,50,340.3,146,40348.578310*25" });
        assertEquals(1, messages.size());
        assertTrue(messages.get(0).hasValue(ExpeditionMessage.ID_GPS_TIME));
        assertTrue(messages.get(0).hasValue(ExpeditionMessage.ID_GPS_LAT));
        assertTrue(messages.get(0).hasValue(ExpeditionMessage.ID_GPS_LNG));
        GPSFix fix = messages.get(0).getGPSFix();
        assertNotNull(fix);
        TimePoint time = fix.getTimePoint();
        Date date = time.asDate();
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.setTime(date);
        assertEquals(2010, cal.get(Calendar.YEAR));
        assertEquals(5, cal.get(Calendar.MONTH));
        assertEquals(19, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(13, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(52, cal.get(Calendar.MINUTE));
        assertEquals(46, cal.get(Calendar.SECOND));
    }

    @Test
    public void sendAndValidateSomeInvalidDatagrams() throws IOException, InterruptedException {
        receiver.addListener(listener, /* validMessagesOnly */ true);
        sendAndWaitABit(someValidWithFourInvalidLines);
        assertEquals(someValidWithFourInvalidLines.length-4 /* assuming 4 lines are invalid */, messages.size());
    }

    @Test
    public void sendAndValidateSomeInvalidDatagramsAcceptingInvalid() throws IOException, InterruptedException {
        receiver.addListener(listener, /* validMessagesOnly */ false);
        sendAndWaitABit(someValidWithFourInvalidLines);
        assertEquals(someValidWithFourInvalidLines.length, messages.size());
    }

    private void sendAndWaitABit(String[] linesToSend) throws IOException, InterruptedException {
        for (String line : linesToSend) {
            byte[] lineAsBytes = line.getBytes();
            System.arraycopy(lineAsBytes, 0, buf, 0, lineAsBytes.length);
            packet.setLength(lineAsBytes.length);
            socket.send(packet);
        }
        Thread.sleep(1000 /* ms */); // wait until all data was received
        receiver.stop();
        receiverThread.join(); // ensure the received has cleaned up and closed its socket
    }
    
    @Test
    public void testWindTrackerWithDeclination() throws IOException, InterruptedException, ClassNotFoundException, ParseException {
        MockedTrackedRace race = new MockedTrackedRace();
        DeclinationService declinationService = DeclinationService.INSTANCE;
        WindTracker windTracker = new WindTracker(race, declinationService);
        receiver.addListener(listener, /* validMessagesOnly */ true);
        receiver.addListener(windTracker, /* validMessagesOnly */ true);
        String[] lines = new String[validLines.length+1];
        lines[0] = "#0,1,7.900,2,-42.0,3,25.90,9,323.0,13,326.0,48,54.511867,49,10.152700,50,340.3,146,40348.578310*25";
        System.arraycopy(validLines, 0, lines, 1, validLines.length);
        // ensure declination service has 2011 loaded (which takes a few seconds)
        declinationService.getDeclination(
                new MillisecondsTimePoint(new SimpleDateFormat("yyyy-MM-dd").parse("2011-07-01").getTime()),
                new DegreePosition(54, 9), /* timeoutForOnlineFetchInMilliseconds */3000);
        sendAndWaitABit(lines);
        Thread.sleep(3000); // wait until at least the declination was received
        assertEquals(lines.length, messages.size());
        // note that the tracks are ordered by timestamps; however, not all Expedition messages have an original timestamp.
        // So, some of them are timestamped with "now" which shuffles ordering. We keep track of the matched wind fixes in
        // a map
        Set<Wind> matched = new HashSet<Wind>();
        // now assert that wind bearings have undergone declination correction
        Position lastKnownPosition = null;
        for (ExpeditionMessage m : messages) {
            if (m.getGPSFix() != null) {
                lastKnownPosition = m.getGPSFix().getPosition();
            }
            if (m.getTrueWind() != null) {
                Declination declination = declinationService.getDeclination(m.getTimePoint(), lastKnownPosition,
                        /* timeoutForOnlineFetchInMilliseconds */5000);
                for (Wind recordedWind : race.getWindTrack().getFixes()) {
                    if (Math.abs(m.getTrueWindBearing().getDegrees() + declination.getBearingCorrectedTo(m.getTimePoint()).getDegrees() -
                        recordedWind.getBearing().getDegrees()) <= 0.0000001) {
                        matched.add(recordedWind);
                        break;
                    }
                }
            }
        }
        assertEquals(Util.size(race.getWindTrack().getFixes()), matched.size());
    }
}
