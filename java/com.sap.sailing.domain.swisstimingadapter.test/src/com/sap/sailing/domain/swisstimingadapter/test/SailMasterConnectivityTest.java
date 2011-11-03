package com.sap.sailing.domain.swisstimingadapter.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.swisstimingadapter.Competitor;
import com.sap.sailing.domain.swisstimingadapter.Course;
import com.sap.sailing.domain.swisstimingadapter.Fix;
import com.sap.sailing.domain.swisstimingadapter.Mark;
import com.sap.sailing.domain.swisstimingadapter.MessageType;
import com.sap.sailing.domain.swisstimingadapter.Race;
import com.sap.sailing.domain.swisstimingadapter.RaceStatus;
import com.sap.sailing.domain.swisstimingadapter.SailMasterConnector;
import com.sap.sailing.domain.swisstimingadapter.SailMasterListener;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;
import com.sap.sailing.domain.swisstimingadapter.StartList;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.util.Util;
import com.sap.sailing.util.Util.Pair;
import com.sap.sailing.util.Util.Triple;

public class SailMasterConnectivityTest {
    private static final int port = 24354;
    
    private SailMasterConnector connector;
    private Thread dummyServerThread;
    private SailMasterDummy sailMaster;
    
    @Before
    public void setUp() throws InterruptedException {
        startSailMasterDummy();
        connector = SwissTimingFactory.INSTANCE.createSailMasterConnector("localhost", port);
    }

    private void startSailMasterDummy() throws InterruptedException {
        // create a SailMaster dummy on port 50002 and launch it in a separate thread
        sailMaster = new SailMasterDummy(port);
        dummyServerThread = new Thread(sailMaster, "SailMasterDummy on port "+port);
        dummyServerThread.start();
        Thread.sleep(100); // give dummy sail master server a change to start listening on its socket
    }
    
    @After
    public void tearDown() throws IOException, InterruptedException {
        connector.sendRequestAndGetResponse(MessageType._STOPSERVER);
        dummyServerThread.join();
        Thread.sleep(100); // give socket subsystem a chance to free up the port
    }
    
    @Test
    public void testRPD() throws IOException, InterruptedException {
        final String[] raceIDResult = new String[1];
        final RaceStatus[] raceStatusResult = new RaceStatus[1];
        final Collection<Fix> fixesResult = new ArrayList<Fix>();
        connector.addSailMasterListener(new SailMasterListener() {
            @Override
            public void receivedTimingData(String raceID, String boatID, List<Triple<Integer, Integer, Long>> markIndicesRanksAndTimesSinceStartInMilliseconds) {}
            @Override
            public void receivedStartList(String raceID, List<Triple<String, String, String>> boatIDsISOCountryCodesAndNames) {}
            @Override
            public void receivedCourseConfiguration(String raceID, List<Mark> marks) {}
            @Override
            public void receivedClockAtMark(String raceID, List<Triple<Integer, TimePoint, String>> markIndicesTimePointsAndBoatIDs) {}
            @Override
            public void receivedAvailableRaces(List<Pair<String, String>> raceIDsAndDescriptions) {}
            
            @Override
            public void receivedRacePositionData(String raceID, RaceStatus status, TimePoint timePoint, TimePoint startTime,
                    Long millisecondsSinceRaceStart, Integer nextMarkIndexForLeader, Distance distanceToNextMarkForLeaderInMeters,
                    Collection<Fix> fixes) {
                raceIDResult[0] = raceID;
                raceStatusResult[0] = status;
                fixesResult.addAll(fixes);
                synchronized (SailMasterConnectivityTest.this) {
                    SailMasterConnectivityTest.this.notifyAll();
                }
            }
        });
        synchronized (this) {
            sailMaster.sendEvent("RPD|W4702|1|18:04:56|||||61|SB;2;0;50.621920;-2.366147;0.2;;90;;;;|MB4;4;0;50.604867;-2.374324;0.8;;45;;;;|MB3;4;0;50.607746;-2.388263;0.8;;90;;;;|MB2;4;0;50.619930;-2.354203;2.3;;169;;;;|MB1;4;0;50.620144;-2.366143;0.6;;297;;;;|M7;5;0;50.621861;-2.366225;0.0;;90;;;;|M6;5;0;50.607597;-2.387903;0.6;;90;;;;|M5;5;0;50.605037;-2.373653;0.4;;270;;;;|M4;5;0;50.617101;-2.352103;0.6;;45;;;;|M3;5;0;50.620275;-2.366998;0.4;;225;;;;|M2;5;0;50.619830;-2.366461;0.0;;225;;;;|M1;5;0;50.617398;-2.352731;0.6;;225;;;;|FP;4;0;50.619943;-2.354210;0.6;;198;;;;|FB;4;0;50.619953;-2.354910;0.0;;270;;;;|SP;4;0;50.618835;-2.364202;0.4;;0;;;;|ARG_8;1;0;50.615682;-2.380307;5.2;;;188;0;;;|AUS_7;1;0;50.612972;-2.378266;5.8;;;185;0;;;|AUT_431;1;0;50.608316;-2.373561;6.0;;;273;0;;;|BRA_177;1;0;50.612929;-2.378144;4.7;;;193;0;;;|BRA_1187;1;0;50.614505;-2.380224;5.4;;;198;0;;;|CAN_11;1;0;50.610103;-2.374437;5.8;;;278;0;;;|CAN_610;1;0;50.615942;-2.380376;4.9;;;188;0;;;|CHN_1211;1;0;50.615443;-2.380390;5.4;;;273;0;;;|CHN_1261;1;0;50.616746;-2.382584;4.9;;;194;0;;;|CHN_221;1;0;50.608719;-2.374986;6.0;;;277;0;;;|CHN_616;1;0;50.609920;-2.376789;5.6;;;278;0;;;|CRO_111;1;0;50.610915;-2.376399;5.2;;;187;0;;;|DEN_143;1;0;50.611673;-2.378124;5.8;;;276;0;;;|ESP_133;1;0;50.616223;-2.381449;5.1;;;186;0;;;|ESP_696;1;0;50.614188;-2.381017;5.6;;;190;0;;;|EST_20;1;0;50.614217;-2.379679;5.4;;;187;0;;;|FRA_12;1;0;50.615051;-2.382063;5.2;;;269;0;;;|FRA_4;1;0;50.614006;-2.378709;5.1;;;193;0;;;|FRA_9;1;0;50.613190;-2.377504;6.2;;;192;0;;;|GBR_812;1;0;50.614486;-2.379102;5.2;;;190;0;;;|GBR_831;1;;;;;;;;;;|GBR_841;1;0;50.608849;-2.373400;3.7;;;281;0;;;|GBR_847;1;0;50.614911;-2.380471;4.9;;;195;0;;;|GBR_850;1;0;50.609848;-2.375080;5.4;;;274;0;;;|GBR_855;1;0;50.614341;-2.380973;5.6;;;270;0;;;|GER_12;1;0;50.615478;-2.380866;6.2;;;252;0;;;|GER_21;1;0;50.615968;-2.380717;4.1;;;197;0;;;|GER_61;1;0;50.610922;-2.375672;4.7;;;200;0;;;|GER_66;1;0;50.610306;-2.374592;5.6;;;209;0;;;|GER_72;1;0;50.615883;-2.381169;4.7;;;186;0;;;|ISR_311;1;0;50.608476;-2.373281;5.2;;;197;0;;;|ITA_23;1;0;50.610968;-2.377932;6.0;;;279;0;;;|JPN_4151;1;0;50.610347;-2.376655;6.6;;;284;0;;;|JPN_4321;1;0;50.610692;-2.374797;5.4;;;195;0;;;|NED_11;1;0;50.611364;-2.377911;5.8;;;279;0;;;|NED_24;1;;;;;;;;;;|NZL_75;1;0;50.609816;-2.374550;5.6;;;194;0;;;|RUS_12;1;0;50.614816;-2.379199;5.1;;;194;0;;;|RUS_700;1;0;50.616436;-2.381705;5.8;;;270;0;;;|SLO_64;1;0;50.614399;-2.379828;5.2;;;195;0;;;|SUI_14;1;0;50.615842;-2.381063;5.2;;;279;0;;;|SWE_342;1;0;50.613742;-2.378664;5.6;;;270;0;;;|SWE_344;1;0;50.607689;-2.373006;5.8;;;283;0;;;|USA_1712;1;0;50.614223;-2.378889;5.1;;;191;0;;;|USA_1736;1;0;50.612042;-2.377204;4.5;;;185;0;;;|USA_1757;1;0;50.609654;-2.374565;4.7;;;190;0;;;");
            this.wait();
        }
        assertEquals("W4702", raceIDResult[0]);
        assertEquals(RaceStatus.ARMED, raceStatusResult[0]);
        assertEquals(59, fixesResult.size());
    }
    
    @Test
    public void testRAC() throws UnknownHostException, IOException, InterruptedException {
        SailMasterMessage response = connector.sendRequestAndGetResponse(MessageType.RAC);
        assertEquals(MessageType.RAC.name()+"!|2|4711;A wonderful test race|4712;Not such a wonderful race", response.getMessage());
        assertArrayEquals(new String[] { "RAC!", "2", "4711;A wonderful test race",
                "4712;Not such a wonderful race" }, response.getSections());
        assertArrayEquals(new Object[] { "4711", "A wonderful test race" }, response.getSections()[2].split(";"));
        assertArrayEquals(new Object[] { "4712", "Not such a wonderful race" }, response.getSections()[3].split(";"));
    }
    
    @Test
    public void testRaceTime() throws UnknownHostException, IOException, ParseException, InterruptedException {
        Iterable<Race> races = connector.getRaces();
        Iterator<Race> i = races.iterator();
        Race r1 = i.next();
        TimePoint start1 = connector.getStartTime(r1.getRaceID());
        TimePoint now = MillisecondsTimePoint.now();
        Calendar cal = new GregorianCalendar();
        Calendar nowCal = new GregorianCalendar();
        nowCal.setTime(now.asDate());
        cal.setTime(start1.asDate());
        assertSameDay(nowCal, cal);
        // 10:15:22
        assertEquals(10, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(15, cal.get(Calendar.MINUTE));
        assertEquals(22, cal.get(Calendar.SECOND));
        Race r2 = i.next();
        TimePoint start2 = connector.getStartTime(r2.getRaceID());
        cal.setTime(start2.asDate());
        assertSameDay(nowCal, cal);
        // 18:17:23
        assertEquals(18, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(17, cal.get(Calendar.MINUTE));
        assertEquals(23, cal.get(Calendar.SECOND));
    }

    private void assertSameDay(Calendar nowCal, Calendar cal) {
        for (int field : new int[] { Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH }) {
            assertEquals(nowCal.get(field), cal.get(field));
        }
    }
    
    @Test
    public void testStructuredRaceId() throws UnknownHostException, IOException, InterruptedException {
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
    public void testCourseConfig() throws UnknownHostException, IOException, InterruptedException {
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
    public void testStartList() throws UnknownHostException, IOException, InterruptedException {
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
