package com.sap.sailing.domain.test.markpassing;

import java.util.LinkedHashMap;
import java.util.List;

import com.maptrack.utils.Pair;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.GPSFix;

public interface AbstractCandidateFinder {
    
    public LinkedHashMap<Competitor, List<Candidate>> getAllCandidates();
    
    public Pair<List<Candidate>, List<Candidate>> getCandidateDeltas(List<GPSFix> fixes);
}
