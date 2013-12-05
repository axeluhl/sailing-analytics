package com.sap.sailing.domain.test.markpassing;

import java.util.LinkedHashMap;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;

public class MarkPassingCalculator  {
    private CandidateFinder finder;
    private CandidateChooser chooser;

    // TODO Start-analysis is wrong for gate starts
    // TODO Prepare for Polars :(
    // TODO How should Edges between the proxy start and end be treated
    // TODO Make sure the functions return the right probability
    // TODO Feldmann issue, also for marks
    // TODO Use Wind/Maneuver analysis
    // TODO Build good test framework that test incremental calculation, tricky cases, ...
    // TODO Document everything

    public MarkPassingCalculator(TrackedRace race) {
        finder = new CandidateFinder(race);
        chooser = new CandidateChooser(race, finder.getAllCandidates());
    }

    public LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>> getMarkPasses() {
        return chooser.getAllMarkPasses();
    }
}