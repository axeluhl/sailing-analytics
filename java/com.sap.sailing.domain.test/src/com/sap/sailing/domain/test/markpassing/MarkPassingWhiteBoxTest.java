package com.sap.sailing.domain.test.markpassing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;

import org.junit.Test;

import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.markpassingcalculation.Candidate;
import com.sap.sailing.domain.markpassingcalculation.CandidateChooser;
import com.sap.sailing.domain.markpassingcalculation.CandidateFinder;
import com.sap.sailing.domain.markpassingcalculation.impl.CandidateChooserImpl;
import com.sap.sailing.domain.markpassingcalculation.impl.CandidateFinderImpl;
import com.sap.sailing.domain.markpassingcalculation.impl.CandidateImpl;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;

/**
 * Tests tricky situation that can fail easily. Created with help of http://itouchmap.com/latlong.html
 * 
 */
public class MarkPassingWhiteBoxTest extends AbstractMockedRaceMarkPassingTest {

    @Test
    public void testNormalPassing() {
        // Normal Passing of Single mark
        GPSFixMoving fix1 = new GPSFixMovingImpl(new DegreePosition(0.000003, 0.000049), new MillisecondsTimePoint(40000), new KnotSpeedWithBearingImpl(5, new DegreeBearingImpl(
                330)));
        GPSFixMoving fix2 = new GPSFixMovingImpl(new DegreePosition(0.000062, 0.000029), new MillisecondsTimePoint(44000), new KnotSpeedWithBearingImpl(5, new DegreeBearingImpl(
                270)));
        GPSFixMoving fix3 = new GPSFixMovingImpl(new DegreePosition(0.000026, -0.000024), new MillisecondsTimePoint(47000), new KnotSpeedWithBearingImpl(5, new DegreeBearingImpl(
                225)));
        GPSFixMoving fix4 = new GPSFixMovingImpl(new DegreePosition(-0.000056, -0.000049), new MillisecondsTimePoint(50000), new KnotSpeedWithBearingImpl(5, new DegreeBearingImpl(
                190)));
        race.recordFix(ron, fix1);
        race.recordFix(ron, fix2);
        CandidateFinderImpl finder = new CandidateFinderImpl(race);
        List<GPSFix> fixes = new ArrayList<GPSFix>();
        fixes.add(fix1);
        fixes.add(fix2);
        Pair<Iterable<Candidate>, Iterable<Candidate>> candidateDeltas = finder.getCandidateDeltas(ron, fixes);
        assertEquals(0, Util.size(candidateDeltas.getA()));

        race.recordFix(ron, fix3);
        fixes.clear();
        fixes.add(fix3);
        candidateDeltas = finder.getCandidateDeltas(ron, fixes);
        assertEquals(2, Util.size(candidateDeltas.getA())); // CTE candidate

        race.recordFix(ron, fix4);
        fixes.clear();
        fixes.add(fix4);
        candidateDeltas = finder.getCandidateDeltas(ron, fixes);
        assertEquals(2, Util.size(candidateDeltas.getA())); // Distance Candidate
    }

    @Test
    public void testDistance() {
        // 3 fixes all on one side of crossing Bearing, therefore no XTE-Candidate
        GPSFixMoving fix1 = new GPSFixMovingImpl(new DegreePosition(-0.000155, 0.000103), new MillisecondsTimePoint(60000), new KnotSpeedWithBearingImpl(5, new DegreeBearingImpl(
                330)));
        GPSFixMoving fix2 = new GPSFixMovingImpl(new DegreePosition(0.000038, 0.000021), new MillisecondsTimePoint(65000), new KnotSpeedWithBearingImpl(4, new DegreeBearingImpl(
                280)));
        GPSFixMoving fix3 = new GPSFixMovingImpl(new DegreePosition(-0.000268, 0.000135), new MillisecondsTimePoint(80000), new KnotSpeedWithBearingImpl(5, new DegreeBearingImpl(
                170)));
        CandidateFinderImpl finder = new CandidateFinderImpl(race);
        List<GPSFix> fixes = new ArrayList<>();
        fixes.addAll(Arrays.asList(fix1, fix3));
        Pair<Iterable<Candidate>, Iterable<Candidate>> cans = finder.getCandidateDeltas(tom, fixes);
        assertEquals(Util.size(cans.getA()), 0);
        assertEquals(Util.size(cans.getB()), 0);

        fixes.clear();
        fixes.add(fix2);
        cans = finder.getCandidateDeltas(tom, fixes);
        assertEquals(Util.size(cans.getA()), 2); // 2 Distance candidates (mark is rounded twice in course)
        Double probability = cans.getA().iterator().next().getProbability();
        assertTrue(probability > 0.5 && probability < 0.8); // Close but distance candidate
        assertEquals(Util.size(cans.getB()), 0);
    }

    @Test
    public void testPastGate() {
        // Competitor sails closely past one mark of gate and rounds the other mark further away
        GPSFixMoving fix1 = new GPSFixMovingImpl(new DegreePosition(-0.000967, -0.000124), new MillisecondsTimePoint(100000), new KnotSpeedWithBearingImpl(5,
                new DegreeBearingImpl(100)));
        GPSFixMoving fix2 = new GPSFixMovingImpl(new DegreePosition(-0.000989, -0.000001), new MillisecondsTimePoint(110000), new KnotSpeedWithBearingImpl(5,
                new DegreeBearingImpl(160)));
        GPSFixMoving fix3 = new GPSFixMovingImpl(new DegreePosition(-0.001079, 0.000045), new MillisecondsTimePoint(115000), new KnotSpeedWithBearingImpl(5, new DegreeBearingImpl(
                90)));
        GPSFixMoving fix4 = new GPSFixMovingImpl(new DegreePosition(-0.000982, 0.000143), new MillisecondsTimePoint(125000), new KnotSpeedWithBearingImpl(5, new DegreeBearingImpl(
                45)));
        CandidateFinderImpl finder = new CandidateFinderImpl(race);
        race.recordFix(tom, fix1);
        race.recordFix(tom, fix2);
        List<GPSFix> fixes = new ArrayList<>();
        fixes.addAll(Arrays.asList(fix1, fix2));
        Pair<Iterable<Candidate>, Iterable<Candidate>> cans = finder.getCandidateDeltas(tom, fixes);
        Double inFront = cans.getA().iterator().next().getProbability();
        assertTrue(Util.size(cans.getA()) == 1);
        // Passing of one mark, close but wrong side and direction
        assertEquals(Util.size(cans.getB()), 0);

        race.recordFix(tom, fix3);
        fixes.clear();
        fixes.add(fix3);
        cans = finder.getCandidateDeltas(tom, fixes);
        assertEquals(3, Util.size(cans.getA()));
        assertEquals(Util.size(cans.getB()), 0);

        race.recordFix(tom, fix4);
        fixes.clear();
        fixes.add(fix4);
        cans = finder.getCandidateDeltas(tom, fixes);
        assertEquals(Util.size(cans.getB()), 0);
        for (Candidate c : cans.getA()) {
            if (c.getOneBasedIndexOfWaypoint() == 3) {
                assertTrue(c.getProbability() > inFront);
                // Passing of correct mark with greater distance
            }
        }
    }

    @Test
    public void testGateAfterLine() {
        // The problem of passing a gate right after the start line is crossed, solved by the distance estimation
        GPSFixMoving fix1 = new GPSFixMovingImpl(new DegreePosition(-0.001051, -0.000008), new MillisecondsTimePoint(10000), new KnotSpeedWithBearingImpl(5, new DegreeBearingImpl(
                5)));
        GPSFixMoving fix2 = new GPSFixMovingImpl(new DegreePosition(-0.000942, 0.000022), new MillisecondsTimePoint(14000), new KnotSpeedWithBearingImpl(5, new DegreeBearingImpl(
                15)));
        race.recordFix(tom, fix1);
        race.recordFix(tom, fix2);
        Candidate c1 = new CandidateImpl(1, new MillisecondsTimePoint(11000), 0.99, waypoints.get(0));
        Candidate c3 = new CandidateImpl(3, new MillisecondsTimePoint(13000), 0.99, waypoints.get(2));
        // Good candidate but bad time
        CandidateChooser chooser = new CandidateChooserImpl(race);
        chooser.calculateMarkPassDeltas(tom, Arrays.asList(c1, c3), new ArrayList<Candidate>());
        NavigableSet<MarkPassing> markPassings = race.getMarkPassings(tom);
        assertEquals(markPassings.size(), 1);
        assertEquals(markPassings.first().getWaypoint(), waypoints.get(0));
    }

    @Test
    public void testVerySlowCompetitor() {
        // TODO
    }

    @Test
    public void testSailingInFrontAndAroundMark() {
        CandidateFinder finder = new CandidateFinderImpl(race);
        GPSFixMoving fix1 = new GPSFixMovingImpl(new DegreePosition(-0.000037, -0.000126), new MillisecondsTimePoint(40000), new KnotSpeedWithBearingImpl(5, new DegreeBearingImpl(
                80)));
        GPSFixMoving fix2 = new GPSFixMovingImpl(new DegreePosition(-0.000022, 0.000001), new MillisecondsTimePoint(43000), new KnotSpeedWithBearingImpl(5, new DegreeBearingImpl(
                45)));
        GPSFixMoving fix3 = new GPSFixMovingImpl(new DegreePosition(0.000018, 0.000038), new MillisecondsTimePoint(46000), new KnotSpeedWithBearingImpl(5, new DegreeBearingImpl(
                330)));
        GPSFixMoving fix4 = new GPSFixMovingImpl(new DegreePosition(0.000084, -0.000004), new MillisecondsTimePoint(49000), new KnotSpeedWithBearingImpl(5, new DegreeBearingImpl(
                260)));
        GPSFixMoving fix5 = new GPSFixMovingImpl(new DegreePosition(0.000054, -0.000153), new MillisecondsTimePoint(52000), new KnotSpeedWithBearingImpl(5, new DegreeBearingImpl(
                200)));
        race.recordFix(ben, fix1);
        race.recordFix(ben, fix2);
        race.recordFix(ben, fix3);
        List<GPSFix> fixes = new ArrayList<>();
        fixes.addAll(Arrays.asList(fix1, fix2, fix3));
        Pair<Iterable<Candidate>, Iterable<Candidate>> cans = finder.getCandidateDeltas(ben, fixes);
        Candidate inFront = Util.get(cans.getA(), 0);
        race.recordFix(ben, fix4);
        race.recordFix(ben, fix5);
        fixes.clear();
        fixes.addAll(Arrays.asList(fix4, fix5));
        cans = finder.getCandidateDeltas(ben, fixes);
        Candidate behind = Util.get(cans.getA(), 0);
        assertTrue(behind.getProbability() > inFront.getProbability());
    }
}
