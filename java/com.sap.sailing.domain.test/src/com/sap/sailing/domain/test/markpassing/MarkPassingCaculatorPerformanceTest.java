package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.markpassingcalculation.AbstractCandidateFinder;
import com.sap.sailing.domain.markpassingcalculation.Candidate;
import com.sap.sailing.domain.markpassingcalculation.CandidateChooser;
import com.sap.sailing.domain.markpassingcalculation.CandidateFinder;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;

public class MarkPassingCaculatorPerformanceTest extends AbstractMockedRaceMarkPassingTest {

    public MarkPassingCaculatorPerformanceTest() {
        super();
    }

    @Test
    public void testFinderPerformance() {
        AbstractCandidateFinder f = new CandidateFinder(trackedRace);
        List<GPSFix> fixesAdded = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            GPSFixMoving fix = rndFix();
            trackedRace.recordFix(bob, fix);
            fixesAdded.add(fix);
        }
        time = System.currentTimeMillis();
        f.getCandidateDeltas(bob, fixesAdded);
        time = System.currentTimeMillis() - time;
        Assert.assertTrue(time<2000);
    }

    public void markPassingReceived(Competitor competitor, Map<Waypoint, MarkPassing> oldMarkPassings,
            Iterable<MarkPassing> markPassings) {
        for (MarkPassing m : markPassings) {
            System.out.println(m);
        }
    }
    
    @Test
    public void testChooserPerformance() {
        testAddingCandidatesToChooser(200, 1);
    }

    private void testAddingCandidatesToChooser(int numberOfCandidates, int numberToAdd) {
        ArrayList<Candidate> newCans = new ArrayList<>();
        for (int i = 0; i < numberOfCandidates; i++) {
            newCans.add(randomCan());
        }
        time = System.currentTimeMillis();
        CandidateChooser c = new CandidateChooser(trackedRace);
        c.calculateMarkPassDeltas(bob, new Pair<List<Candidate>, List<Candidate>>(newCans, new ArrayList<Candidate>()));
        time = System.currentTimeMillis() - time;

        ArrayList<Long> times = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            ArrayList<Candidate> old = new ArrayList<>();
            ArrayList<Candidate> ne = new ArrayList<>();
            for (int j = 0; j < numberToAdd; j++) {
                ne.add(randomCan());
            }
            time = System.currentTimeMillis();
            c.calculateMarkPassDeltas(bob, new Pair<List<Candidate>, List<Candidate>>(ne, old));
            times.add(System.currentTimeMillis() - time);
            old = ne;
        }
        long total = 0;
        for (long l : times) {
            total = total + l;
        }
        Assert.assertTrue(total / times.size()<500);
    }

    private Candidate randomCan() {
        int id = rnd.nextInt(5);
        return new Candidate(id + 1, new MillisecondsTimePoint(
                (long) (System.currentTimeMillis() - 300000 + (Math.random() * (7800000)))), 0.5 + 0.5 * Math.random(),
                waypoints.get(id));
    }

}
