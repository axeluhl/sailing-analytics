package com.sap.sailing.domain.markpassingcalculation.impl;

import java.util.Comparator;
import java.util.HashSet;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Bounds;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.markpassingcalculation.Candidate;
import com.sap.sailing.domain.markpassingcalculation.CandidateChooser;
import com.sap.sailing.domain.markpassingcalculation.CandidateFinder;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

/**
 * Filters one {@link Competitor}'s {@link Candidate}s based on the movement patterns between them. Objects of this
 * class can be used as a filter stage for the {@link CandidateChooser} implementation in order to keep the graph
 * constructed from candidates small. The candidate sequence is analyzed using a bounding box-based approach. As long as
 * the track that connects a sequence of candidates fits in a bounding box not exceeding a certain
 * {@link Bounds#getDiameter() diameter}, as determined by {@link StationarySequence}, those candidates are joined into
 * a "stationary sequence," and only a {@link StationarySequence#getBorderCandidates() short head and tail of the
 * sequence} pass this filter, assuming that with such a time range-based head/tail we capture the candidates relevant
 * for all waypoints to which they may apply. Other candidates that don't fall into a {@link StationarySequence} are
 * passed through by this filter.
 * <p>
 * 
 * This way, objects that were rather "stationary" won't feed huge candidate sets into the {@link #allEdges graph}.
 * Still, as a tracked object starts moving, candidates will be created, even for the stationary segment in the form of
 * a short leading and tailing candidate sequence representing this stationary segment.
 * <p>
 * 
 * The filter is created empty. It keeps track of all stationary sequences found so far and sorts them by time. The
 * algorithm guarantees that after a call to {@link #updateCandidates(Iterable, Iterable)} as well as after a call to
 * {@link #addFix} each stationary sequence has at least two candidates in them which passed the first filter state, and
 * no stationary sequence can be extended to the next candidate following it or any previous candidate preceding it
 * because the track leading there would extend the stationary sequence's bounding box beyond limits, and no two
 * adjacent candidates that are not part of the same {@link StationarySequence} can be joined into a valid such sequence.
 * Furthermore, all {@link StationarySequence}s maintain their invariant, in particular that the track leading from
 * their {@link StationarySequence#getFirst() first} to their {@link StationarySequence#getLast() last} candidate
 * remains within a bounding box whose {@link Bounds#getDiameter() diameter} remains below the threshold. Calling
 * {@link #getFilteredCandidates()} returns those candidates that were announced through
 * {@link #updateCandidates(Iterable, Iterable)} as being added, not being announced later through
 * {@link #updateCandidates(Iterable, Iterable)} as having been removed and that currently pass this filter.
 * <p>
 * 
 * The sequences are non-overlapping but may touch each other, meaning that a preceding sequence's
 * {@link StationarySequence#getLast() last} candidate is the same as the following sequence's
 * {@link StationarySequence#getFirst() first} candidate.
 * <p>
 * 
 * All sequences, candidates and the track belong to the same {@link Competitor}.
 * <p>
 * 
 * This group of sequences must be {@link #addFix(GPSFixMoving) notified} about new position fixes received for the
 * competitor, and for {@link #updateCandidates(Iterable, Iterable) changes} in the set of {@link Candidates} available
 * for processing at this filter stage.
 * <p>
 * 
 * The approach implemented by this class makes no guarantee regarding maximum length stationary sequences. Finding such
 * segments with maximum length is considerably more expensive than finding "good" such segments with a "greedy"
 * algorithm. If we analyze the candidate sequence in chronological order and build up stationary sequences by a
 * "greedy" algorithm, not much harm will be done at the boundaries of two adjacent stationary sequences. Only the two
 * candidates at the sequence boundary would be added and we would also need a solution for which candidates to preserve
 * in case of overlapping stationary sequences. This seems acceptable. The approach will still help to significantly
 * reduce the number of candidates for trackers that remained stationary for a significant amount of time.
 * <p>
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class StationarySequenceBasedFilter {
    /**
     * Set of sequences managed here; ordered by their {@link StationarySequence#getFirst() first} candidate,
     * based on the candidate comparator passed to the constructor.
     */
    private final NavigableSet<StationarySequence> stationarySequences;
    
    /**
     * Set of all candidates managed by this filter. A subset of these may be contained in
     * {@link StationarySequence}s held in {@link #stationarySequences}. Those may be subject
     * to being filtered out if they are not close to the border of their sequence. Candidates
     * that are not part of a {@link StationarySequence} will pass this filter.
     */
    private final NavigableSet<Candidate> candidates;
    
    /**
     * Maintains those {@link Candidate}s from {@link #candidates} and the {@link #startProxyCandidate} and
     * {@link #endProxyCandidate} (if the latter have been added) that pass the filter. The set is updated by calls to
     * {@link #updateCandidates(Iterable, Iterable)} and {@link #updateFixes(Iterable, Iterable)} which each tell the
     * caller how this set has changed.
     */
    private final Set<Candidate> filteredCandidates;

    /**
     * The competitor's position track; fixes on this track decide which {@link Candidate}s form a
     * {@link StationarySequence}
     */
    private DynamicGPSFixTrack<Competitor, GPSFixMoving> track;

    private final Comparator<Candidate> candidateComparator;

    private final Candidate startProxyCandidate;
    
    private final Candidate endProxyCandidate;
    
    /**
     * The start and end proxy candidates need to be known because they need special treatment, particularly because
     * their time points may be {@code null}, and in particular because they explicitly always pass all filters as soon
     * as they have been added. They never participate in any {@link StationarySequence} managed by this filter and are
     * always returned as part of the {@link #getFilteredCandidates()} result once they were added.
     */
    StationarySequenceBasedFilter(final Comparator<Candidate> candidateComparator,
            DynamicGPSFixTrack<Competitor, GPSFixMoving> track, Candidate startProxyCandidate, Candidate endProxyCandidate) {
        this.stationarySequences = new TreeSet<>((ss1, ss2)->candidateComparator.compare(ss1.getFirst(), ss2.getFirst()));
        this.candidateComparator = candidateComparator;
        this.candidates = new TreeSet<>(candidateComparator);
        this.filteredCandidates = new TreeSet<>(candidateComparator);
        this.track = track;
        this.startProxyCandidate = startProxyCandidate;
        this.endProxyCandidate = endProxyCandidate;
    }
    
    /**
     * Creates a {@link StationarySequence} with a single candidate ({@code firstCandidate}) in it. Such a sequence
     * can also be used for searches in {@link #stationarySequences}.
     */
    private StationarySequence createStationarySequence(final Candidate firstCandidate) {
        assert firstCandidate != startProxyCandidate && firstCandidate != endProxyCandidate;
        return new StationarySequence(firstCandidate, candidateComparator, track);
    }
    
    /**
     * Updates this set of {@link StationarySequence}s according to the change in candidates described by the parameters
     * and returns the change in candidates that pass the filter implemented here. For all new candidates we can
     * distinguish the following cases:
     * <ul>
     * <li>There is no existing stationary sequence yet and it's the first candidate that passed the filter's first
     * pass. No sequence can be constructed from a single candidate, so the candidate passes this stage of the
     * filter.</li>
     * <li>The candidate is outside of any existing stationary sequence. Look for neighboring candidates in both
     * directions. Since there is at least one neighboring candidate (otherwise see first case above), traverse the
     * smoothened fixes along the track in the respective direction(s) towards the neighbor candidate(s) that passed the
     * first filter stage. If the respective neighbor belongs to a stationary sequence, check if the fixes keep its
     * bounding box sufficiently small and if so, add the new candidate to that stationary sequence; it passes the
     * filter, whereas the neighbor is removed unless it is less than {@link #CANDIDATE_FILTER_TIME_WINDOW} away from
     * the new candidate. If the neighbor does not belong to a stationary sequence yet and the fixes remained within
     * small-enough bounds, create a new stationary segment with the new candidate and the neighbor. (Note: based on the
     * invariant it is not possible that the new candidate has two neighbors each part of a stationary sequence and all
     * fixes between them fitting into each of their bounding boxes; because if this were the case, the two sequences
     * would already have been merged.)</li>
     * <li>The candidate falls into an existing stationary sequence (at or after first and at or before last candidate
     * in sequence). In this case the set of fixes on the track considered within the sequence hasn't changed. The
     * candidate does not pass the filter, unless it is within {@link #CANDIDATE_FILTER_TIME_WINDOW} from the stationary
     * sequence's start or end. The sequence's bounding box remains unchanged.</li>
     * </ul>
     * For all candidates removed (either because the {@link CandidateFinder} no longer considers them candidates,
     * or because a previous filter stage filtered them out):
     * <ul>
     * <li>If it was not part of a stationary sequence, no action is required.</li>
     * <li>If it was within the time range of an existing stationary sequence and further than
     * {@link #CANDIDATE_FILTER_TIME_WINDOW} away from both of the sequence's borders, it used to be a candidate removed
     * by this second filter stage. It is removed from the stationary sequence but doesn't change the filter
     * results.</li>
     * <li>If it was within the time range of an existing stationary sequence and closer than
     * {@link #CANDIDATE_FILTER_TIME_WINDOW} to one of the sequence's borders, it used to be a candidate passing this
     * second filter stage and therefore has to be removed from both, the stationary sequence and the filter result. If
     * only one candidate is left in the sequence, delete the sequence. Otherwise, if it was the first or the last
     * candidate of the stationary sequence, re-evaluate which fixes on that end of the sequence now fall within
     * {@link #CANDIDATE_FILTER_TIME_WINDOW} from that border of the sequence.</li>
     * </ul>
     * <p>
     */
    Pair<Iterable<Candidate>, Iterable<Candidate>> updateCandidates(Iterable<Candidate> newCandidates, Iterable<Candidate> removedCandidates) {
        // The following two sets build up the resulting "delta." As we may handle more than one change here,
        // changes may also revert previous changes. For example, a candidate that extends a stationary sequence
        // may cause an inner candidate in that sequence to no longer pass the filter; but then removing a candidate
        // from the other end of the same stationary sequence may make the candidate be sufficiently close to that
        // sequence's border again, so it is decided to again pass the filter.
        final Set<Candidate> candidatesEffectivelyAdded = new HashSet<>();
        final Set<Candidate> candidatesEffectivelyRemoved = new HashSet<>();
        for (final Candidate newCandidate : newCandidates) {
            addCandidate(newCandidate, candidatesEffectivelyAdded, candidatesEffectivelyRemoved);
        }
        for (final Candidate removedCandidate : removedCandidates) {
            removeCandidate(removedCandidate, candidatesEffectivelyAdded, candidatesEffectivelyRemoved);
        }
        updateFilteredCandidates(candidatesEffectivelyAdded, candidatesEffectivelyRemoved);
        return new Pair<>(candidatesEffectivelyAdded, candidatesEffectivelyRemoved);
    }

    private void updateFilteredCandidates(final Set<Candidate> candidatesEffectivelyAdded, final Set<Candidate> candidatesEffectivelyRemoved) {
        assert !new HashSet<>(filteredCandidates).removeAll(candidatesEffectivelyAdded);
        filteredCandidates.addAll(candidatesEffectivelyAdded);
        filteredCandidates.removeAll(candidatesEffectivelyRemoved);
        assert !new HashSet<>(candidatesEffectivelyAdded).removeAll(candidatesEffectivelyRemoved) &&
               !new HashSet<>(candidatesEffectivelyRemoved).removeAll(candidatesEffectivelyAdded);
    }

    /**
     * For all new candidates we can distinguish the following cases:
     * <ul>
     * <li>The candidate is outside of any existing stationary sequence. We look for neighboring candidates in both
     * directions. For each neighbor found, if the neighbor belongs to a stationary sequence, check if it can be
     * extended up to the new candidate, and if so, add the new candidate to that stationary sequence; it passes the
     * filter, whereas the neighbor is removed unless it is less than {@link #CANDIDATE_FILTER_TIME_WINDOW} away from
     * the new candidate. If the neighbor does not belong to a stationary sequence yet or that sequence cannot validly
     * be extended up to the new candidate, try to construct a new {@link StationarySequence} from the new candidate to
     * the neighbor (new candidate and neighbor will still pass the filter if such a sequence can be validly constructed
     * as they are the only two candidates in the new sequence for now). (Note: It is not possible that a single
     * {@link StationarySequence} can be constructed spanning between the two neighbors because it it were possible then
     * this would have had to have happened before as it does not depend on the appearance of the new candidate.)</li>
     * <li>The candidate falls into an existing stationary sequence (at or after first and at or before last candidate
     * in sequence). In this case the set of fixes on the track considered within the sequence hasn't changed. The
     * candidate does not pass the filter, unless it is within {@link #CANDIDATE_FILTER_TIME_WINDOW} from the stationary
     * sequence's start or end. The sequence's bounding box remains unchanged.</li>
     * </ul>
     */
    private void addCandidate(Candidate newCandidate,
            Set<Candidate> candidatesEffectivelyAdded, Set<Candidate> candidatesEffectivelyRemoved) {
        assert !candidates.contains(newCandidate);
        if (newCandidate == startProxyCandidate) {
            candidatesEffectivelyAdded.add(newCandidate);
            candidatesEffectivelyRemoved.remove(newCandidate);
        } else if (newCandidate == endProxyCandidate) {
            candidatesEffectivelyAdded.add(newCandidate);
            candidatesEffectivelyRemoved.remove(newCandidate);
        } else {
            candidates.add(newCandidate);
            lookLeft(newCandidate, candidatesEffectivelyAdded, candidatesEffectivelyRemoved);
            lookRight(newCandidate, candidatesEffectivelyAdded, candidatesEffectivelyRemoved);
        }
    }

    private void lookLeft(Candidate newCandidate, Set<Candidate> candidatesEffectivelyAdded,
            Set<Candidate> candidatesEffectivelyRemoved) {
        final StationarySequence latestStationarySequenceStartingAtOrBeforeNewCandidate =
                                            stationarySequences.floor(createStationarySequence(newCandidate));
        final boolean createNewSequenceFromLowerToNew;
        if (latestStationarySequenceStartingAtOrBeforeNewCandidate != null) {
            if (latestStationarySequenceStartingAtOrBeforeNewCandidate.getLast().getTimePoint().before(newCandidate.getTimePoint())) {
                // earlier sequence ends before newCandidate; try to extend, and if extending doesn't work,
                // request a new sequence to be constructed:
                createNewSequenceFromLowerToNew = !latestStationarySequenceStartingAtOrBeforeNewCandidate.tryToExtendAfterLast(
                        newCandidate, candidatesEffectivelyAdded, candidatesEffectivelyRemoved);
            } else {
                // newCandidate falls within the sequence
                latestStationarySequenceStartingAtOrBeforeNewCandidate.addWithin(newCandidate, candidatesEffectivelyAdded, candidatesEffectivelyRemoved);
                createNewSequenceFromLowerToNew = false;
            }
        } else {
            // no stationary sequence found before newCandidate; try to create one
            createNewSequenceFromLowerToNew = true;
        }
        if (createNewSequenceFromLowerToNew) {
            candidatesEffectivelyAdded.add(newCandidate);
            candidatesEffectivelyRemoved.remove(newCandidate);
            // neighbour's stationary sequence can't be extended to newCandidate or there was no earlier stationary sequence,
            // look for an earlier candidate and try to create a new stationary sequence; no candidate from the new
            // sequence will be eliminated by this filter for now as they form the sequence's boundaries.
            final Candidate lower = candidates.lower(newCandidate);
            if (lower != null) {
                final StationarySequence newSequence = tryToConstructStationarySequence(lower, newCandidate);
                if (newSequence != null) {
                    stationarySequences.add(newSequence);
                }
            }
        }
    }
    
    /**
     * Looks for a {@link StationarySequence} starting truly later than {@code newCandidate} because a sequence that ends
     * at {@code newCandidate} would already have been considered by {@link #lookLeft(Candidate, Set, Set)}.
     */
    private void lookRight(Candidate newCandidate, Set<Candidate> candidatesEffectivelyAdded,
            Set<Candidate> candidatesEffectivelyRemoved) {
        final StationarySequence earliestStationarySequenceStartingAfterNewCandidate =
                                            stationarySequences.higher(createStationarySequence(newCandidate));
        if (earliestStationarySequenceStartingAfterNewCandidate == null
                || !earliestStationarySequenceStartingAfterNewCandidate.tryToExtendBeforeFirst(newCandidate,
                        candidatesEffectivelyAdded, candidatesEffectivelyRemoved)) {
            // no later sequence found, or extending it backwards to newCandidate wasn't validly possible;
            // request a new sequence to be constructed; newCandidate will pass the filter as it's one of
            // only two candidates in the new sequence.
            candidatesEffectivelyAdded.add(newCandidate);
            candidatesEffectivelyRemoved.remove(newCandidate);
            final Candidate higher = candidates.higher(newCandidate);
            if (higher != null) {
                final StationarySequence newSequence = tryToConstructStationarySequence(newCandidate, higher);
                if (newSequence != null) {
                    stationarySequences.add(newSequence);
                }
            }
        }
    }
    
    private StationarySequence tryToConstructStationarySequence(Candidate start, Candidate end) {
        assert start != null && end != null;
        final StationarySequence newSequence = createStationarySequence(start);
        // we don't care about the updates here because we know for a fact that both candidates will be
        // passed through as valid by the new sequence as they form its borders
        final StationarySequence result = newSequence.tryToExtendAfterLast(end,
                /* candidatesEffectivelyAdded */ new HashSet<>(), /* candidatesEffectivelyRemoved */ new HashSet<>())
                ? newSequence : null;
        assert result == null || Util.contains(result.getValidCandidates(), start) && Util.contains(result.getValidCandidates(), end);
        return result;
    }

    /**
     * For all candidates removed (either because the {@link CandidateFinder} no longer considers them candidates, or
     * because a previous filter stage filtered them out):
     * <ul>
     * <li>If it was not part of a stationary sequence, no action is required in the stationary sequences; the candidate
     * is removed from the filter results.</li>
     * <li>If it was within the time range of an existing stationary sequence and further than
     * {@link #CANDIDATE_FILTER_TIME_WINDOW} away from both of the sequence's borders, it used to be a candidate removed
     * by this second filter stage. It is removed from the stationary sequence but doesn't change the filter
     * results.</li>
     * <li>If it was within the time range of an existing stationary sequence and closer than
     * {@link #CANDIDATE_FILTER_TIME_WINDOW} to one of the sequence's borders, it used to be a candidate passing this
     * second filter stage and therefore has to be removed from both, the stationary sequence and the filter result. If
     * only one candidate is left in the sequence, delete the sequence. Otherwise, if it was the first or the last
     * candidate of the stationary sequence, re-evaluate which fixes on that end of the sequence now fall within
     * {@link #CANDIDATE_FILTER_TIME_WINDOW} from that border of the sequence.</li>
     * </ul>
     * <p>
     */
    private void removeCandidate(Candidate removedCandidate, Set<Candidate> candidatesEffectivelyAdded,
            Set<Candidate> candidatesEffectivelyRemoved) {
        if (removedCandidate == startProxyCandidate) {
            candidatesEffectivelyRemoved.add(removedCandidate);
            candidatesEffectivelyAdded.remove(removedCandidate);
        } else if (removedCandidate == endProxyCandidate) {
            candidatesEffectivelyRemoved.add(removedCandidate);
            candidatesEffectivelyAdded.remove(removedCandidate);
        } else {
            assert candidates.contains(removedCandidate);
            final StationarySequence latestStationarySequenceStartingAtOrBeforeRemovedCandidate =
                    stationarySequences.floor(createStationarySequence(removedCandidate));
            final boolean addToEffectivelyRemoved;
            if (latestStationarySequenceStartingAtOrBeforeRemovedCandidate != null) {
                if (!latestStationarySequenceStartingAtOrBeforeRemovedCandidate.getLast().getTimePoint()
                        .before(removedCandidate.getTimePoint())) {
                    // within the sequence; remove:
                    latestStationarySequenceStartingAtOrBeforeRemovedCandidate.remove(removedCandidate,
                            candidatesEffectivelyAdded, candidatesEffectivelyRemoved);
                    addToEffectivelyRemoved = false; // already taken care of by the remove call above
                } else {
                    // candidate not in any sequence
                    addToEffectivelyRemoved = true;
                }
            } else {
                // no stationary sequence found before newCandidate
                addToEffectivelyRemoved = true;
            }
            if (addToEffectivelyRemoved) {
                candidatesEffectivelyRemoved.add(removedCandidate);
                candidatesEffectivelyAdded.remove(removedCandidate);
            }
        }
    }

    /**
     * Informs this filter that for its competitor a new position fix was added or replaced. All necessary updates to
     * the stationary sequences managed by this filter are carried out, and the changes that this has to the result of
     * calling {@link #getFilteredCandidates()} are reported as the return value, with the {@link Pair#getA() first}
     * element of the pair being the candidates that now additionally pass this filter, and the {@link Pair#getB()
     * second} element describing those that no longer pass the filter.
     * <p>
     * 
     * The following cases can be distinguished:
     * <ul>
     * <li>A GPS fix was added to or replaces one of those in the competitor's track within the time range of an
     * existing stationary sequence. The fix needs to be added to the stationary sequence's bounding box. If the box
     * still remains small enough, nothing changes. Otherwise, the algorithm tries to extend the stationary sequence
     * from the last candidate before the new fix until the track lets the bounding box grow too large. A new stationary
     * sequence is started, adding all remaining candidates. Any sequence resulting from such a split and having only
     * one candidate left is removed. The candidates immediately left and right of the split now pass the filter and
     * therefore are added to the {@link #allEdges graph}.</li>
     * <li>A GPS fix was added outside any stationary sequence. Nothing changes because no bounding box would get
     * smaller by a new fix added.</li>
     * <li>A GPS fix <em>replaces</em> an existing one outside of any stationary sequence. In this case it is possible
     * that the fix replaced caused a bounding box to exceed the diameter threshold and thus avoided a stationary
     * sequence from being created or extended, and the new fix "smoothes" the track such that a stationary sequence may come into
     * existence or be extended. Therefore, if a fix is replaced outside of any stationary sequence, a stationary sequence creation or
     * extension attempt is made for any adjacent candidate towards the fix replaced. For simplicity, the extension
     * attempts will stop as they reach the next candidate, although slightly improved results may be
     * achievable by trying to merge two adjacent stationary sequences in their entirety where possible. We can, however,
     * be sure that we won't miss any possible segment between two candidates; being able to extend further than up to the next
     * candidate would mean that there must already have been a stationary sequence covering the segment from the next to
     * the one after the next, but that was excluded because we didn't want to extend into existing adjacent stationary
     * sequences.</li>
     * </ul>
     * 
     * @return the candidates that now pass the filter after applying the fix changes (and which didn't pass before) as
     *         the {@link Pair#getA() first} element of the pair returned, and the candidates that no longer pass the
     *         filter after applying the fix changes (and which did pass the filter before) as the {@link Pair#getB()
     *         second} element of the pair returned.
     */
    Pair<Iterable<Candidate>, Iterable<Candidate>> updateFixes(Iterable<GPSFixMoving> newFixes,
            Iterable<GPSFixMoving> fixesReplacingExistingOnes) {
        final Set<Candidate> candidatesEffectivelyAdded = new HashSet<>();
        final Set<Candidate> candidatesEffectivelyRemoved = new HashSet<>();
        if (newFixes != null) {
            for (final GPSFixMoving newFix : newFixes) {
                final StationarySequence lastSequenceStartingAtOrBeforeFix = stationarySequences.floor(createStationarySequence(
                        StationarySequence.createDummyCandidate(newFix.getTimePoint())));
                if (lastSequenceStartingAtOrBeforeFix != null && !lastSequenceStartingAtOrBeforeFix.getLast().getTimePoint().before(newFix.getTimePoint())) {
                    // fix falls into the existing StationarySequence; update its bounding box:
                    final StationarySequence splitResult = lastSequenceStartingAtOrBeforeFix.tryToAddFix(newFix, candidatesEffectivelyAdded, candidatesEffectivelyRemoved);
                    if (Util.size(lastSequenceStartingAtOrBeforeFix.getAllCandidates()) <= 1) {
                        stationarySequences.remove(lastSequenceStartingAtOrBeforeFix);
                    }
                    if (splitResult != null) {
                        stationarySequences.add(splitResult);
                    }
                }
            }
        }
        if (fixesReplacingExistingOnes != null) {
            for (final GPSFixMoving fixReplacingExistingOne : fixesReplacingExistingOnes) {
                assert Util.contains(newFixes, fixesReplacingExistingOnes);
                final Candidate dummyCandidateForReplacementFix = StationarySequence.createDummyCandidate(fixReplacingExistingOne.getTimePoint());
                final StationarySequence dummyStationarySequenceForFix = createStationarySequence(dummyCandidateForReplacementFix);
                final StationarySequence lastSequenceStartingAtOrBeforeFix = stationarySequences.floor(dummyStationarySequenceForFix);
                final boolean fixIsInStationarySequence = !lastSequenceStartingAtOrBeforeFix.getLast().getTimePoint().before(fixReplacingExistingOne.getTimePoint());
                if (!fixIsInStationarySequence) {
                    final StationarySequence lastSequenceEndingBeforeFix = // null in case fix is within a stationary sequence
                            lastSequenceStartingAtOrBeforeFix != null && fixIsInStationarySequence ? null : lastSequenceStartingAtOrBeforeFix;
                    final Candidate lastCandidateBeforeReplacementFix = candidates.lower(dummyCandidateForReplacementFix);
                    if (lastCandidateBeforeReplacementFix != null) {
                        final Candidate firstCandidateAfterReplacementFix = candidates.higher(dummyCandidateForReplacementFix);
                        if (firstCandidateAfterReplacementFix != null) {
                            // the fix is between two candidates, so we may try to extend or create a stationary sequence:
                            if (lastCandidateBeforeReplacementFix == lastSequenceEndingBeforeFix.getLast()) {
                                // previous candidate is end of a sequence; try to extend
                                lastSequenceEndingBeforeFix.tryToExtendAfterLast(firstCandidateAfterReplacementFix, candidatesEffectivelyAdded, candidatesEffectivelyRemoved);
                            } else {
                                final StationarySequence firstSequenceStartingAfterFix = stationarySequences.higher(dummyStationarySequenceForFix);
                                if (firstCandidateAfterReplacementFix == firstSequenceStartingAfterFix.getFirst()) {
                                    // next candidate is start of a sequence; try to extend
                                    firstSequenceStartingAfterFix.tryToExtendBeforeFirst(lastCandidateBeforeReplacementFix, candidatesEffectivelyAdded, candidatesEffectivelyRemoved);
                                } else {
                                    // none of the adjacent candidates is part of a sequence; try to create a new one:
                                    final StationarySequence newSequence = createStationarySequence(lastCandidateBeforeReplacementFix);
                                    if (newSequence.tryToExtendAfterLast(firstCandidateAfterReplacementFix, candidatesEffectivelyAdded, candidatesEffectivelyRemoved)) {
                                        stationarySequences.add(newSequence);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        updateFilteredCandidates(candidatesEffectivelyAdded, candidatesEffectivelyRemoved);
        return new Pair<>(candidatesEffectivelyAdded, candidatesEffectivelyRemoved);
    }

    /**
     * @return the candidates from the sequences managed here that pass the filter criteria; these are the
     *         {@link Candidate}s that are not part of any stationary sequence or are at the border of a stationary
     *         sequence.
     */
    Iterable<Candidate> getFilteredCandidates() {
        return filteredCandidates;
    }
}
