package com.sap.sailing.domain.test.markpassing;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;

import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.markpassingcalculation.CandidateFinder;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;

public class CandidateFinderTest extends AbstractMockedRaceMarkPassingTest {

    @Ignore
    // TODO And Test is wrong with new linear interpolation
    public void test() {
       
        GPSFixMoving fix1 = new GPSFixMovingImpl(new DegreePosition(37.88917, -122.279581),
                new MillisecondsTimePoint(System.currentTimeMillis() - 2000), new KnotSpeedWithBearingImpl(1,
                        new DegreeBearingImpl(90)));
        GPSFixMoving fix2 = new GPSFixMovingImpl(new DegreePosition(37.888848, -122.279769),
                new MillisecondsTimePoint(System.currentTimeMillis()), new KnotSpeedWithBearingImpl(2,
                        new DegreeBearingImpl(90)));
        GPSFixMoving fix3 = new GPSFixMovingImpl(new DegreePosition(37.888496,-122.279618),
                new MillisecondsTimePoint(System.currentTimeMillis() + 2000), new KnotSpeedWithBearingImpl(3,
                        new DegreeBearingImpl(90)));
        
        trackedRace.recordFix(bob, fix1);
        trackedRace.recordFix(bob, fix2);
        CandidateFinder finder = new CandidateFinder(trackedRace);
        List<GPSFix> fixes = new ArrayList<GPSFix>();
        fixes.add(fix1);
        fixes.add(fix2);
        
        assertEquals(0, finder.getCandidateDeltas(bob, fixes).getA().size());
        trackedRace.recordFix(bob, fix3);
        fixes.clear();
        fixes.add(fix3);
        assertEquals(2, finder.getCandidateDeltas(bob, fixes).getA().size());
    }
}
