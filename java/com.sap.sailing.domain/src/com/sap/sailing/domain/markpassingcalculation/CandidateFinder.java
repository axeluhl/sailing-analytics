package com.sap.sailing.domain.markpassingcalculation;

import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

/**
 * Converts the incoming GPSFixes of competitors and marks into {@link Candidate}s for each competitor.
 * 
 * @author Nicolas Klose
 * @author Axel Uhl (d043530)
 */
public interface CandidateFinder {
    /**
     * @param fixes
     *            Either new fixes or fixes that may have changed their status, e.g. as a result of new mark
     *            fixes.
     * @return new {@link Candidate}s and those that should be removed.
     */
    Util.Pair<Iterable<Candidate>, Iterable<Candidate>> getCandidateDeltas(Competitor c, Iterable<GPSFixMoving> fixes);

    /**
     * When initializing or refreshing the calculator, the whole race until now is evaluated. For that purpose all of the
     * {@link Candidate}s are needed instead of just the deltas.
     * 
     * @return new {@link Candidate}s and those that should be removed
     */
    Util.Pair<Iterable<Candidate>, Iterable<Candidate>> getAllCandidates(Competitor c);
    
    Map<Competitor, Util.Pair<List<Candidate>, List<Candidate>>> updateWaypoints(Iterable<Waypoint> addedWaypoints, Iterable<Waypoint> removedWaypoints, int smallestIndex);

    /**
     * @return The fixes for each Competitor that may have changed their status as a {@link Candidate} because of new
     *         mark fixes..
     */
    Map<Competitor, List<GPSFixMoving>> calculateFixesAffectedByNewMarkFixes(Map<Mark, List<GPSFix>> newMarkFixes);

    /**
     * Notifies this finder about the race's start time having changed. The finder is only interested in the non-inferred
     * start time and double-checks whether this non-inferred start time has changed. If so, the candidates collections
     * are adjusted accordingly: new candidates may be added if the start time was moved towards the past and therefore
     * extends the time range for candidates; candidates are removed if the start time was moved to a later point in time
     * and the candidate is no longer in the time range valid for mark passings. The candidates added and removed are
     * returned in a map keyed by the competitors. The {@link Pair#getA() first} element of the value pair has the
     * candidates added for this competitor, the {@link Pair#getB() second} element has the candidates removed.
     */
    Map<Competitor, Pair<Iterable<Candidate>, Iterable<Candidate>>> getCandidateDeltasAfterRaceStartTimeChange();

    /**
     * Notifies this finder about the race's start of tracking time having changed. In case of a race that infers
     * the race start time from the start mark passings, a change in start of tracking needs to adjust the time range
     * in which candidates are considered valid.
     */
    Map<Competitor, Pair<Iterable<Candidate>, Iterable<Candidate>>> getCandidateDeltasAfterStartOfTrackingChange();
    
    /**
     * Notifies this finder about the race's finished time having changed. The candidates collections are adjusted
     * accordingly: new candidates may be added if the finished time was moved to a later point in time and therefore
     * extends the time range for candidates; candidates are removed if the finished time was moved to an earlier point
     * in time and the candidate is no longer in the time range valid for mark passings. The candidates added and
     * removed are returned in a map keyed by the competitors. The {@link Pair#getA() first} element of the value pair
     * has the candidates added for this competitor, the {@link Pair#getB() second} element has the candidates removed.
     */
    Map<Competitor, Pair<Iterable<Candidate>, Iterable<Candidate>>> getCandidateDeltasAfterRaceFinishedTimeChange(TimePoint oldFinishedTime, TimePoint newFinishedTime);
}
