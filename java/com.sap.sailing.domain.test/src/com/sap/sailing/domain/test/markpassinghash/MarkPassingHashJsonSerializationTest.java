package com.sap.sailing.domain.test.markpassinghash;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.ControlPointWithTwoMarks;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.ControlPointWithTwoMarksImpl;
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.markpassingcalculation.MarkPassingCalculator;
import com.sap.sailing.domain.markpassinghash.MarkPassingHashFingerprint;
import com.sap.sailing.domain.markpassinghash.MarkPassingHashCalculationFactory;
import com.sap.sailing.domain.test.OnlineTracTracBasedTest;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class MarkPassingHashJsonSerializationTest extends OnlineTracTracBasedTest {
    DynamicTrackedRaceImpl trackedRace1;
    DynamicTrackedRaceImpl trackedRace2;
    MarkPassingCalculator calculator1;
    MarkPassingCalculator calculator2;

    public MarkPassingHashJsonSerializationTest() throws MalformedURLException, URISyntaxException {
        super();
    }

    @Override
    protected String getExpectedEventName() {
        return "Academy Tracking 2011";
    }

    @Before
    public void setUp() throws Exception {
        super.setUp("event_20110505_SailingTea", // Semifinale
                /* raceId */ "01ea3604-02ef-11e1-9efc-406186cbf87c", /* liveUri */ null, /* storedUri */ null,
                new ReceiverType[] { ReceiverType.MARKPASSINGS, ReceiverType.MARKPOSITIONS, ReceiverType.RACECOURSE,
                        ReceiverType.RACESTARTFINISH, ReceiverType.RAWPOSITIONS, ReceiverType.SENSORDATA });
        trackedRace1 = getTrackedRace();
        super.setUp();
        super.setUp("event_20110505_SailingTea", // Semifinale
                /* raceId */ "01ea3604-02ef-11e1-9efc-406186cbf87c", /* liveUri */ null, /* storedUri */ null,
                new ReceiverType[] { ReceiverType.MARKPASSINGS, ReceiverType.MARKPOSITIONS, ReceiverType.RACECOURSE,
                        ReceiverType.RACESTARTFINISH, ReceiverType.RAWPOSITIONS, ReceiverType.SENSORDATA });
        trackedRace2 = getTrackedRace();
        calculator1 = new MarkPassingCalculator(trackedRace1, false, false);
        calculator2 = new MarkPassingCalculator(trackedRace2, false,false);
    }

    // @Test
    public void testJsonSerialization() {
        MarkPassingHashCalculationFactory factory = MarkPassingHashCalculationFactory.INSTANCE;
        MarkPassingHashFingerprint fingerprint1 = factory.createFingerprint(trackedRace1);
        assertTrue(fingerprint1.matches(trackedRace1));
        JSONObject json1 = fingerprint1.toJson();
        MarkPassingHashFingerprint output1 = factory.fromJson(json1);
        assertTrue("Original and de-serialized copy are equal", output1.matches(trackedRace1));
    }

    // @Test
    public void testJsonSerializationWithChangesInMarkFixes() {
        DynamicTrackedRaceImpl testRace = trackedRace2;
        MarkPassingHashCalculationFactory factory = MarkPassingHashCalculationFactory.INSTANCE;
        MarkPassingHashFingerprint fingerprint1 = factory.createFingerprint(trackedRace1);
        assertTrue(fingerprint1.matches(trackedRace2));
        // Change of the race should result in a different hash
        TimePoint epoch = new MillisecondsTimePoint(0l);
        TimePoint now = MillisecondsTimePoint.now();
        Map<String, Position> markPositions = new HashMap<String, Position>();
        markPositions.put("CR Start (1)", new DegreePosition(53.562944999999985, 10.010104000000046));
        markPositions.put("CR Start (2)", new DegreePosition(53.562944999999985, 10.010104000000046));
        markPositions.put("Leeward mark", new DegreePosition(53.562145000000015, 10.009252));
        markPositions.put("Luvtonne", new DegreePosition(53.560581899999995, 10.005657));
        for (Waypoint w : trackedRace2.getRace().getCourse().getWaypoints()) {
            for (Mark mark : w.getMarks()) {
                assert markPositions.containsKey(mark.getName());
                testRace.getOrCreateTrack(mark).addGPSFix(new GPSFixImpl(markPositions.get(mark.getName()), epoch));
                testRace.getOrCreateTrack(mark).addGPSFix(new GPSFixImpl(markPositions.get(mark.getName()), now));
            }
        }
        assertFalse(fingerprint1.matches(trackedRace2));
        MarkPassingHashFingerprint fingerprint2 = factory.createFingerprint(testRace);
        assertFalse(fingerprint2.matches(trackedRace1));
        JSONObject json1 = fingerprint1.toJson();
        JSONObject json2 = fingerprint2.toJson();
        assertNotEquals("Json1 and Json2 are equal: " + json1 + " json2: " + json2, json1, json2);
    }

    // @Test
    public void testWaypointChangePassingInstruction() {
        MarkPassingHashCalculationFactory factory = MarkPassingHashCalculationFactory.INSTANCE;
        MarkPassingHashFingerprint fingerprint1 = factory.createFingerprint(trackedRace1);
        assertTrue(fingerprint1.matches(trackedRace2));
        Waypoint wp = trackedRace2.getRace().getCourse().getFirstWaypoint();
        ControlPoint cP = wp.getControlPoint();
        WaypointImpl wpNew = new WaypointImpl(cP, PassingInstruction.Gate);
        trackedRace2.getRace().getCourse().removeWaypoint(0);
        trackedRace2.getRace().getCourse().addWaypoint(0, wpNew);
        MarkPassingHashFingerprint fingerprint2 = factory.createFingerprint(trackedRace2);
        assertFalse(fingerprint1.matches(trackedRace2));
        assertFalse(fingerprint2.matches(trackedRace1));
        JSONObject json1 = fingerprint1.toJson();
        JSONObject json2 = fingerprint2.toJson();
        assertNotEquals("Json1 and Json2 are equal: " + json1 + " json2: " + json2, json1, json2);
    }

    // @Test
    public void testControlPointChange() {
        MarkPassingHashCalculationFactory factory = MarkPassingHashCalculationFactory.INSTANCE;
        MarkPassingHashFingerprint fingerprint1 = factory.createFingerprint(trackedRace1);
        assertTrue(fingerprint1.matches(trackedRace2));
        Mark gate1 = new MarkImpl("Gate1");
        Mark gate2 = new MarkImpl("Gate2");
        ControlPointWithTwoMarks cp = new ControlPointWithTwoMarksImpl(gate1, gate2, "cp", "");
        Waypoint wpNew = new WaypointImpl(cp, PassingInstruction.None);
        trackedRace2.getRace().getCourse().removeWaypoint(0);
        trackedRace2.getRace().getCourse().addWaypoint(0, wpNew);
        MarkPassingHashFingerprint fingerprint2 = factory.createFingerprint(trackedRace2);
        assertFalse(fingerprint1.matches(trackedRace2));
        assertFalse(fingerprint2.matches(trackedRace1));
        JSONObject json1 = fingerprint1.toJson();
        JSONObject json2 = fingerprint2.toJson();
        assertNotEquals("Json1 and Json2 are equal: " + json1 + " json2: " + json2, json1, json2);
    }

    @Test
    public void testCompetitorFixChange() {
        DynamicTrackedRaceImpl testRace = trackedRace2;
        final DynamicTrackedRaceImpl secureRace = trackedRace2;
        MarkPassingHashCalculationFactory factory = MarkPassingHashCalculationFactory.INSTANCE;
        MarkPassingHashFingerprint fingerprint1 = factory.createFingerprint(trackedRace1);
        assertTrue(fingerprint1.matches(trackedRace2));
        Competitor firstCompetitor = trackedRace2.getRace().getCompetitors().iterator().next();
        secureRace.getTrack(firstCompetitor).lockForRead();
        GPSFixMoving firstFix;
        try {
            firstFix = testRace.getTrack(firstCompetitor).getRawFixes().iterator().next();
        } finally {
            secureRace.getTrack(firstCompetitor).unlockAfterRead();
        }
        SpeedWithBearing speed = firstFix.getSpeed();
        Position pos = firstFix.getPosition();
        TimePoint tp = firstFix.getTimePoint();
        DegreePosition degPos = new DegreePosition(pos.getLatDeg() + 0.05, pos.getLngDeg() + 0.05);
        GPSFixMoving gpsM = new GPSFixMovingImpl(degPos, tp, speed);
        testRace.getTrack(firstCompetitor).add(gpsM, true);
        MarkPassingHashFingerprint fingerprint2 = factory.createFingerprint(testRace);
        assertFalse(fingerprint1.matches(testRace));
        assertFalse(fingerprint2.matches(trackedRace1));
        JSONObject json1 = fingerprint1.toJson();
        JSONObject json2 = fingerprint2.toJson();
        assertNotEquals("Json1 and Json2 are equal: " + json1 + " json2: " + json2, json1, json2);
    }
}
