package com.sap.sailing.domain.test.markpassing;

import java.util.LinkedHashMap;
import java.util.List;

import com.maptrack.utils.Pair;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.tracking.GPSFix;

/**
 * A CandidateFinder converts the incoming GPSFixes of competitors and marks into Candidates for each competitor.
 * @author Nicolas Klose
 *
 */

public interface AbstractCandidateFinder {

    public LinkedHashMap<Competitor, List<Candidate>> getAllCandidates();

    public LinkedHashMap<Competitor, Pair<List<Candidate>, List<Candidate>>> getCandidateDeltas(
            Pair<LinkedHashMap<Competitor, List<GPSFix>>, LinkedHashMap<Mark, List<GPSFix>>> fixes);
}
