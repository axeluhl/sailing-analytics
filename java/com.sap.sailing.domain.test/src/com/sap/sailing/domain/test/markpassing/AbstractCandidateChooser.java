package com.sap.sailing.domain.test.markpassing;

import java.util.LinkedHashMap;
import java.util.List;

import com.maptrack.utils.Pair;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.MarkPassing;

public interface AbstractCandidateChooser {
    
    public LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>> getAllMarkPasses();
    
    public void calculateMarkPassDeltas(Competitor c, Pair<List<Candidate>, List<Candidate>> candidateDeltas);

}
