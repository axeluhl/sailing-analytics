package com.sap.sailing.domain.markpassingcalculation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.sap.sailing.domain.markpassingcalculation.Candidate;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.TimeRangeImpl;

/**
 * A candidate filter based on the notion that within a time window with candidates each not further apart
 * than a relatively short duration, only the most probable candidates should be selected for addition to
 * the graph used to determine the actual mark passings.<p>
 * 
 * This filter works for a single competitor and maintains the filter results in an incremental fashion.
 * When calling {@link #updateCandidates(NavigableSet, Iterable, Iterable)} with
 * the total set of candidates for the competitor, as well as a set of new and removed original candidates,
 * this filter updates its filter results which can efficiently be obtained by calling {@link #getFilteredCandidates()}
 * and returns the changes to those filter results for incremental onward processing, e.g., by a next filter level.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class MostProbableCandidatesInSmallTimeRangeFilter {
    /**
     * Candidate filtering will be used to select only the most likely Candidates from those that are very close
     * time-wise. The duration is used to construct clusters of candidates per competitor such that for each pair of two
     * candidates in the cluster there is a sequence of candidates in the same cluster with the two candidates at the
     * path's ends such that each path step spans a duration less than or equal to the duration specified by this field.
     * In other words, each cluster is the transitive hull of fixes not further than this duration apart.
     */
    private static final Duration CANDIDATE_FILTER_TIME_WINDOW = Duration.ONE_SECOND.times(5);
    
    /**
     * When looking for the most probable candidates within time windows of length {@link #CANDIDATE_FILTER_TIME_WINDOW},
     * all candidates are considered that have at most this much lower probability than the candidate with the
     * highest probability in the contiguous sequence.
     */
    private static final double MAX_PROBABILITY_DELTA = 0.20;
    
    private final NavigableSet<Candidate> filteredCandidates;
    private final Comparator<Candidate> candidateComparator;
    private final Candidate startProxyCandidate;
    private final Candidate endProxyCandidate;

    MostProbableCandidatesInSmallTimeRangeFilter(Comparator<Candidate> candidateComparator, Candidate startProxyCandidate,
            Candidate endProxyCandidate) {
        this.candidateComparator = candidateComparator;
        filteredCandidates = new TreeSet<>(candidateComparator);
        this.startProxyCandidate = startProxyCandidate;
        this.endProxyCandidate = endProxyCandidate;
    }
    
    /**
     * Finds the contiguous (by definition of {@link #CANDIDATE_FILTER_TIME_WINDOW}) sequences of candidates adjacent to
     * those candidates added / removed. The {@code competitorCandidates} set is expected to already contain the
     * {@code newCandidates} and to no longer contain the {@code removedCandidates}. {@link #filteredCandidatesStage1} is
     * expected to not yet reflect the changes described by {@code newCandidates} and {@code removedCandidates}.
     * <p>
     * 
     * The following steps are performed:
     * <ul>
     * <li>All candidates from {@code removedCandidates} are removed from {@link #filteredCandidatesStage1}.</li>
     * <li>The disjoint sequences containing all new candidates and adjacent to all removed candidates are computed.</li>
     * <li>The most probable candidate(s) from each contiguous sequence is/are determined. Those pass the filter and are
     * added to {@link #filteredCandidatesStage1}</li>
     * <li>All other candidates from those sequences do not pass the filter. All of those that are still in
     * {@link #filteredCandidatesStage1} are removed from {@link #filteredCandidatesStage1}.</li>
     * </ul>
     * @return a pair whose first element holds the set of candidates that now pass the filter and didn't before, and
     *         whose second element holds the set of candidates that did pass the filter before but don't anymore
     */
    Pair<Set<Candidate>, Set<Candidate>> updateCandidates(NavigableSet<Candidate> competitorCandidates,
            Iterable<Candidate> newCandidates, Iterable<Candidate> removedCandidates) {
        assert Util.containsAll(competitorCandidates, newCandidates); // all new candidates must be in competitorCandidates
        assert !competitorCandidates.stream().filter(c->Util.contains(removedCandidates, c)).findAny().isPresent(); // no removedCandidate must be in competitorCandidates
        final Set<Candidate> candidatesAddedToFiltered = new HashSet<>();
        final Set<Candidate> candidatesRemovedFromFiltered = new HashSet<>();
        // create a copy of newCandidates so we can remove objects from the copy as we add candidates to sequences;
        // this way we'll obtain disjoint sequences and don't need to consider candidates again which have already
        // been added to a sequence:
        final Set<Candidate> newCandidatesModifiableCopy = new HashSet<>();
        final Set<Candidate> removedCandidatesModifiableCopy = new HashSet<>();
        Util.addAll(newCandidates, newCandidatesModifiableCopy);
        Util.addAll(removedCandidates, removedCandidatesModifiableCopy);
        // If the start/end candidates are part of newCandidates and/or removedCandidates, pass them on as is;
        // they may have null TimePoints and cannot reasonably be considered by the algorithm implemented here:
        filterStartAndEndCandidates(newCandidatesModifiableCopy, removedCandidatesModifiableCopy, candidatesAddedToFiltered, candidatesRemovedFromFiltered);
        final Set<SortedSet<Candidate>> disjointSequencesAffectedByNewAndRemovedCandidates = new HashSet<>();
        while (!newCandidatesModifiableCopy.isEmpty()) {
            final Candidate nextNewCandidate = newCandidatesModifiableCopy.iterator().next();
            newCandidatesModifiableCopy.remove(nextNewCandidate);
            final NavigableSet<Candidate> contiguousSequenceForNextNewCandidate = getTimeWiseContiguousDistanceCandidates(competitorCandidates, nextNewCandidate, /* includeStartFrom */ true);
            disjointSequencesAffectedByNewAndRemovedCandidates.add(contiguousSequenceForNextNewCandidate);
            // remove the candidate grouped in the sequence from the remaining new candidates to consider
            // because they have been "scooped up" by this sequence already and need no further consideration:
            Util.removeAll(contiguousSequenceForNextNewCandidate, newCandidatesModifiableCopy);
            removeAllRemovedCandidatesInOrNearSequence(contiguousSequenceForNextNewCandidate, removedCandidatesModifiableCopy);
        }
        // Those candidates that were not yet removed from removedCandidatesModifiableCopy were not within or near
        // any of the sequences produced by new candidates. Checking their CANDIDATE_FILTER_TIME_WINDOW-neighborhood
        // and constructing the sequences affected by their removal.
        Util.addAll(removedCandidates, candidatesRemovedFromFiltered);
        candidatesRemovedFromFiltered.retainAll(filteredCandidates); // those explicitly removed and previously accepted by the filter go away
        while (!removedCandidatesModifiableCopy.isEmpty()) {
            final Candidate nextRemovedCandidate = removedCandidatesModifiableCopy.iterator().next();
            removedCandidatesModifiableCopy.remove(nextRemovedCandidate);
            final NavigableSet<Candidate> contiguousSequenceForNextRemovedCandidate = getTimeWiseContiguousDistanceCandidates(competitorCandidates, nextRemovedCandidate, /* includeStartFrom */ false);
            removeAllRemovedCandidatesInOrNearSequence(contiguousSequenceForNextRemovedCandidate, removedCandidatesModifiableCopy);
            // If adjacent sequences were added before and after the removed candidate,
            // we need to distinguish whether the gap around the removed candidate was greater
            // than the CANDIDATE_FILTER_TIME_WINDOW or not. If it was greater, we need to split
            // the sequence into two:
            Util.addAll(splitIfGapAroundRemovedCandidateIsTooLarge(contiguousSequenceForNextRemovedCandidate, nextRemovedCandidate),
                    disjointSequencesAffectedByNewAndRemovedCandidates);
        }
        // Now find the most probable candidates in the sequences affected by new/removed candidates
        for (final SortedSet<Candidate> contiguousCandidateSequence : disjointSequencesAffectedByNewAndRemovedCandidates) {
            findNewAndRemovedCandidates(contiguousCandidateSequence, candidatesAddedToFiltered, candidatesRemovedFromFiltered);
        }
        filteredCandidates.addAll(candidatesAddedToFiltered);
        filteredCandidates.removeAll(candidatesRemovedFromFiltered);
        return new Pair<>(candidatesAddedToFiltered, candidatesRemovedFromFiltered);
    }

    /**
     * The "core" of the first filter pass: The most probable candidates in the {@code contiguousCandidateSequence}
     * are determined. Within a small probability tolerance only the top probabilities pass the filter, assuming that
     * those are the usually equally probable candidates based on the same fix and distance, but for different occurrences
     * of the respective mark in the sequence of waypoints that defines the course.<p>
     * 
     * Once the filter result has been determined, we need to figure out how this <em>changes</em> the filter results.
     * For this, we may assume that {@link #filteredCandidatesStage1} has not yet been modified to reflect any changes during
     * this pass. Therefore, candidates added to the filter result can easily be identified because they are not yet
     * contained in {@link #filteredCandidatesStage1}. Candidates that did pass the filter but no longer do can be identified
     * based on the time range formed by {@code contiguousCandidateSequence}. Any candidate in {@link #filteredCandidatesStage1}
     * that is within this time range and is not part of the new filter result for the {@code contiguousCandidateSequence}
     * will have to be removed from the previous filter results.
     */
    private void findNewAndRemovedCandidates(SortedSet<Candidate> contiguousCandidateSequence, Set<Candidate> candidatesAddedToFiltered,
            Set<Candidate> candidatesRemovedFromFiltered) {
        if (!contiguousCandidateSequence.isEmpty()) {
            final SortedSet<Candidate> candidatesPreviouslyPassingFilter = filteredCandidates.subSet(contiguousCandidateSequence.first(),
                    /* fromInclusive */ true, contiguousCandidateSequence.last(), /* toInclusive */ true);
            ArrayList<Candidate> sortedByProbabilityFromLowToHigh = new ArrayList<>(contiguousCandidateSequence);
            Collections.sort(sortedByProbabilityFromLowToHigh, (c1, c2)->Double.compare(c1.getProbability(), c2.getProbability()));
            double maxProbability = sortedByProbabilityFromLowToHigh.get(sortedByProbabilityFromLowToHigh.size()-1).getProbability();
            Set<Candidate> candidatesAcceptedFromSequence = new HashSet<>();
            Candidate currentCandidate;
            for (int i=sortedByProbabilityFromLowToHigh.size()-1; i>=0 &&
                    (currentCandidate=sortedByProbabilityFromLowToHigh.get(i)).getProbability()+MAX_PROBABILITY_DELTA >= maxProbability; i--) {
                candidatesAcceptedFromSequence.add(currentCandidate);
                if (!candidatesPreviouslyPassingFilter.contains(currentCandidate)) {
                    candidatesAddedToFiltered.add(currentCandidate);
                }
            }
            for (final Candidate candidateThatPreviouslyPassedFilter : candidatesPreviouslyPassingFilter) {
                if (!candidatesAcceptedFromSequence.contains(candidateThatPreviouslyPassedFilter)) {
                    candidatesRemovedFromFiltered.add(candidateThatPreviouslyPassedFilter);
                }
            }
        }
    }

    /**
     * A singleton containing {@code contiguousSequenceForNextRemovedCandidate} will be returned,
     * unless it contains candidates before and after {@code nextRemovedCandidate} such that
     * the smallest gap spanning {@code nextRemovedCandidate} exceeds {@link #CANDIDATE_FILTER_TIME_WINDOW}.
     * In the latter case, the sequence is split into two at the time point of {@code nextRemovedCandidate}.
     */
    private Iterable<SortedSet<Candidate>> splitIfGapAroundRemovedCandidateIsTooLarge(
            NavigableSet<Candidate> contiguousSequenceForNextRemovedCandidate, Candidate nextRemovedCandidate) {
        Iterable<SortedSet<Candidate>> result;
        final Candidate lastBefore = contiguousSequenceForNextRemovedCandidate.lower(nextRemovedCandidate);
        if (lastBefore != null) {
            final Candidate firstAfter = contiguousSequenceForNextRemovedCandidate.higher(nextRemovedCandidate);
            if (firstAfter != null && lastBefore.getTimePoint().until(firstAfter.getTimePoint()).compareTo(CANDIDATE_FILTER_TIME_WINDOW) > 0) {
                // split:
                result = Arrays.asList(contiguousSequenceForNextRemovedCandidate.headSet(nextRemovedCandidate),
                        contiguousSequenceForNextRemovedCandidate.tailSet(nextRemovedCandidate, /* inclusive */ false));
            } else {
                result = Collections.singleton(contiguousSequenceForNextRemovedCandidate);
            }
        } else {
            result = Collections.singleton(contiguousSequenceForNextRemovedCandidate);
        }
        return result;
    }

    private void removeAllRemovedCandidatesInOrNearSequence(NavigableSet<Candidate> contiguousSequenceForNextNewCandidate,
            Set<Candidate> removedCandidatesModifiableCopy) {
        if (!contiguousSequenceForNextNewCandidate.isEmpty()) {
            final TimeRange sequenceTimeRange = new TimeRangeImpl(
                    contiguousSequenceForNextNewCandidate.first().getTimePoint(), contiguousSequenceForNextNewCandidate.last().getTimePoint());
            for (final Iterator<Candidate> removedCandidateIter = removedCandidatesModifiableCopy.iterator(); removedCandidateIter.hasNext(); ) {
                final Candidate removedCandidate = removedCandidateIter.next();
                if (sequenceTimeRange.timeDifference(removedCandidate.getTimePoint()).compareTo(CANDIDATE_FILTER_TIME_WINDOW) <= 0) {
                    removedCandidateIter.remove();
                }
            }
        }
    }

    /**
     * Starting at {@code startFrom}, looks into earlier and later candidates in the {@code candidates} set and adds all
     * candidates that continue to be no more than {@link #CANDIDATE_FILTER_TIME_WINDOW} away from their adjacent
     * candidate. This way, in the resulting set, the time difference between any two adjacent fixes is no more than
     * {@link #CANDIDATE_FILTER_TIME_WINDOW}. The result will always at least contain {@code startFrom}.
     * 
     * @param includeStartFrom
     *            whether or not to include {@code startFrom} in the resulting set
     */
    private NavigableSet<Candidate> getTimeWiseContiguousDistanceCandidates(NavigableSet<Candidate> competitorCandidates, Candidate startFrom, boolean includeStartFrom) {
        final NavigableSet<Candidate> result = new TreeSet<>(candidateComparator);
        if (includeStartFrom) {
            result.add(startFrom);
        }
        addContiguousCandidates(competitorCandidates.descendingSet().tailSet(startFrom), result);
        addContiguousCandidates(competitorCandidates.tailSet(startFrom), result);
        return result;
    }

    /**
     * The first element in the iteration order of {@code candidates} will not be added to {@code addTo}
     * but serves as the first element to compute distance to. The {@link #start} and {@link #end} proxy
     * candidates are ignored by this method and will never be added to {@code addTo}.
     */
    private void addContiguousCandidates(Iterable<Candidate> candidates, Collection<Candidate> addTo) {
        final Iterator<Candidate> iter = candidates.iterator();
        if (iter.hasNext()) {
            Candidate current = iter.next();
            while (iter.hasNext()) {
                final Candidate next = iter.next();
                if (next != startProxyCandidate && next != endProxyCandidate) {
                    if (next.getTimePoint().until(current.getTimePoint()).abs().compareTo(CANDIDATE_FILTER_TIME_WINDOW) <= 0) {
                        addTo.add(next);
                        current = next;
                    } else {
                        break; // too large a gap; end of contiguous sequence in this direction found
                    }
                }
            }
        }
    }

    /**
     * The {@link #start} and {@link #end} proxy candidates are moved from {@link #newCandidatesModifiableCopy} to
     * {@link #candidatesAddedToFiltered} and from {@link #removedCandidatesModifiableCopy} to
     * {@link #candidatesRemovedFromFiltered}, respectively.
     */
    private void filterStartAndEndCandidates(Set<Candidate> newCandidatesModifiableCopy,
            Set<Candidate> removedCandidatesModifiableCopy, Set<Candidate> candidatesAddedToFiltered,
            Set<Candidate> candidatesRemovedFromFiltered) {
        for (final Candidate startAndEnd : Arrays.asList(startProxyCandidate, endProxyCandidate)) {
            if (newCandidatesModifiableCopy.remove(startAndEnd)) {
                candidatesAddedToFiltered.add(startAndEnd);
            }
            if (removedCandidatesModifiableCopy.remove(startAndEnd)) {
                candidatesRemovedFromFiltered.add(startAndEnd);
            }
        }
    }

    Iterable<Candidate> getFilteredCandidates() {
        return filteredCandidates;
    }

}
