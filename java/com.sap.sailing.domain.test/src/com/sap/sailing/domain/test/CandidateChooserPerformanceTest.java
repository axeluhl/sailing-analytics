package com.sap.sailing.domain.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.markpassingcalculation.Candidate;
import com.sap.sailing.domain.markpassingcalculation.CandidateChooser;

public class CandidateChooserPerformanceTest extends AbstractMarkPassingTest {

    public CandidateChooserPerformanceTest() {
        super();
    }

    @Test
    public void test() {
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
