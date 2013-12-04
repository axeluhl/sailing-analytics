package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.Arrays;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseListener;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;

public class MarkPassingCalculator extends AbstractRaceChangeListener implements CourseListener {
    private CandidateFinder finder;
    private CandidateChooser chooser;
    private ArrayList<Waypoint> waypoints = new ArrayList<>();
    private TrackedRace race;

    // TODO Start-analysis is wrong for gate starts
    // TODO Prepare for Polars :(
    // TODO How should Edges between the proxy start and end be treated
    // TODO Make sure the functions return the right probability
    // TODO Connection to rest of infrastructure
    // TODO Feldmann issue, also for marks
    // TODO Use Wind/Maneuver analysis
    // TODO Build good test framework that test incremental calculation, tricky cases, ...
    // TODO Document everything

    public MarkPassingCalculator(TrackedRace race) {
        
        this.race = race;
        chooser = new CandidateChooser(race);
        finder = new CandidateFinder(race.getRace().getCompetitors(), chooser);
        for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
            waypoints.add(w);
        }
        waypointsChanged();
        race.addListener(this);
    }

    public MarkPassing getMarkPass(Competitor c, Waypoint w) {
        return chooser.getMarkPass(c, w);
    }

    @Override
    public void markPositionChanged(GPSFix fix, Mark mark) {
        finder.newMarkFixes(mark, Arrays.asList(fix));
    }

    @Override
    public void competitorPositionChanged(GPSFixMoving fix, Competitor item) {
        finder.newCompetitorFixes(Arrays.asList(fix), item);
    }

    private void waypointsChanged() {
        ArrayList<TrackedLeg> legs = new ArrayList<>();
        for (TrackedLeg l : race.getTrackedLegs()) {
            legs.add(l);
        }
        finder.upDateWaypoints(waypoints, legs);
        chooser.upDateLegs(legs);
    }

    @Override
    public void waypointAdded(int zeroBasedIndex, Waypoint waypointThatGotAdded) {
        waypoints.add(zeroBasedIndex, waypointThatGotAdded);
        waypointsChanged();
    }

    @Override
    public void waypointRemoved(int zeroBasedIndex, Waypoint waypointThatGotRemoved) {
        if (waypoints.get(zeroBasedIndex) == waypointThatGotRemoved) {
            waypoints.remove(zeroBasedIndex);
        } else {
            for (Waypoint w : race.getRace().getCourse().getWaypoints())
                waypoints.add(w);
        }
        waypointsChanged();
    }
}