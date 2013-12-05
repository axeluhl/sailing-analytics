package com.sap.sailing.domain.test.markpassing;

import java.util.LinkedHashMap;
import java.util.List;

import com.maptrack.utils.Pair;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;

public class MarkPassingCalculator  {
    private CandidateFinder finder;
    private CandidateChooser chooser;

    public MarkPassingCalculator(TrackedRace race) {
        finder = new CandidateFinder(race);
        chooser = new CandidateChooser(race, finder.getAllCandidates());
    }

    public LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>> getMarkPasses() {
        return chooser.getAllMarkPasses();
    }
    
    public void calculateMarkPassDeltas(Pair<LinkedHashMap<Competitor, List<GPSFixMoving>>, LinkedHashMap<Mark, List<GPSFix>>> fixes){
        chooser.getMarkPassDeltas(finder.getCandidateDeltas(fixes));
    }
}