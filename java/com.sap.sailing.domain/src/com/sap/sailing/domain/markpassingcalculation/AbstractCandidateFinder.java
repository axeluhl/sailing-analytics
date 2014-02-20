package com.sap.sailing.domain.markpassingcalculation;

import java.util.LinkedHashMap;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.GPSFix;

/**
 * Converts the incoming GPSFixes of competitors and marks into {@link Candidate}s for each competitor.
 * 
 * @author Nicolas Klose
 * 
 */

public interface AbstractCandidateFinder {

    /**
     * @return The fixes of each Competitor that may have changed their status
     */
    LinkedHashMap<Competitor, List<GPSFix>> calculateFixesAffectedByNewMarkFixes(Mark mark, Iterable<GPSFix> gps);

    /**
     * @param fixes
     *            Either new fixes or fixes that may have changed their status, e.g. as a result of new {@link Mark}
     *            fixes.
     * @return new {@link Candidate}s and those that should be removed.
     */
    Pair<Iterable<Candidate>, Iterable<Candidate>> getCandidateDeltas(Competitor c, Iterable<GPSFix> fixes);

    /**
     * When initializing the calculator, the whole race until now is evaluated. For that purpose all of the
     * {@link Candidate}s are needed instead of just the deltas.
     * 
     */
    Pair<Iterable<Candidate>, Iterable<Candidate>> getAllCandidates(Competitor c);
}
