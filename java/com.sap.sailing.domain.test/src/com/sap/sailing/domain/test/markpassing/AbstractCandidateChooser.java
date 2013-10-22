package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;

import com.sap.sailing.domain.common.TimePoint;

public interface AbstractCandidateChooser {
    
     public ArrayList<TimePoint> getMarkPasses(ArrayList<Candidate> candidates,  Candidate start, Candidate end);

}
