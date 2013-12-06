package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
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

public class MarkPassingCalculator {
    private CandidateFinder finder;
    private CandidateChooser chooser;
    private MarkPassingUpdateListener listener;

    public MarkPassingCalculator(TrackedRace race, boolean update) {
        finder = new CandidateFinder(race);
        chooser = new CandidateChooser(race, finder.getAllCandidates());
        if (update) {
            listener = new MarkPassingUpdateListener();
            race.addListener(listener);
            List<Pair<Object, GPSFix>> allNewFixes = new ArrayList<Pair<Object, GPSFix>>();
            while (true) {
                listener.getQueue().drainTo(allNewFixes);
                Pair<LinkedHashMap<Competitor, List<GPSFixMoving>>, LinkedHashMap<Mark, List<GPSFix>>> sortedFixes = new Pair<LinkedHashMap<Competitor, List<GPSFixMoving>>, LinkedHashMap<Mark, List<GPSFix>>>(
                        new LinkedHashMap<Competitor, List<GPSFixMoving>>(), new LinkedHashMap<Mark, List<GPSFix>>());
                for(Pair<Object, GPSFix> fix : allNewFixes){
                    
                    
                }
                
                chooser.getMarkPassDeltas(finder.getCandidateDeltas(sortedFixes));
            }
        }
    }

    public LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>> getMarkPasses() {
        return chooser.getAllMarkPasses();
    }

    public void calculateMarkPassDeltas(
            Pair<LinkedHashMap<Competitor, List<GPSFixMoving>>, LinkedHashMap<Mark, List<GPSFix>>> fixes) {
        chooser.getMarkPassDeltas(finder.getCandidateDeltas(fixes));
    }
}