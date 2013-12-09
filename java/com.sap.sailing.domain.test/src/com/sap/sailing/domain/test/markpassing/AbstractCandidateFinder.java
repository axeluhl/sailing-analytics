package com.sap.sailing.domain.test.markpassing;

import java.util.LinkedHashMap;
import java.util.List;

import com.maptrack.utils.Pair;
import com.sap.sailing.domain.base.Competitor;

/**
 * A CandidateFinder converts the incoming GPSFixes of competitors and marks into Candidates for each competitor.
 * 
 * @author Nicolas Klose
 * 
 */

public interface AbstractCandidateFinder {

    public LinkedHashMap<Competitor, List<Candidate>> getAllCandidates();

    public Pair<List<Candidate>, List<Candidate>> getCandidateDeltas(Competitor c);
}
