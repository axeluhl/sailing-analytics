package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;

public interface AbstractCandidateChooser {
    
     public LinkedHashMap<Integer, TimePoint> getMarkPasses(ArrayList<Candidate> candidates,  Candidate start, Candidate end, LinkedHashMap<Waypoint, ArrayList<LinkedHashMap<TimePoint, Position>>> markPostions, ArrayList<String> legs);

}
