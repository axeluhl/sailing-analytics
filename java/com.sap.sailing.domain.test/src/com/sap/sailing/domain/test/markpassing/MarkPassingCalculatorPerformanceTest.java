package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPointWithTwoMarks;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.ControlPointWithTwoMarksImpl;
import com.sap.sailing.domain.base.impl.CourseAreaImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.impl.HighPoint;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.tracking.impl.TrackedRegattaImpl;

public class MarkPassingCalculatorPerformanceTest {

    private Competitor bob = new CompetitorImpl("Bob", "Bob", null, null, null);
    private Mark m = new MarkImpl("black");
    private Mark m2 = new MarkImpl("white 1");
    private Mark m3 = new MarkImpl("white 2");

    private DynamicTrackedRace trackedRace;
    private long time;

    public MarkPassingCalculatorPerformanceTest() {
        ControlPointWithTwoMarks cp = new ControlPointWithTwoMarksImpl(m2, m3, "cp");
        Waypoint w1 = new WaypointImpl(cp, PassingInstruction.Line);
        Waypoint w2 = new WaypointImpl(m, PassingInstruction.Port);
        Waypoint w3 = new WaypointImpl(cp, PassingInstruction.Gate);
        Waypoint w4 = new WaypointImpl(m, PassingInstruction.Port);
        Waypoint w5 = new WaypointImpl(cp, PassingInstruction.Line);
        Regatta r = new RegattaImpl("regatta", new BoatClassImpl("boat", true), Arrays.asList(new SeriesImpl("Series",
                true, Arrays.asList(new FleetImpl("fleet")), new ArrayList<String>(), null)), true, new HighPoint(),
                "ID", new CourseAreaImpl("area", new UUID(5, 5)));
        Course course = new CourseImpl("course", Arrays.asList(w1, w2, w3, w4, w5));
        RaceDefinition race = new RaceDefinitionImpl("Performance Race", course, new BoatClassImpl("boat", true),
                Arrays.asList(bob));
        trackedRace = new DynamicTrackedRaceImpl(new TrackedRegattaImpl(r), race, new ArrayList<Sideline>(),
                new EmptyWindStore(), 0, 10000, 10000);
        trackedRace.setStartTimeReceived(new MillisecondsTimePoint(System.currentTimeMillis() - 120000));
        trackedRace.recordFix(m, new GPSFixImpl(new DegreePosition(37.888796, -122.279602), new MillisecondsTimePoint(
                System.currentTimeMillis())));
        trackedRace.recordFix(m2, new GPSFixImpl(new DegreePosition(37.889653, -122.268991), new MillisecondsTimePoint(
                System.currentTimeMillis())));
        trackedRace.recordFix(m3, new GPSFixImpl(new DegreePosition(37.887936, -122.268841), new MillisecondsTimePoint(
                System.currentTimeMillis())));
    }

    @Before
    public void clearMarkPasses() {

    }

    @Test
    public void testTimeOfAddingManyCandidatesToChooser() {
        int numberOfCandidates = 500;
        List<Waypoint> waypoints = new ArrayList<>();
        for (Waypoint w : trackedRace.getRace().getCourse().getWaypoints()) {
            waypoints.add(w);
        }
        ArrayList<Candidate> newCans = new ArrayList<>();
        int id = 0;
        for (int i = 0; i < numberOfCandidates; i++) {
            newCans.add(new Candidate(id + 1, new MillisecondsTimePoint(
                    (long) (System.currentTimeMillis() - 300000 + (Math.random() * (7800000)))), 0.5 + 0.5 * Math
                    .random(), waypoints.get(id)));
            if (id == 4) {
                id = 0;
            } else {
                id++;
            }
        }
        time = System.currentTimeMillis();
        new CandidateChooser(trackedRace).calculateMarkPassDeltas(bob, new Pair<List<Candidate>, List<Candidate>>(
                newCans, new ArrayList<Candidate>()));
        System.out.println(System.currentTimeMillis() - time);
    }

    @Test
    public void testTimeOfAddingOneCandidateToChooser() {
        int numberOfCandidatesInChooser = 500;

        

            List<Waypoint> waypoints = new ArrayList<>();
            for (Waypoint w : trackedRace.getRace().getCourse().getWaypoints()) {
                waypoints.add(w);
            }
            ArrayList<Candidate> newCans = new ArrayList<>();
            int id = 0;
            for (int i = 0; i < numberOfCandidatesInChooser; i++) {
                newCans.add(new Candidate(id + 1, new MillisecondsTimePoint(
                        (long) (System.currentTimeMillis() - 300000 + (Math.random() * (7800000)))), 0.5 + 0.5 * Math
                        .random(), waypoints.get(id)));
                if (id == 4) {
                    id = 0;
                } else {
                    id++;
                }
            }

            CandidateChooser c = new CandidateChooser(trackedRace);
            c.calculateMarkPassDeltas(bob, new Pair<List<Candidate>, List<Candidate>>(newCans,
                    new ArrayList<Candidate>()));
            while (true) {
            newCans.clear(); //Remove old Candidate!!
            newCans.add(new Candidate(id + 1, new MillisecondsTimePoint(
                    (long) (System.currentTimeMillis() - 300000 + (Math.random() * (7800000)))), 0.5 + 0.5 * Math
                    .random(), waypoints.get(id)));

            time = System.currentTimeMillis();
            c.calculateMarkPassDeltas(bob, new Pair<List<Candidate>, List<Candidate>>(newCans,
                    new ArrayList<Candidate>()));
            System.out.println(System.currentTimeMillis() - time);
        }
    }
}
