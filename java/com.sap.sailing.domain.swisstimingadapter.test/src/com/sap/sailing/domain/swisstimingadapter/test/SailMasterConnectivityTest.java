package com.sap.sailing.domain.swisstimingadapter.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.swisstimingadapter.Competitor;
import com.sap.sailing.domain.swisstimingadapter.Course;
import com.sap.sailing.domain.swisstimingadapter.Mark;
import com.sap.sailing.domain.swisstimingadapter.Race;
import com.sap.sailing.domain.swisstimingadapter.SailMasterConnector;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;
import com.sap.sailing.domain.swisstimingadapter.StartList;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.util.Util;

public class SailMasterConnectivityTest {
    private static final int port = 50002;
    
    private SailMasterConnector connector;
    private Thread dummyServerThread;
    
    /**
     * Interactive test console. Reads lines from the command line until "exit" is entered, wraps those lines with
     * STX/ETX markers and sends them to the server and displays the server's response on stdout.
     * 
     * @param args
     *            [ hostname port ]; if hostname/port are provided, connects to that server; otherwise, a dummy server
     *            is started locally and connected to
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        SailMasterConnectivityTest instance = new SailMasterConnectivityTest();
        instance.runInteractively(args);
    }

    private void runInteractively(String[] args) throws IOException, UnknownHostException, InterruptedException {
        if (args.length == 0) {
            setUp();
        } else {
            connector = SwissTimingFactory.INSTANCE.createSailMasterConnector(args[0], Integer.valueOf(args[1]));
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line = br.readLine();
        while (line != null && !line.equals("exit")) {
            SailMasterMessage response = connector.sendRequestAndGetResponse(line);
            System.out.println(response);
            line = br.readLine();
        }
    }
    
    @Before
    public void setUp() throws InterruptedException {
        startSailMasterDummy();
        connector = SwissTimingFactory.INSTANCE.createSailMasterConnector("localhost", port);
    }

    private void startSailMasterDummy() throws InterruptedException {
        // create a SailMaster dummy on port 50002 and launch it in a separate thread
        SailMasterDummy sailMaster = new SailMasterDummy(port);
        dummyServerThread = new Thread(sailMaster, "SailMasterDummy on port "+port);
        dummyServerThread.start();
        Thread.sleep(100); // give dummy sail master server a change to start listening on its socket
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

    @Test
    public void testCourseConfig() throws UnknownHostException, IOException {
        Iterable<Race> races = connector.getRaces();
        Iterator<Race> i = races.iterator();
        Race r1 = i.next();
        Course course1 = connector.getCourse(r1.getRaceID());
        assertEquals(r1.getRaceID(), course1.getRaceID());
        assertEquals(2, Util.size(course1.getMarks()));
        Iterator<Mark> mi = course1.getMarks().iterator();
        Mark m11 = mi.next();
        assertEquals("Lee Gate", m11.getDescription());
        assertEquals(Arrays.asList(new String[] { "LG1", "LG2" }), m11.getDevices());
        assertEquals(1, m11.getIndex());
        Mark m12 = mi.next();
        assertEquals("Windward", m12.getDescription());
        assertEquals(Arrays.asList(new String[] { "WW1" }), m12.getDevices());
        assertEquals(2, m12.getIndex());

        Race r2 = i.next();
        Course course2 = connector.getCourse(r2.getRaceID());
        assertEquals(r2.getRaceID(), course2.getRaceID());
        assertEquals(3, Util.size(course2.getMarks()));
        mi = course2.getMarks().iterator();
        Mark m21 = mi.next();
        assertEquals("Lee Gate", m21.getDescription());
        assertEquals(Arrays.asList(new String[] { "LG1", "LG2" }), m21.getDevices());
        assertEquals(1, m21.getIndex());
        Mark m22 = mi.next();
        assertEquals("Windward", m22.getDescription());
        assertEquals(Arrays.asList(new String[] { "WW1" }), m22.getDevices());
        assertEquals(2, m22.getIndex());
        Mark m23 = mi.next();
        assertEquals("Offset", m23.getDescription());
        assertEquals(Arrays.asList(new String[] { "OS1" }), m23.getDevices());
        assertEquals(3, m23.getIndex());
    }
    
    @Test
    public void testStartList() throws UnknownHostException, IOException {
        Iterable<Race> races = connector.getRaces();
        Iterator<Race> i = races.iterator();
        Race r1 = i.next();
        StartList sl1 = connector.getStartList(r1.getRaceID());
        assertEquals(r1.getRaceID(), sl1.getRaceID());
        assertEquals(2, Util.size(sl1.getCompetitors()));
        Iterator<Competitor> ci = sl1.getCompetitors().iterator();
        Competitor polgarKoy = ci.next();
        assertEquals("GER 8414", polgarKoy.getBoatID());
        assertEquals("GER", polgarKoy.getThreeLetterIOCCode());
        assertEquals("Polgar/Koy", polgarKoy.getName());
        Competitor schlonskiBohn = ci.next();
        assertEquals("GER 8140", schlonskiBohn.getBoatID());
        assertEquals("GER", schlonskiBohn.getThreeLetterIOCCode());
        assertEquals("Schlonski/Bohn", schlonskiBohn.getName());
        Race r2 = i.next();
        StartList sl2 = connector.getStartList(r2.getRaceID());
        assertEquals(r2.getRaceID(), sl2.getRaceID());
        assertEquals(3, Util.size(sl2.getCompetitors()));
        ci = sl2.getCompetitors().iterator();
        Competitor stanjekKleen = ci.next();
        assertEquals("GER 8340", stanjekKleen.getBoatID());
        assertEquals("GER", stanjekKleen.getThreeLetterIOCCode());
        assertEquals("Stanjek/Kleen", stanjekKleen.getName());
        Competitor babendererdeJacobs = ci.next();
        assertEquals("GER 8433", babendererdeJacobs.getBoatID());
        assertEquals("GER", babendererdeJacobs.getThreeLetterIOCCode());
        assertEquals("Babendererde/Jacobs", babendererdeJacobs.getName());
        Competitor elsnerSchulz = ci.next();
        assertEquals("GER 8299", elsnerSchulz.getBoatID());
        assertEquals("GER", elsnerSchulz.getThreeLetterIOCCode());
        assertEquals("Elsner/Schulz", elsnerSchulz.getName());
    }
}
