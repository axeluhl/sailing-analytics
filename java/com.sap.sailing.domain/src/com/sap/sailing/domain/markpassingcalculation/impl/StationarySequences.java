package com.sap.sailing.domain.markpassingcalculation.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
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
 * because the track leading there would extend the stationary sequence's bounding box beyond limits. Furthermore, all
 * {@link StationarySequence}s maintain their invariant, in particular that the track leading from their
 * {@link StationarySequence#getFirst() first} to their {@link StationarySequence#getLast() last} candidate remains
 * within a bounding box whose {@link Bounds#getDiameter() diameter} remains below the threshold. Calling
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
public class StationarySequences {
    /**
     * Set of sequences managed here; ordered by their {@link StationarySequence#getFirst() first} candidate,
     * based on the candidate comparator passed to the constructor.
     */
    private final SortedSet<StationarySequence> stationarySequences;
    
    /**
     * Set of all candidates managed by this filter. A subset of these may be contained in
     * {@link StationarySequence}s held in {@link #stationarySequences}. Those may be subject
     * to being filtered out if they are not close to the border of their sequence. Candidates
     * that are not part of a {@link StationarySequence} will pass this filter.
     */
    private final SortedSet<Candidate> candidates;

    /**
     * The competitor's position track; fixes on this track decide which {@link Candidate}s form a
     * {@link StationarySequence}
     */
    private DynamicGPSFixTrack<Competitor, GPSFixMoving> track;
    
    protected StationarySequences(final Comparator<Candidate> candidateComparator,
            DynamicGPSFixTrack<Competitor, GPSFixMoving> track) {
        this.stationarySequences = new TreeSet<>((ss1, ss2)->candidateComparator.compare(ss1.getFirst(), ss2.getFirst()));
        this.candidates = new TreeSet<>(candidateComparator);
        this.track = track;
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
        // TODO naively, update the candidates collection for now...
        Util.addAll(newCandidates, candidates);
        Util.removeAll(removedCandidates, candidates);
        return new Pair<>(newCandidates, removedCandidates);
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
     * sequence is started, adding all remaining candidates. Any of the sequences resulting from the split and having
     * only one candidate left is removed. The candidates immediately left and right of the split now pass the filter
     * and therefore are added to the {@link #allEdges graph}.</li>
     * <li>A GPS fix was added outside any stationary sequence. Nothing changes because no bounding box would get
     * smaller by a new fix added.</li>
     * <li>A GPS fix <em>replaces</em> an existing one outside of any stationary sequence. In this case it is possible
     * that the fix replaced caused a bounding box to exceed the diameter threshold and thus avoided a stationary
     * sequence from being created, and the new fix "smoothes" the track such that a stationary sequence may come into
     * existence. Therefore, if a fix is replaced outside of any stationary sequence, an extension attempt is made for
     * any adjacent stationary sequence towards the fix replaced. The extension attempts could stop as they reach the
     * next stationary sequence; however, we could also try to merge two adjacent stationary sequences in their
     * entirety. If this fails, the next best solution is still to extend such that they touch each other.</li>
     * </ul>
     * 
     */
    Pair<Iterable<Candidate>, Iterable<Candidate>> addFix(GPSFixMoving fix) {
        // TODO
        return new Pair<>(Collections.emptySet(), Collections.emptySet());
    }

    /**
     * @return the candidates from the sequences managed here that pass the filter criteria; these
     * are the {@link Candidate}s that are not part of any stationary sequence or are at the border of
     * a stationary sequence, within {@link 
     */
    public Set<Candidate> getFilteredCandidates() {
        return candidates; // TODO
    }
}
