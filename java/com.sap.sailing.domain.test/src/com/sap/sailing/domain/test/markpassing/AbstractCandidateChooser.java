package com.sap.sailing.domain.test.markpassing;

import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.impl.Util.Pair;

public interface AbstractCandidateChooser {
    
    public void calculateMarkPassDeltas(Competitor c, Pair<List<Candidate>, List<Candidate>> candidateDeltas);

}
