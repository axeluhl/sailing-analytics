package com.sap.sailing.domain.test.markpassing;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.markpassingcalculation.CandidateFinder;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class CandidateFinderTest extends AbstractMockedRaceMarkPassingTest {

    @Test
    public void test() {
        GPSFixMoving fix1 = new GPSFixMovingImpl(new DegreePosition(0.000003,0.000049),
                new MillisecondsTimePoint(System.currentTimeMillis() - 2000), new KnotSpeedWithBearingImpl(5,
                        new DegreeBearingImpl(330)));
        GPSFixMoving fix2 = new GPSFixMovingImpl(new DegreePosition(0.000062,0.000029),
                new MillisecondsTimePoint(System.currentTimeMillis()), new KnotSpeedWithBearingImpl(5,
                        new DegreeBearingImpl(270)));
        GPSFixMoving fix3 = new GPSFixMovingImpl(new DegreePosition(0.000026,-0.000024),
                new MillisecondsTimePoint(System.currentTimeMillis() + 2000), new KnotSpeedWithBearingImpl(5,
                        new DegreeBearingImpl(225)));
        GPSFixMoving fix4 = new GPSFixMovingImpl(new DegreePosition(-0.000056,-0.000049),
                new MillisecondsTimePoint(System.currentTimeMillis() + 4000), new KnotSpeedWithBearingImpl(5,
                        new DegreeBearingImpl(190)));
        trackedRace.recordFix(bob, fix1);
        trackedRace.recordFix(bob, fix2);
        CandidateFinder finder = new CandidateFinder(trackedRace);
        List<GPSFix> fixes = new ArrayList<GPSFix>();
        fixes.add(fix1);
        fixes.add(fix2);
        assertEquals(0, Util.size(finder.getCandidateDeltas(bob, fixes).getA()));
        
        trackedRace.recordFix(bob, fix3);
        fixes.clear();
        fixes.add(fix3);
        assertEquals(1, Util.size(finder.getCandidateDeltas(bob, fixes).getA())); // CTE candidate
        
        trackedRace.recordFix(bob, fix4);
        fixes.clear();
        fixes.add(fix4);
        assertEquals(1, Util.size(finder.getCandidateDeltas(bob, fixes).getA())); // Distance Candidate
    }
}
