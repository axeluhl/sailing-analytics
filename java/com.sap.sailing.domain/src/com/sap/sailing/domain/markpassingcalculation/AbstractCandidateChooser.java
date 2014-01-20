package com.sap.sailing.domain.markpassingcalculation;

import java.util.LinkedHashMap;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.MarkPassing;

/**
 * Takes the CandidateDeltas from a {@link AbstractCandidateFinder} and calculates the most likely sequence of
 * MarkPassings. The race should then be notified about these new MarkPassings.
 * 
 * @author Nicolas Klose
 * 
 */

public interface AbstractCandidateChooser {

    public void calculateMarkPassDeltas(Competitor c, Pair<List<Candidate>, List<Candidate>> candidateDeltas);

    public LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>> getAllPasses();
}
