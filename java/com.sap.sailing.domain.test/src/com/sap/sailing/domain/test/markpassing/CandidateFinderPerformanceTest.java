package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.markpassingcalculation.AbstractCandidateFinder;
import com.sap.sailing.domain.markpassingcalculation.CandidateFinder;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;

public class CandidateFinderPerformanceTest extends AbstractMockedRaceMarkPassingTest implements RaceChangeListener {

    public CandidateFinderPerformanceTest() {
        super();
    }

    @Test
    public void test() {
        AbstractCandidateFinder f = new CandidateFinder(trackedRace);
        List<GPSFix> fixesAdded = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            GPSFixMoving fix = rndFix();
            trackedRace.recordFix(bob, fix);
            fixesAdded.add(fix);
        }
        time = System.currentTimeMillis();
        f.calculateFixesAffectedByNewCompetitorFixes(bob, fixesAdded);
        f.getCandidateDeltas(bob);
        time = System.currentTimeMillis() - time;
        System.out.println(time);
        Assert.assertTrue(time<2000);
    }

    private GPSFixMoving rndFix() {
        DegreePosition position = new DegreePosition(37.8878 + rnd.nextDouble() * 0.0019, -122.268 - rnd.nextDouble()
                * 0.012);
        TimePoint p = new MillisecondsTimePoint(
                (long) (System.currentTimeMillis() - 300000 + (Math.random() * (7800000))));
        SpeedWithBearing speed = new KnotSpeedWithBearingImpl(rnd.nextInt(11), new DegreeBearingImpl(rnd.nextInt(360)));

        return new GPSFixMovingImpl(position, p, speed);
    }

    @Override
    public void competitorPositionChanged(GPSFixMoving fix, Competitor competitor) {
    }

    @Override
    public void markPositionChanged(GPSFix fix, Mark mark) {
    }

    @Override
    public void markPassingReceived(Competitor competitor, Map<Waypoint, MarkPassing> oldMarkPassings,
            Iterable<MarkPassing> markPassings) {
        for (MarkPassing m : markPassings) {
            System.out.println(m);
        }
    }

    @Override
    public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
    }

    @Override
    public void windDataReceived(Wind wind, WindSource windSource) {
    }

    @Override
    public void windDataRemoved(Wind wind, WindSource windSource) {
    }

    @Override
    public void windAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
    }

    @Override
    public void raceTimesChanged(TimePoint startOfTracking, TimePoint endOfTracking, TimePoint startTimeReceived) {
    }

    @Override
    public void delayToLiveChanged(long delayToLiveInMillis) {
    }

    @Override
    public void windSourcesToExcludeChanged(Iterable<? extends WindSource> windSourcesToExclude) {
    }

    @Override
    public void statusChanged(TrackedRaceStatus newStatus) {
    }

}
