package com.sap.sailing.domain.markpassingcalculation.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.markpassingcalculation.Candidate;
import com.sap.sailing.domain.tracking.impl.TimedComparator;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class MostProbableCandidatesInSmallTimeRangeFilterTest {
    private Candidate c1, c2, c3, c4, c5, c6, c7, c8, c9, c10;
    private NavigableSet<Candidate> competitorCandidates;
    private MostProbableCandidatesInSmallTimeRangeFilter filter;
    
    @Before
    public void setUp() {
        final TimePoint now = MillisecondsTimePoint.now();
        final Candidate start = new CandidateImpl(0, now.minus(Duration.ONE_HOUR), 1.0, null);
        final Candidate end = new CandidateImpl(6, now.plus(Duration.ONE_HOUR), 1.0, null);
        final Comparator<Candidate> candidateComparator = (c1, c2)->TimedComparator.INSTANCE.compare(c1, c2);
        filter = new MostProbableCandidatesInSmallTimeRangeFilter(
                candidateComparator, start, end);
        competitorCandidates = new TreeSet<>(candidateComparator);
        final Duration gapless = MostProbableCandidatesInSmallTimeRangeFilter.CANDIDATE_FILTER_TIME_WINDOW.divide(2);
        c1 = candidate(now, "c1");
        c2 = candidate(c1.getTimePoint().plus(gapless), "c2");
        c3 = candidate(c2.getTimePoint().plus(gapless), "c3");
        c4 = candidate(c3.getTimePoint().plus(gapless), "c4");
        c5 = candidate(c4.getTimePoint().plus(gapless), "c5");
        // add a cap here that is greater than the threshold but not twice as long;
        // this way, adding one in the middle of this gap is the interesting case
        c6 = candidate(c5.getTimePoint().plus(MostProbableCandidatesInSmallTimeRangeFilter.CANDIDATE_FILTER_TIME_WINDOW.times(1.5)), "c6");
        c7 = candidate(c6.getTimePoint().plus(gapless), "c7");
        c8 = candidate(c7.getTimePoint().plus(gapless), "c8");
        c9 = candidate(c8.getTimePoint().plus(gapless), "c9");
        c10= candidate(c9.getTimePoint().plus(gapless), "c10");
        competitorCandidates.add(c1);
        competitorCandidates.add(c2);
        competitorCandidates.add(c3);
        competitorCandidates.add(c4);
        competitorCandidates.add(c5);
        competitorCandidates.add(c6);
        competitorCandidates.add(c7);
        competitorCandidates.add(c8);
        competitorCandidates.add(c9);
        competitorCandidates.add(c10);
    }

    @Test
    public void testGetTimeWiseContiguousDistanceCandidates() {
        final NavigableSet<Candidate> result = filter.getTimeWiseContiguousDistanceCandidates(competitorCandidates, c3, /* includeStartFrom */ true);
        assertContains(result, c1, c2, c3, c4, c5);
    }
    
    @Test
    public void testGetTimeWiseContiguousDistanceCandidatesOnRightBorder() {
        final NavigableSet<Candidate> result = filter.getTimeWiseContiguousDistanceCandidates(competitorCandidates, c5, /* includeStartFrom */ true);
        assertContains(result, c1, c2, c3, c4, c5);
    }
    
    @Test
    public void testGetTimeWiseContiguousDistanceCandidatesOnLeftBorder() {
        final NavigableSet<Candidate> result = filter.getTimeWiseContiguousDistanceCandidates(competitorCandidates, c6, /* includeStartFrom */ true);
        assertContains(result, c6, c7, c8, c9, c10);
    }
    
    @Test
    public void testGetTimeWiseContiguousDistanceCandidatesInMiddleOfGapNotInserted() {
        final Candidate middleOfGap = candidate(c5.getTimePoint().plus(c5.getTimePoint().until(c6.getTimePoint()).divide(2)), "gap");
        final NavigableSet<Candidate> result = filter.getTimeWiseContiguousDistanceCandidates(competitorCandidates, middleOfGap,
                /* includeStartFrom */ true);
        assertContains(result, c1, c2, c3, c4, c5, middleOfGap, c6, c7, c8, c9, c10);
    }
    
    @Test
    public void testGetTimeWiseContiguousDistanceCandidatesInMiddleOfGapNotInsertedDontIncludeStartFrom() {
        final Candidate middleOfGap = candidate(c5.getTimePoint().plus(c5.getTimePoint().until(c6.getTimePoint()).divide(2)), "gap");
        final NavigableSet<Candidate> result = filter.getTimeWiseContiguousDistanceCandidates(competitorCandidates, middleOfGap,
                /* includeStartFrom */ false);
        assertContains(result, c1, c2, c3, c4, c5, c6, c7, c8, c9, c10);
    }
    
    @Test
    public void testGetTimeWiseContiguousDistanceCandidatesInMiddleOfGapInserted() {
        final Candidate middleOfGap = candidate(c5.getTimePoint().plus(c5.getTimePoint().until(c6.getTimePoint()).divide(2)), "gap");
        competitorCandidates.add(middleOfGap);
        final NavigableSet<Candidate> result = filter.getTimeWiseContiguousDistanceCandidates(competitorCandidates, middleOfGap,
                /* includeStartFrom */ true);
        assertContains(result, c1, c2, c3, c4, c5, middleOfGap, c6, c7, c8, c9, c10);
    }
    
    @Test
    public void testGetTimeWiseContiguousDistanceCandidatesInMiddleOfWidenedGap() {
        competitorCandidates.remove(c5);
        competitorCandidates.remove(c6);
        final Candidate middleOfGap = candidate(c5.getTimePoint().plus(c5.getTimePoint().until(c6.getTimePoint()).divide(2)), "gap");
        final NavigableSet<Candidate> result = filter.getTimeWiseContiguousDistanceCandidates(competitorCandidates, middleOfGap,
                /* includeStartFrom */ true);
        assertContains(result, middleOfGap);
    }
    
    private void assertContains(NavigableSet<Candidate> candidates, Candidate... candidatesExpected) {
        assertEquals(candidatesExpected.length, candidates.size());
        for (final Candidate candidateExpected : candidatesExpected) {
            assertTrue(candidates.contains(candidateExpected));
        }
    }

    private Candidate candidate(TimePoint timePoint, String name) {
        return new CandidateImpl(1, timePoint, 1.0, null) {
            private static final long serialVersionUID = 8547646939054562751L;
            @Override public String toString() { return name; }
        };
    }
}
