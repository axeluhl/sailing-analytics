package com.sap.sailing.domain.test.markpassing;

import java.util.List;

import com.maptrack.utils.Pair;
import com.sap.sailing.domain.tracking.GPSFix;

public interface AbstractCandidateFinder {
    
    public Pair<List<Candidate>, List<Candidate>> getCandidateDeltas(List<GPSFix> fixes);

}
