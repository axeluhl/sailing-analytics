package com.sap.sailing.domain.test.hash;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.test.OnlineTracTracBasedTest;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class CalculatingHashTest extends OnlineTracTracBasedTest{
    private static final Logger logger = Logger.getLogger(CalculatingHashTest.class.getName());
    DynamicTrackedRaceImpl trackedRace1;
    DynamicTrackedRaceImpl trackedRace2;
    
    public CalculatingHashTest() throws MalformedURLException, URISyntaxException {
        super();
    }
    
    @Override
    protected String getExpectedEventName() {
        return "Academy Tracking 2011";
    }
    
    @Before
    public void setUp() throws Exception{
        super.setUp("event_20110505_SailingTea", // Semifinale
                /* raceId */ "01ea3604-02ef-11e1-9efc-406186cbf87c",
                /* liveUri */ null, /* storedUri */ null,
                new ReceiverType[] { ReceiverType.MARKPASSINGS, ReceiverType.MARKPOSITIONS, ReceiverType.RACECOURSE, ReceiverType.RACESTARTFINISH, ReceiverType.RAWPOSITIONS, ReceiverType.SENSORDATA});
        trackedRace1 = getTrackedRace();
        super.setUp();
        super.setUp("event_20110505_SailingTea", // Semifinale
                /* raceId */ "01ea3604-02ef-11e1-9efc-406186cbf87c",
                /* liveUri */ null, /* storedUri */ null,
                new ReceiverType[] { ReceiverType.MARKPASSINGS, ReceiverType.MARKPOSITIONS, ReceiverType.RACECOURSE, ReceiverType.RACESTARTFINISH, ReceiverType.RAWPOSITIONS, ReceiverType.SENSORDATA});
        trackedRace2 = getTrackedRace();
    }
        
//    @Test
//    public void testWithoutChangesInRace() {
//        trackedRace1.calculateHash();
//        int[] hash1 = trackedRace1.getHashValue();
//        trackedRace2.calculateHash();
//        int[] hash2 = trackedRace2.getHashValue();
//        
//        assertEquals("0: Hash1: " + hash1[0] + " Hash2: " + hash2[0], hash1[0], hash2[0]);
//        assertEquals("1: Hash1: " + hash1[1] + " Hash2: " + hash2[1], hash1[1], hash2[1]);
//        assertEquals("2: Hash1: " + hash1[2] + " Hash2: " + hash2[2], hash1[2], hash2[2]);
//        assertEquals("3: Hash1: " + hash1[3] + " Hash2: " + hash2[3], hash1[3], hash2[3]);
//        assertEquals("4: Hash1: " + hash1[4] + " Hash2: " + hash2[4], hash1[4], hash2[4]);
//        assertEquals("5: Hash1: " + hash1[5] + " Hash2: " + hash2[5], hash1[5], hash2[5]);
//        assertEquals("6: Hash1: " + hash1[6] + " Hash2: " + hash2[6], hash1[6], hash2[6]);
//        //assertEquals("7: Hash1: " + hash1[7] + " Hash2: " + hash2[7], hash1[7], hash2[7]);
//    }
    
//    @Test
//    public void testWithChangesInRace() {
//        trackedRace1.calculateHash();
//        int[] hash1 = trackedRace1.getHashValue();
////      Change of the race should result in a different hash
//        TimePoint epoch = new MillisecondsTimePoint(0l);
//        TimePoint now = MillisecondsTimePoint.now();
//        Map<String, Position> markPositions = new HashMap<String, Position>();
//        markPositions.put("CR Start (1)", new DegreePosition(53.562944999999985, 10.010104000000046));
//        markPositions.put("CR Start (2)", new DegreePosition(53.562944999999985, 10.010104000000046));
//        markPositions.put("Leeward mark", new DegreePosition(53.562145000000015, 10.009252));
//        markPositions.put("Luvtonne", new DegreePosition(53.560581899999995, 10.005657));
//        for (Waypoint w : getTrackedRace().getRace().getCourse().getWaypoints()) {
//            for (Mark mark : w.getMarks()) {
//                trackedRace2.getOrCreateTrack(mark).addGPSFix(new GPSFixImpl(markPositions.get(mark.getName()), epoch));
//                trackedRace2.getOrCreateTrack(mark).addGPSFix(new GPSFixImpl(markPositions.get(mark.getName()), now));
//            }
//        }
//        trackedRace2.calculateHash();
//        int[] hash2 = trackedRace2.getHashValue();
//        
//        assertNotEquals("Hash1 and Hash2 are equal", hash1, hash2);
//    }
}
