package com.sap.sailing.domain.markpassingcalculation.impl;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Bounds;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.markpassingcalculation.Candidate;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util.Pair;

/**
 * A sequence of {@link Candidate}s such that the track of smoothed GPS fixes between them fits into a bounding box that
 * has a maximum {@link Bounds#getDiameter() diameter} of {@link #CANDIDATE_FILTER_DISTANCE}. Candidates less than
 * {@link #CANDIDATE_FILTER_TIME_WINDOW} away from one of the ends of this sequence are considered valid and are returned
 * from {@link #getValidCandidates()}.<p>
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class StationarySequence {
    /**
     * If we identify several consecutive candidates that all lie in a bounding box with a {@link Bounds#getDiameter()
     * diameter} less than or equal to this distance, only the first and the last of those candidates pass the filter.
     */
    private static final Distance CANDIDATE_FILTER_DISTANCE = new MeterDistance(20);

    private static final Duration CANDIDATE_FILTER_TIME_WINDOW = Duration.ONE_SECOND.times(10);
    
    private final SortedSet<Candidate> candidates;

    private final GPSFixTrack<Competitor, GPSFixMoving> track;
    
    private Bounds boundingBoxOfTrackSpanningCandidates;

    StationarySequence(Comparator<Candidate> candidateComparator, GPSFixTrack<Competitor, GPSFixMoving> track) {
        candidates = new TreeSet<>(candidateComparator);
        this.track = track;
    }
    
    /**
     * Tries to extend this stationary sequence at the end by traversing fixes, adding them to the
     * {@link #boundingBoxOfTrackSpanningCandidates} and seeing if the bounding box grows larger than
     * {@link #CANDIDATE_FILTER_DISTANCE} in {@link Bounds#getDiameter() diameter}.
     */
    Pair<Iterable<Candidate>, Iterable<Candidate>> extendAfter() {
        return null; // TODO
    }

    Iterable<Candidate> getValidCandidates() {
        return null; // TODO
    }
    
    Iterable<Candidate> getAllCandidates() {
        return null; // TODO
    }
    
    void add(Candidate candidate) {
        candidates.add(candidate); // TODO update the bounding box here?
    }
    
    Candidate getFirst() {
        return candidates.first();
    }
    
    Candidate getLast() {
        return candidates.last();
    }
    
    /**
     * Returns the candidates that are closer than {@link #CANDIDATE_FILTER_TIME_WINDOW} to this sequence's
     * {@link #getFirst() first} or {@link #getLast() last} {@link Candidate}.
     */
    Iterable<Candidate> getBorderCandidates() {
        final Set<Candidate> result = new HashSet<>();
        result.addAll(candidates.headSet(new CandidateImpl(
                /* one-based index of waypoint */ 1, getFirst().getTimePoint().plus(CANDIDATE_FILTER_TIME_WINDOW),
                /* probability */ 0, /* waypoint */ null)));
        result.addAll(candidates.tailSet(new CandidateImpl(
                /* one-based index of waypoint */ 1, getLast().getTimePoint().minus(CANDIDATE_FILTER_TIME_WINDOW),
                /* probability */ 0, /* waypoint */ null)));
        return result;
    }
}
