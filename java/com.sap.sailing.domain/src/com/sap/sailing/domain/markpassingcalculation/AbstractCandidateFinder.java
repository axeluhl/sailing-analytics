package com.sap.sailing.domain.markpassingcalculation;

import java.util.List;

import com.sap.sailing.domain.common.impl.Util.Pair;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.tracking.GPSFix;

/**
 * A CandidateFinder converts the incoming GPSFixes of competitors and marks into {@link Candidate}s for each competitor. With
 * new Fixes of an Object one can call the {@link #calculateFixesAffectedByNewCompetitorFixes(Competitor, List)} or
 * the {@link #calculateFixesAffectedByNewMarkFixes(Mark, List)} methods. This determines those fixes that may have
 * changed their status as a {@link Candidate}. {@link #getCandidateDeltas(Competitor)} then returns the actual changes
 * to the list of Candidates as a Pair of Lists, one of the new Candidates and one of the Candidates that should be
 * removed. 
 * 
 * @author Nicolas Klose
 * 
 */

public interface AbstractCandidateFinder {

    Pair<List<Candidate>, List<Candidate>> getCandidateDeltas(Competitor c);

    /**
     * @return the {@link Competitor}s that have affected Fixes, so the other Competitors can be ignored in the following calculations.
     */
    Iterable<Competitor> getAffectedCompetitors();

    void calculateFixesAffectedByNewCompetitorFixes(Competitor c, List<GPSFix> fixes);

    void calculateFixesAffectedByNewMarkFixes(Mark mark, Iterable<GPSFix> gps);

    
    /**
     * When initializing the calculator, the whole race until now is evaluated. For that purpose all of the Candidates are needed instead of just the deltas.
     * 
     * @param c
     *          the competitor whose {@link Candidate}s should be returned
     * @return all of the Candidates for the specified competitor
     */
    Pair<List<Candidate>, List<Candidate>> getAllCandidates(Competitor c);
}
