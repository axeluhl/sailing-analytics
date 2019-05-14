package com.sap.sailing.domain.markpassingcalculation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Bounds;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.BoundsImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.markpassingcalculation.Candidate;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.TimeRangeImpl;

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
    public static final Distance CANDIDATE_FILTER_DISTANCE = new MeterDistance(30);

    public static final Duration CANDIDATE_FILTER_TIME_WINDOW = Duration.ONE_SECOND.times(10);
    
    private final NavigableSet<Candidate> candidates;

    private final GPSFixTrack<Competitor, GPSFixMoving> track;
    
    private Bounds boundingBoxOfTrackSpanningCandidates;

    private final Comparator<Candidate> candidateComparator;

    /**
     * Constructs a new stationary sequence with a single {@code seed} candidate in it which acts as {@link #getFirst()
     * first} and {@link #getLast() last} element at the same time. Therefore, the track segment spanned by the
     * resulting sequence is empty which trivially fulfills the bounding box criterion.
     */
    public StationarySequence(Candidate seed, Comparator<Candidate> candidateComparator, GPSFixTrack<Competitor, GPSFixMoving> track) {
        this.candidates = new TreeSet<>(candidateComparator);
        this.candidateComparator = candidateComparator;
        this.track = track;
        this.candidates.add(seed);
        boundingBoxOfTrackSpanningCandidates = createNewBounds(seed);
    }
    
    /**
     * Tries to extend this stationary sequence at the end by traversing fixes, adding them to the
     * {@link #boundingBoxOfTrackSpanningCandidates} and seeing if the bounding box grows larger than
     * {@link #CANDIDATE_FILTER_DISTANCE} in {@link Bounds#getDiameter() diameter}. If it does, nothing changes, and
     * {@code false} is returned. But if the track leading to the {@code candidateAfterSequence} keeps the bounding box
     * sufficiently small, the candidate is added, the {@link #boundingBoxOfTrackSpanningCandidates} is adjusted,
     * the {@code candidateAfterSequence} will be added to {@code candidatesEffectivelyAdded} because it is the
     * new {@link #getLast() last} element of this sequence, and all other candidates in this sequence
     * that no longer are sufficiently close (time-wise) to the new last candidate are added to
     * {@code candidatesEffectivelyRemoved}; {@code true} is returned in this case, indicating that the sequence
     * has been extended successfully.
     */
    public boolean tryToExtendAfterLast(Candidate candidateAfterSequence,
            Set<Candidate> candidatesEffectivelyAdded, Set<Candidate> candidatesEffectivelyRemoved) {
        Bounds bounds = computeExtendedBoundsForFixesBetweenCandidates(getLast(), candidateAfterSequence);
        if (bounds != null) {
            boundingBoxOfTrackSpanningCandidates = bounds;
            final Candidate lastSoFar = candidates.last();
            final Duration extendedBy = lastSoFar.getTimePoint().until(candidateAfterSequence.getTimePoint());
            candidates.add(candidateAfterSequence); // non-empty sequence extended at end, so first candidate does not change
            candidatesEffectivelyAdded.add(candidateAfterSequence);
            candidatesEffectivelyRemoved.remove(candidateAfterSequence);
            // now compute the candidates that no longer pass the filter because they are too far away from the
            // borders of this sequence:
            final TimeRange timeRangeNoLongerPassingFilter = getExtendedEndBorderRange(extendedBy);
            if (timeRangeNoLongerPassingFilter != null) {
                removeFromFilterResult(timeRangeNoLongerPassingFilter, candidatesEffectivelyAdded, candidatesEffectivelyRemoved);
            }
        }
        return bounds != null;
    }

    /**
     * Has no side effects on this object. In particular, the {@link #boundingBoxOfTrackSpanningCandidates} field is not
     * yet updated to the result of this method. Use the method to test whether extending would be possible.
     * 
     * @return {@code null} if {@link #boundingBoxOfTrackSpanningCandidates} was {@code null} already or the
     *         {@link #boundingBoxOfTrackSpanningCandidates} would grow too large if the fixes between {@code start} and
     *         {@code end} were inserted; the extended bounds otherwise.
     */
    private Bounds computeExtendedBoundsForFixesBetweenCandidates(Candidate start, Candidate end) {
        Bounds bounds = boundingBoxOfTrackSpanningCandidates;
        if (bounds != null) {
            track.lockForRead();
            try {
                final Iterator<GPSFixMoving> iter = track.getFixesIterator(start.getTimePoint(), /* inclusive */ true,
                        end.getTimePoint(), /* inclusive */ true);
                while (iter.hasNext()) {
                    final GPSFixMoving fix = iter.next();
                    bounds = bounds.extend(fix.getPosition());
                    if (bounds.getDiameter().compareTo(CANDIDATE_FILTER_DISTANCE) > 0) {
                        bounds = null;
                        break;
                    }
                }
            } finally {
                track.unlockAfterRead();
            }
        }
        return bounds;
    }

    /**
     * Tries to extend this stationary sequence before the start by traversing fixes, adding them to the
     * {@link #boundingBoxOfTrackSpanningCandidates} and seeing if the bounding box grows larger than
     * {@link #CANDIDATE_FILTER_DISTANCE} in {@link Bounds#getDiameter() diameter}. If it does, nothing changes, and
     * {@code false} is returned. But if the track leading to the {@code candidateBeforeSequence} keeps the bounding box
     * sufficiently small, the candidate is added, the {@link #boundingBoxOfTrackSpanningCandidates} is adjusted, the
     * {@code candidateBeforeSequence} will be added to {@code candidatesEffectivelyAdded} because it is the new
     * {@link #getFirst() first} element of this sequence, and all other candidates in this sequence that no longer are
     * sufficiently close (time-wise) to the new first candidate are added to {@code candidatesEffectivelyRemoved};
     * {@code true} is returned in this case, indicating that the sequence has been extended successfully.
     * 
     * @param stationarySequenceSetToUpdate
     *            when this method causes a change in what {@link #getFirst()} returns before and after the call, this
     *            method maintains the set referenced by this parameter accordingly, assuming that the position in the
     *            set may change, or, if this sequence runs empty, it has to be removed from the set altogether.
     */
    public boolean tryToExtendBeforeFirst(Candidate candidateBeforeSequence,
            Set<Candidate> candidatesEffectivelyAdded, Set<Candidate> candidatesEffectivelyRemoved,
            NavigableSet<StationarySequence> stationarySequenceSetToUpdate) {
        Bounds bounds = computeExtendedBoundsForFixesBetweenCandidates(candidateBeforeSequence, getFirst());
        if (bounds != null) {
            boundingBoxOfTrackSpanningCandidates = bounds;
            final Candidate firstSoFar = candidates.first();
            final Duration extendedBy = candidateBeforeSequence.getTimePoint().until(firstSoFar.getTimePoint());
            stationarySequenceSetToUpdate.remove(this);
            candidates.add(candidateBeforeSequence);
            stationarySequenceSetToUpdate.add(this);
            candidatesEffectivelyAdded.add(candidateBeforeSequence);
            candidatesEffectivelyRemoved.remove(candidateBeforeSequence);
            // now compute the candidates that no longer pass the filter because they are too far away from the
            // borders of this sequence:
            final TimeRange timeRangeNoLongerPassingFilter = getExtendedStartBorderRange(extendedBy);
            if (timeRangeNoLongerPassingFilter != null) {
                removeFromFilterResult(timeRangeNoLongerPassingFilter, candidatesEffectivelyAdded, candidatesEffectivelyRemoved);
            }
        }
        return bounds != null;
    }

    /**
     * @return a valid time range describing the area starting {@link #CANDIDATE_FILTER_TIME_WINDOW} after the
     *         {@link #getFirst() first} candidate, reaching at most {@code extendedBy}, but no further than up to
     *         {@link #CANDIDATE_FILTER_TIME_WINDOW} before the {@link #getLast() last} candidate.
     */
    private TimeRange getExtendedStartBorderRange(Duration extendedBy) {
        final TimePoint startOfTimeRangeNoLongerPassingFilter = getFirst().getTimePoint().plus(CANDIDATE_FILTER_TIME_WINDOW);
        // crop the "right" end of the "invalidation time range" at the beginning of the "valid" time range on the "right" border:
        final TimePoint endOfTimeRangeNoLongerPassingFilter = startOfTimeRangeNoLongerPassingFilter.plus(extendedBy).after(
                getLast().getTimePoint().minus(CANDIDATE_FILTER_TIME_WINDOW)) ?
                        getLast().getTimePoint().minus(CANDIDATE_FILTER_TIME_WINDOW) :
                            startOfTimeRangeNoLongerPassingFilter.plus(extendedBy);
        return timeRangeOrNull(endOfTimeRangeNoLongerPassingFilter, startOfTimeRangeNoLongerPassingFilter);
    }

    /**
     * @return a valid time range describing the area ending {@link #CANDIDATE_FILTER_TIME_WINDOW} before the
     *         {@link #getLast() last} candidate, reaching at most {@code extendedBy}, but no further than up to
     *         {@link #CANDIDATE_FILTER_TIME_WINDOW} after the {@link #getFirst() first} candidate.
     */
    private TimeRange getExtendedEndBorderRange(Duration extendedBy) {
        final TimePoint endOfTimeRangeNoLongerPassingFilter = getLast().getTimePoint().minus(CANDIDATE_FILTER_TIME_WINDOW);
        // crop the "left" end of the "invalidation time range" at the end of the "valid" time range on the "left" border:
        final TimePoint startOfTimeRangeNoLongerPassingFilter = endOfTimeRangeNoLongerPassingFilter.minus(extendedBy).before(
                getFirst().getTimePoint().plus(CANDIDATE_FILTER_TIME_WINDOW)) ?
                        getFirst().getTimePoint().plus(CANDIDATE_FILTER_TIME_WINDOW) :
                            endOfTimeRangeNoLongerPassingFilter.minus(extendedBy);
        return timeRangeOrNull(endOfTimeRangeNoLongerPassingFilter, startOfTimeRangeNoLongerPassingFilter);
    }

    private TimeRange timeRangeOrNull(final TimePoint from, final TimePoint to) {
        final TimeRange result;
        if (from.after(to)) {
            result = new TimeRangeImpl(to, from);
        } else {
            result = null;
        }
        return result;
    }

    private void removeFromFilterResult(final TimeRange timeRangeNoLongerPassingFilter, Set<Candidate> candidatesEffectivelyAdded,
        Set<Candidate> candidatesEffectivelyRemoved) {
        final SortedSet<Candidate> candidatesNoLongerPassingFilter = getCandidatesInTimeRange(timeRangeNoLongerPassingFilter);
        candidatesEffectivelyAdded.removeAll(candidatesNoLongerPassingFilter);
        candidatesEffectivelyRemoved.addAll(candidatesNoLongerPassingFilter);
    }

    private SortedSet<Candidate> getCandidatesInTimeRange(final TimeRange timeRange) {
        final SortedSet<Candidate> candidatesNoLongerPassingFilter = candidates.subSet(
                getCandidateMatchingTimePointOrCreateDummy(timeRange.from()),
                getCandidateMatchingTimePointOrCreateDummy(timeRange.to()));
        return candidatesNoLongerPassingFilter;
    }

    public Iterable<Candidate> getValidCandidates() {
        return ()->candidates.stream().filter(c->isCloseEnoughToSequenceBorder(c)).iterator();
    }
    
    private boolean isCloseEnoughToSequenceBorder(Candidate candidate) {
        return candidate.getTimePoint().until(getFirst().getTimePoint()).abs().compareTo(CANDIDATE_FILTER_TIME_WINDOW) < 0 ||
                candidate.getTimePoint().until(getLast().getTimePoint()).abs().compareTo(CANDIDATE_FILTER_TIME_WINDOW) < 0;
    }
    
    public int size() {
        return candidates.size();
    }
    
    public boolean isEmpty() {
        return candidates.isEmpty();
    }
    
    /**
     * Adds a candidate whose time point is within (including the first/last candidate's time points) the
     * time range of this sequence. This will not change this sequence's set of fixes spanned. Therefore,
     * also the {@link #boundingBoxOfTrackSpanningCandidates bounding box} remains unchanged. If the candidate
     * is closer than {@link #CANDIDATE_FILTER_TIME_WINDOW} to {@link #getFirst()} or {@link #getLast()} then
     * it is added to {@code candidatesEffectivelyRemoved}.
     */
    void addWithin(Candidate candidate, Set<Candidate> candidatesEffectivelyAdded, Set<Candidate> candidatesEffectivelyRemoved) {
        assert !candidates.contains(candidate) && !getFirst().getTimePoint().after(candidate.getTimePoint()) &&
                !getLast().getTimePoint().before(candidate.getTimePoint());
        candidates.add(candidate);
        if (isCloseEnoughToSequenceBorder(candidate)) {
            candidatesEffectivelyAdded.add(candidate);
            candidatesEffectivelyRemoved.remove(candidate);
        }
    }
    
    /**
     * Removes the candidate from {@link candidates}. If the candidate was a valid candidate because it was sufficiently
     * close to at least one of this sequence's borders, it is added to {@code candidatesEffectivelyRemoved} and removed
     * from {@code candidatesEffectivelyAdded}. If it was the first or the last candidate in this sequence, the time
     * difference to the new candidate on the respective border is calculated and used to add new candidates to the
     * filter result incrementally which were previously filtered out because they were too far away from the border.
     * 
     * @param stationarySequenceSetToUpdate
     *            when this method causes a change in what {@link #getFirst()} returns before and after the call, this
     *            method maintains the set referenced by this parameter accordingly, assuming that the position in the
     *            set may change, or, if this sequence runs empty, it has to be removed from the set altogether. Furthermore,
     *            should removing the {@code candidate} reduce this sequence's size to {@code 1}, this sequence is
     *            removed from the {@code stationarySequenceSetToUpdate}.
     */
    void remove(Candidate candidate, Set<Candidate> candidatesEffectivelyAdded, Set<Candidate> candidatesEffectivelyRemoved,
            NavigableSet<StationarySequence> stationarySequenceSetToUpdate) {
        assert candidates.contains(candidate);
        final boolean wasValidCandidate = isCloseEnoughToSequenceBorder(candidate);
        final boolean wasFirst = candidate == getFirst();
        final boolean wasLast = candidate == getLast();
        if (wasFirst || size() == 2) { // if size() == 2 then it will shrink to 1 and this sequence shall be removed
            stationarySequenceSetToUpdate.remove(this);
        }
        candidates.remove(candidate);
        if (wasFirst && candidates.size() > 1) {
            stationarySequenceSetToUpdate.add(this);
        }
        if (wasValidCandidate) {
            candidatesEffectivelyRemoved.add(candidate);
            candidatesEffectivelyAdded.remove(candidate);
            if (wasFirst) {
                final Duration extendedBy = candidate.getTimePoint().until(getFirst().getTimePoint());
                if (extendedBy.compareTo(Duration.NULL) > 0) {
                    refreshBoundingBox();
                }
                final TimeRange timeRangeForCandidatesBecomingValid = getExtendedStartBorderRange(extendedBy);
                if (timeRangeForCandidatesBecomingValid != null) {
                    addToFilterResult(timeRangeForCandidatesBecomingValid, candidatesEffectivelyAdded,
                            candidatesEffectivelyRemoved);
                }
            }
            if (wasLast) {
                final Duration extendedBy = getFirst().getTimePoint().until(candidate.getTimePoint());
                if (extendedBy.compareTo(Duration.NULL) > 0) {
                    refreshBoundingBox();
                }
                final TimeRange timeRangeForCandidatesBecomingValid = getExtendedEndBorderRange(extendedBy);
                if (timeRangeForCandidatesBecomingValid != null) {
                    addToFilterResult(timeRangeForCandidatesBecomingValid, candidatesEffectivelyAdded,
                            candidatesEffectivelyRemoved);
                }
            }
        }
    }

    private void refreshBoundingBox() {
        if (isEmpty()) {
            boundingBoxOfTrackSpanningCandidates = null;
        } else {
            boundingBoxOfTrackSpanningCandidates = createNewBounds(getFirst());
            boundingBoxOfTrackSpanningCandidates = computeExtendedBoundsForFixesBetweenCandidates(getFirst(), getLast());
            assert boundingBoxOfTrackSpanningCandidates != null;
        }
    }

    /**
     * Creates new {@link Bounds} based on the estimated position on the {@link #track} at the {@code candidate}'s time point.
     * If no estimated position can be obtained, {@code null} is returned.
     */
    private Bounds createNewBounds(Candidate candidate) {
        final Position estimatedPosition = track.getEstimatedPosition(candidate.getTimePoint(), /* extrapolate */ false);
        return estimatedPosition == null ? null : new BoundsImpl(estimatedPosition);
    }

    private void addToFilterResult(final TimeRange timeRangeForCandidatesBecomingValid,
            Set<Candidate> candidatesEffectivelyAdded, Set<Candidate> candidatesEffectivelyRemoved) {
        final SortedSet<Candidate> candidatesBecomingValid = getCandidatesInTimeRange(timeRangeForCandidatesBecomingValid);
        candidatesEffectivelyAdded.addAll(candidatesBecomingValid);
        candidatesEffectivelyRemoved.removeAll(candidatesBecomingValid);
    }
    
    Candidate getFirst() {
        return candidates.first();
    }
    
    Candidate getLast() {
        return candidates.last();
    }
    
    /**
     * A fix was added to the track in the time range spanned by this stationary sequence. One of the following cases
     * applied:
     * <ul>
     * <li>Adding the fix to the {@link #boundingBoxOfTrackSpanningCandidates} keeps the bounding box's
     * {@link Bounds#getDiameter() diameter} within {@link #CANDIDATE_FILTER_DISTANCE thresholds}. Only the bounding box
     * update is performed, and no change to the candidates that pass this filter is applied.</li>
     * <li>Adding the fix enlarged the bounding box beyond thresholds. This stationary sequence needs splitting. This
     * object keeps its {@link #getFirst() first} candidate and all further candidates up to but excluding the time
     * point of the {@code newFix}. (Note: the first candidate may still be exactly at the fix, but a resulting sequence
     * with only one candidate will be removed anyway.) A second, new stationary sequence is created for all remaining
     * candidates if there are at least two of them. The changes to the filter results are announced by updating
     * {@code candidatesEffectivelyAdded} and {@code candidatesEffectivelyRemoved}.</li>
     * </ul>
     * 
     * @param stationarySequenceSetToUpdate
     *            when this method causes a change in what {@link #getFirst()} returns before and after the call,
     *            this method maintains the set referenced by this parameter accordingly, assuming that the position
     *            in the set may change, or, if this sequence runs empty, it has to be removed from the set altogether.
     * 
     * @return {@code null} if no new {@link StationarySequence} resulted from any splitting activity (could be because
     *         no split took place, or the split didn't leave more than one candidate for a second sequence}; the new
     *         sequence created by a split otherwise.
     */
    public StationarySequence tryToAddFix(GPSFixMoving newFix, Set<Candidate> candidatesEffectivelyAdded,
            Set<Candidate> candidatesEffectivelyRemoved, NavigableSet<StationarySequence> stationarySequenceSetToUpdate,
            boolean isReplacement) {
        assert !newFix.getTimePoint().before(getFirst().getTimePoint());
        assert !newFix.getTimePoint().after(getLast().getTimePoint());
        final Bounds newBounds = boundingBoxOfTrackSpanningCandidates.extend(newFix.getPosition());
        final StationarySequence tailSequence;
        if (newBounds.getDiameter().compareTo(CANDIDATE_FILTER_DISTANCE) < 0) {
            boundingBoxOfTrackSpanningCandidates = newBounds; // ...and we're done
            tailSequence = null;
            if (isReplacement) {
                // the fix could have replaced another one, and the bounding box may shrink now;
                // later additions may unnecessarily split, so we should be kind and refresh the bounding
                // box in this case:
                refreshBoundingBox();
            }
        } else {
            // split:
            final Set<Candidate> oldValidCandidates = new TreeSet<>(candidateComparator);
            Util.addAll(getValidCandidates(), oldValidCandidates);
            final Candidate candidateForFixTimePoint = getCandidateMatchingTimePointOrCreateDummy(newFix.getTimePoint());
            SortedSet<Candidate> tailSet = candidates.tailSet(candidateForFixTimePoint); // will contain a candidate for newFix.getTimePoint() if such a candidate exists
            boolean tryToAddCandidateAtFixLater = false;
            if (!tailSet.isEmpty() && tailSet.first().getTimePoint().equals(candidateForFixTimePoint.getTimePoint())) {
                // new fix TimePoint matches exactly with that of a candidate in this stationary sequence;
                // construct the tailing stationary sequence
                // without this candidate to start with, then try whether it can be extended to the left:
                tryToAddCandidateAtFixLater = true;
                tailSet = candidates.tailSet(candidateForFixTimePoint, /* inclusive */ false);
                assert !tailSet.contains(candidateForFixTimePoint);
            }
            tailSequence = tailSet.isEmpty() ? null : createStationarySequence(tailSet);
            if (tailSequence != null && tryToAddCandidateAtFixLater) {
                tailSequence.tryToExtendBeforeFirst(candidates.floor(candidateForFixTimePoint),
                        new TreeSet<>(candidateComparator), new TreeSet<>(candidateComparator), stationarySequenceSetToUpdate);
            }
            // now remove the tail set candidates from this stationary sequence:
            final ArrayList<Candidate> fullTailSet = new ArrayList<>(candidates.tailSet(candidateForFixTimePoint));
            if (!fullTailSet.isEmpty() && fullTailSet.get(0) == getFirst()) {
                // all candidates will be removed from this sequence; the sequence must be removed from its containing set before removing the first candidate...
                stationarySequenceSetToUpdate.remove(this);
            }
            candidates.removeAll(fullTailSet);
            if (size() == 1) {
                stationarySequenceSetToUpdate.remove(this);
            }
            // ...and it doesn't need adding because if it was removed, it's empty now.
            refreshBoundingBox();
            final Set<Candidate> newValidCandidates = new TreeSet<>(candidateComparator);
            Util.addAll(getValidCandidates(), newValidCandidates);
            if (tailSequence != null) { // this includes the possibility of a single candidate being added to the tail set
                Util.addAll(tailSequence.getValidCandidates(), newValidCandidates);
            }
            final Set<Candidate> candidatesAdded = new TreeSet<>(candidateComparator);
            candidatesAdded.addAll(newValidCandidates);
            candidatesAdded.removeAll(oldValidCandidates);
            final Set<Candidate> candidatesRemoved = new TreeSet<>(candidateComparator);
            candidatesRemoved.addAll(oldValidCandidates);
            candidatesRemoved.removeAll(newValidCandidates);
            candidatesEffectivelyAdded.addAll(candidatesAdded);
            candidatesEffectivelyRemoved.removeAll(candidatesAdded);
            candidatesEffectivelyRemoved.addAll(candidatesRemoved);
            candidatesEffectivelyAdded.removeAll(candidatesRemoved);
        }
        final StationarySequence result = tailSequence != null && tailSequence.size() >= 2 ? tailSequence : null;
        assert result == null || !this.getLast().getTimePoint().equals(result.getFirst().getTimePoint());
        return result;
    }

    /**
     * Precondition: the track between the {@code candidates} is valid for a stationary sequence, not leaving a bounding
     * box of diameter {@link #CANDIDATE_FILTER_DISTANCE}, and {@code candidates} contains at least one element.<p>
     * 
     * Constructs a new sequence from the candidates. No updates to {@link #candidates} are performed here.
     * 
     * @return a sequence with the candidate(s)
     */
    private StationarySequence createStationarySequence(SortedSet<Candidate> candidates) {
        final Set<Candidate> candidatesEffectivelyAdded = new TreeSet<>(candidateComparator);
        final Set<Candidate> candidatesEffectivelyRemoved = new TreeSet<>(candidateComparator);
        final Iterator<Candidate> candidateIterator = candidates.iterator();
        assert candidateIterator.hasNext();
        final StationarySequence result = new StationarySequence(candidateIterator.next(), candidateComparator, track);
        while (candidateIterator.hasNext()) {
            boolean extensionOk = result.tryToExtendAfterLast(candidateIterator.next(), candidatesEffectivelyAdded, candidatesEffectivelyRemoved);
            assert extensionOk;
        }
        return result;
    }

    /**
     * If this sequence contains a {@link Candidate} that matches the {@code timePoint} exactly, that candidate is
     * returned. Otherwise, a dummy candidate is created that has the {@code timePoint} provided.<p>
     * 
     * Note that depending on the {@link #candidateComparator} used, two candidates are not necessarily considered
     * equal only because they have equal time points.
     */
    private Candidate getCandidateMatchingTimePointOrCreateDummy(TimePoint timePoint) {
        final Candidate dummy = new CandidateImpl(/* one-based index of waypoint */ 1, timePoint, /* probability */ 0, /* waypoint */ null);
        final Candidate floorCandidate = candidates.floor(dummy);
        final Candidate ceilingCandidate = candidates.ceiling(dummy);
        final Candidate result;
        if (floorCandidate.getTimePoint().equals(timePoint)) {
            result = floorCandidate;
        } else if (ceilingCandidate.getTimePoint().equals(timePoint)) {
            result = ceilingCandidate;
        } else {
            result = dummy;
        }
        return result;
    }
    
    @Override
    public String toString() {
        return "Stationary Sequence "+candidates+", bounds diameter "+boundingBoxOfTrackSpanningCandidates.getDiameter();
    }
    
    /**
     * Creates a dummy time point that can be used for searching in {@link #candidates} by time point. It
     * uses {@code 1} for the one-based waypoint index, {@code null} for the waypoint and {@code 0.0} for its
     * probability.
     */
    static Candidate createDummyCandidate(TimePoint timePoint) {
        return new CandidateImpl(/* one-based index of waypoint */ 1, timePoint, /* probability */ 0, /* waypoint */ null);
    }

    public Iterable<Candidate> getAllCandidates() {
        return Collections.unmodifiableCollection(candidates);
    }

    /**
     * Tells whether the {@link #candidates} set contains {@code candidate}. Containment is decided based on
     * the {@link #candidateComparator}'s notion of equality. 
     */
    public boolean contains(Candidate candidate) {
        return candidates.contains(candidate);
    }
}

