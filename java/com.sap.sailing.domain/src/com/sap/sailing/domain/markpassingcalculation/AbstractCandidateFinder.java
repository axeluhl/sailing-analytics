package com.sap.sailing.domain.markpassingcalculation;

import java.util.List;

import com.sap.sailing.domain.common.impl.Util.Pair;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.tracking.GPSFix;

/**
 * A CandidateFinder converts the incoming GPSFixes of competitors and marks into Candidates for each competitor.
 * 
 * @author Nicolas Klose
 * 
 */

public interface AbstractCandidateFinder {

    Pair<List<Candidate>, List<Candidate>> getCandidateDeltas(Competitor c);

    Iterable<Competitor> getAffectedCompetitors();

    void calculateFixesAffectedByNewCompetitorFixes(Competitor c, List<GPSFix> fixes);

    void calculateFixesAffectedByNewMarkFixes(Mark mark, Iterable<GPSFix> gps);

    Pair<List<Candidate>, List<Candidate>> getAllCandidates(Competitor c);
}
