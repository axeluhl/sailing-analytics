package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPointWithTwoMarks;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.ControlPointWithTwoMarksImpl;
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.markpassingcalculation.Candidate;
import com.sap.sailing.domain.markpassingcalculation.CandidateChooser;
import com.sap.sailing.domain.tracking.impl.GPSFixImpl;

public class CandidateChooserPerformanceTest extends AbstractMockedRaceMarkPassingTest {
    Mark m = new MarkImpl("black");
    Mark m2 = new MarkImpl("white 1");
    Mark m3 = new MarkImpl("white 2");

    ControlPointWithTwoMarks cp = new ControlPointWithTwoMarksImpl(m2, m3, "cp");
    Waypoint w1 = new WaypointImpl(cp, PassingInstruction.Line);
    Waypoint w2 = new WaypointImpl(m, PassingInstruction.Port);
    Waypoint w3 = new WaypointImpl(cp, PassingInstruction.Gate);
    Waypoint w4 = new WaypointImpl(m, PassingInstruction.Port);
    Waypoint w5 = new WaypointImpl(cp, PassingInstruction.Line);

    Competitor bob = new CompetitorImpl("Bob", "Bob", null, null, null);
    Competitor joe = new CompetitorImpl("Joe", "Joe", null, null, null);
    Competitor mike = new CompetitorImpl("Mike", "Mike", null, null, null);

    public CandidateChooserPerformanceTest() {
        
        setUp(Arrays.asList(w1, w2, w3, w4, w5), Arrays.asList(new CompetitorImpl("Bob", "Bob", null, null, null)));
    
        getRace().setStartTimeReceived(new MillisecondsTimePoint(System.currentTimeMillis() - 120000));
        getRace().recordFix(m, new GPSFixImpl(new DegreePosition(37.888796, -122.279602), new MillisecondsTimePoint(
            System.currentTimeMillis())));
        getRace().recordFix(m2, new GPSFixImpl(new DegreePosition(37.889653, -122.268991), new MillisecondsTimePoint(
            System.currentTimeMillis())));
        getRace().recordFix(m3, new GPSFixImpl(new DegreePosition(37.887936, -122.268841), new MillisecondsTimePoint(
            System.currentTimeMillis())));}

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
        CandidateChooser c = new CandidateChooser(getRace());
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
        Assert.assertTrue(total / times.size() < 500);
    }

    private Candidate randomCan() {
        int id = rnd.nextInt(5);
        return new Candidate(id + 1, new MillisecondsTimePoint(
                (long) (System.currentTimeMillis() - 300000 + (Math.random() * (7800000)))), 0.5 + 0.5 * Math.random(),
                waypoints.get(id));
    }
}
