package com.sap.sailing.domain.test.markpassing;

import java.util.List;

import com.maptrack.utils.Pair;
import com.sap.sailing.domain.base.Competitor;

public interface AbstractCandidateChooser {
    
    public void calculateMarkPassDeltas(Competitor c, Pair<List<Candidate>, List<Candidate>> candidateDeltas);

}
