package com.sap.sailing.domain.test.markpassing;

import static org.junit.Assert.*;

import org.junit.Test;

import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.markpassingcalculation.MarkPassingCalculator;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tracking.impl.TrackedRaceStatusImpl;

public class CandidateFinderTest extends AbstractMockedRaceMarkPassingTest {

    @Test
    public void test() {
       
        trackedRace.recordFix(bob, new GPSFixMovingImpl(new DegreePosition(37.88917, -122.279581),
                new MillisecondsTimePoint(System.currentTimeMillis() - 2000), new KnotSpeedWithBearingImpl(1,
                        new DegreeBearingImpl(90))));
        trackedRace.recordFix(bob, new GPSFixMovingImpl(new DegreePosition(37.888848, -122.279769),
                new MillisecondsTimePoint(System.currentTimeMillis()), new KnotSpeedWithBearingImpl(2,
                        new DegreeBearingImpl(90))));
        MarkPassingCalculator m = new MarkPassingCalculator(trackedRace, true);
        m.toString();
        trackedRace.recordFix(bob, new GPSFixMovingImpl(new DegreePosition(37.888496,-122.279618),
                new MillisecondsTimePoint(System.currentTimeMillis()), new KnotSpeedWithBearingImpl(3,
                        new DegreeBearingImpl(90))));
        trackedRace.setStatus(new TrackedRaceStatusImpl(TrackedRaceStatusEnum.FINISHED, 100));
        while(trackedRace.getMarkPassings(bob).size()==0){
           System.currentTimeMillis();
        }
        assertEquals(1, trackedRace.getMarkPassings(bob).size());
    }
}
