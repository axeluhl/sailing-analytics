package com.sap.sailing.domain.markpassingcalculation;

import java.util.LinkedHashMap;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sse.common.UtilNew;

/**
 * A CandidateFinder converts the incoming GPSFixes of competitors and marks into {@link Candidate}s for each
 * competitor. {@link #calculateFixesAffectedByNewMarkFixes(Mark, List)} determines the fixes that may have changed
 * their status as {@link Candidate} after new fixes for a mark arrive. This determines those fixes that may have
 * changed their status as a {@link Candidate}. {@link #getCandidateDeltas(Competitor, Iterable)} then returns the
 * actual changes to the list of Candidates as a Pair of Lists, one of the new Candidates and one of the Candidates that
 * should be removed.
 * 
 * @author Nicolas Klose
 * 
 */

public interface AbstractCandidateFinder {

    LinkedHashMap<Competitor, List<GPSFix>> calculateFixesAffectedByNewMarkFixes(Mark mark, Iterable<GPSFix> gps);

    /**
     * Before calling this method, there have to be fixes that may have changed their status as a Candidate. This is
     * done by the the method {@link #calculateFixesAffectedByNewMarkFixes(Mark, Iterable)}
     * 
     * @param c
     * @return
     */
    UtilNew.Pair<Iterable<Candidate>, Iterable<Candidate>> getCandidateDeltas(Competitor c, Iterable<GPSFix> fixes);

    /**
     * When initializing the calculator, the whole race until now is evaluated. For that purpose all of the Candidates
     * are needed instead of just the deltas.
     * 
     * @param c
     *            the competitor whose {@link Candidate}s should be returned
     * @return all of the Candidates for the specified competitor
     */
    UtilNew.Pair<Iterable<Candidate>, Iterable<Candidate>> getAllCandidates(Competitor c);
}
