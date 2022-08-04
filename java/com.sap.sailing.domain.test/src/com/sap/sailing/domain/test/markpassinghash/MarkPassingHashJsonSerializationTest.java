package com.sap.sailing.domain.test.markpassinghash;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.ControlPointWithTwoMarks;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.ControlPointWithTwoMarksImpl;
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.markpassinghash.TrackedRaceHashFingerprint;
import com.sap.sailing.domain.markpassinghash.impl.TrackedRaceHashForMarkPassingCalculationFactoryImpl;
import com.sap.sailing.domain.test.OnlineTracTracBasedTest;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class MarkPassingHashJsonSerializationTest extends OnlineTracTracBasedTest {
    private static final Logger logger = Logger.getLogger(MarkPassingHashJsonSerializationTest.class.getName());
    DynamicTrackedRaceImpl trackedRace1;
    DynamicTrackedRaceImpl trackedRace2;

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
    }

    @Test
    public void JsonSerialization() {
        TrackedRaceHashForMarkPassingCalculationFactoryImpl factory = new TrackedRaceHashForMarkPassingCalculationFactoryImpl();
        TrackedRaceHashFingerprint fingerprint1 = factory.createFingerprint(trackedRace1);
        TrackedRaceHashFingerprint fingerprint2 = factory.createFingerprint(trackedRace2);
        JSONObject json1 = fingerprint1.toJson();
        JSONObject json2 = fingerprint2.toJson();
        assertEquals("Json1 and Json2 are not equal: " + json1 + " json2: " + json2, json1, json2);
    }

    @Test
    public void JsonDeserialization() {
        TrackedRaceHashForMarkPassingCalculationFactoryImpl factory = new TrackedRaceHashForMarkPassingCalculationFactoryImpl();
        TrackedRaceHashFingerprint fingerprint1 = factory.createFingerprint(trackedRace1);
        TrackedRaceHashFingerprint fingerprint2 = factory.createFingerprint(trackedRace2);
        JSONObject json1 = fingerprint1.toJson();
        JSONObject json2 = fingerprint2.toJson();
        TrackedRaceHashFingerprint output1 = factory.fromJson(json1);
        TrackedRaceHashFingerprint output2 = factory.fromJson(json2);
        // TODO Does it make sense with the .toJson in the comparison?
        assertEquals(output1.toJson(), output2.toJson());
    }

    @Test
    public void JsonSerializationWithChangesInRace() {
        DynamicTrackedRaceImpl testRace = trackedRace2;
        TrackedRaceHashForMarkPassingCalculationFactoryImpl factory = new TrackedRaceHashForMarkPassingCalculationFactoryImpl();
        TrackedRaceHashFingerprint fingerprint1 = factory.createFingerprint(trackedRace1);
        // Change of the race should result in a different hash
        TimePoint epoch = new MillisecondsTimePoint(0l);
        TimePoint now = MillisecondsTimePoint.now();
        Map<String, Position> markPositions = new HashMap<String, Position>();
        markPositions.put("CR Start (1)", new DegreePosition(53.562944999999985, 10.010104000000046));
        markPositions.put("CR Start (2)", new DegreePosition(53.562944999999985, 10.010104000000046));
        markPositions.put("Leeward mark", new DegreePosition(53.562145000000015, 10.009252));
        markPositions.put("Luvtonne", new DegreePosition(53.560581899999995, 10.005657));
        for (Waypoint w : getTrackedRace().getRace().getCourse().getWaypoints()) {
            for (Mark mark : w.getMarks()) {
                testRace.getOrCreateTrack(mark).addGPSFix(new GPSFixImpl(markPositions.get(mark.getName()), epoch));
                testRace.getOrCreateTrack(mark).addGPSFix(new GPSFixImpl(markPositions.get(mark.getName()), now));
            }
        }
        TrackedRaceHashFingerprint fingerprint2 = factory.createFingerprint(testRace);
        JSONObject json1 = fingerprint1.toJson();
        JSONObject json2 = fingerprint2.toJson();
        assertNotEquals("Json1 and Json2 are equal: " + json1 + " json2: " + json2, json1, json2);
    }

    @Test
    public void JsonDeserializationWithChangesInRace() {
        DynamicTrackedRaceImpl testRace = trackedRace2;
        TrackedRaceHashForMarkPassingCalculationFactoryImpl factory = new TrackedRaceHashForMarkPassingCalculationFactoryImpl();
        TrackedRaceHashFingerprint fingerprint1 = factory.createFingerprint(trackedRace1);
        // Change of the race should result in a different hash
        TimePoint epoch = new MillisecondsTimePoint(0l);
        TimePoint now = MillisecondsTimePoint.now();
        Map<String, Position> markPositions = new HashMap<String, Position>();
        markPositions.put("CR Start (1)", new DegreePosition(53.562944999999985, 10.010104000000046));
        markPositions.put("CR Start (2)", new DegreePosition(53.562944999999985, 10.010104000000046));
        markPositions.put("Leeward mark", new DegreePosition(53.562145000000015, 10.009252));
        markPositions.put("Luvtonne", new DegreePosition(53.560581899999995, 10.005657));
        for (Waypoint w : getTrackedRace().getRace().getCourse().getWaypoints()) {
            for (Mark mark : w.getMarks()) {
                testRace.getOrCreateTrack(mark).addGPSFix(new GPSFixImpl(markPositions.get(mark.getName()), epoch));
                testRace.getOrCreateTrack(mark).addGPSFix(new GPSFixImpl(markPositions.get(mark.getName()), now));
            }
        }
        TrackedRaceHashFingerprint fingerprint2 = factory.createFingerprint(testRace);
        JSONObject json1 = fingerprint1.toJson();
        JSONObject json2 = fingerprint2.toJson();
        TrackedRaceHashFingerprint output1 = factory.fromJson(json1);
        TrackedRaceHashFingerprint output2 = factory.fromJson(json2);
        assertNotEquals(output1.toJson(), output2.toJson());
    }

    @Test
    public void WaypointChangePassingInstructionTest() {
        DynamicTrackedRaceImpl testRace = trackedRace2;
        TrackedRaceHashForMarkPassingCalculationFactoryImpl factory = new TrackedRaceHashForMarkPassingCalculationFactoryImpl();
        Waypoint wp = testRace.getRace().getCourse().getFirstWaypoint();
        ControlPoint cP = wp.getControlPoint();
        WaypointImpl wpNew = new WaypointImpl(cP, PassingInstruction.Gate);
        testRace.getRace().getCourse().removeWaypoint(0);
        testRace.getRace().getCourse().addWaypoint(0, wpNew);
        TrackedRaceHashFingerprint fingerprint1 = factory.createFingerprint(trackedRace1);
        TrackedRaceHashFingerprint fingerprint2 = factory.createFingerprint(testRace);
        JSONObject json1 = fingerprint1.toJson();
        JSONObject json2 = fingerprint2.toJson();
        assertNotEquals("Json1 and Json2 are equal: " + json1 + " json2: " + json2, json1, json2);
    }

    @Test
    public void ControlPointChangeTest() {
        DynamicTrackedRaceImpl testRace = trackedRace2;
        TrackedRaceHashForMarkPassingCalculationFactoryImpl factory = new TrackedRaceHashForMarkPassingCalculationFactoryImpl();
        Waypoint wp = testRace.getRace().getCourse().getFirstWaypoint();
        MarkImpl mark1 = new MarkImpl("eins");
        MarkImpl mark2 = new MarkImpl("zwei");
        Mark gate1 = new MarkImpl("Gate1");
        Mark gate2 = new MarkImpl("Gate2");
        ControlPointWithTwoMarks cp = new ControlPointWithTwoMarksImpl(gate1, gate2, "cp", "");
        Waypoint wpNew = new WaypointImpl(cp, PassingInstruction.None);
        testRace.getRace().getCourse().removeWaypoint(0);
        testRace.getRace().getCourse().addWaypoint(0, wpNew);
        TrackedRaceHashFingerprint fingerprint1 = factory.createFingerprint(trackedRace1);
        TrackedRaceHashFingerprint fingerprint2 = factory.createFingerprint(testRace);
        JSONObject json1 = fingerprint1.toJson();
        JSONObject json2 = fingerprint2.toJson();
        assertNotEquals("Json1 and Json2 are equal: " + json1 + " json2: " + json2, json1, json2);
    }
}
