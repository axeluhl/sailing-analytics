package com.sap.sailing.domain.markpassingcalculation;

import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

/**
 * Converts the incoming GPSFixes of competitors and marks into {@link Candidate}s for each competitor.
 * 
 * @author Nicolas Klose
 */
public interface CandidateFinder {

    

    /**
     * @param fixes
     *            Either new fixes or fixes that may have changed their status, e.g. as a result of new mark
     *            fixes.
     * @return new {@link Candidate}s and those that should be removed.
     */
    Util.Pair<Iterable<Candidate>, Iterable<Candidate>> getCandidateDeltas(Competitor c, Iterable<GPSFix> fixes);

    /**
     * When initializing or refreshing the calculator, the whole race until now is evaluated. For that purpose all of the
     * {@link Candidate}s are needed instead of just the deltas.
     */
    Util.Pair<Iterable<Candidate>, Iterable<Candidate>> getAllCandidates(Competitor c);
    
    Map<Competitor, Util.Pair<List<Candidate>, List<Candidate>>> updateWaypoints(Iterable<Waypoint> addedWaypoints, Iterable<Waypoint> removedWaypoints, Integer smallestIndex);

    /**
     * @return The fixes for each Competitor that may have changed their status as a {@link Candidate} because of new
     *         mark fixes..
     */
    Map<Competitor, List<GPSFix>> calculateFixesAffectedByNewMarkFixes(Map<Mark, List<GPSFix>> newMarkFixes);

}
