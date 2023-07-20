package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.leaderboard.impl.LowPointWithEliminatingMedalSeriesPromotingOneToFinalAndTwoToSemifinal;

public class LowPointWithEliminatingMedalSeriesPromotingOneToFinalAndTwoToSemifinalTest {
    @Test
    public void testCountingPromotedCompetitors() {
        final LowPointWithEliminatingMedalSeriesPromotingOneToFinalAndTwoToSemifinal scoringScheme = new LowPointWithEliminatingMedalSeriesPromotingOneToFinalAndTwoToSemifinal();
        final RaceColumnInSeries quarterFinal = mock(RaceColumnInSeries.class);
        final RaceColumnInSeries semiFinal = mock(RaceColumnInSeries.class);
        final RaceColumnInSeries grandFinal = mock(RaceColumnInSeries.class);
        final Series medalSeries = mock(Series.class);
        final Iterable<RaceColumnInSeries> medalSeriesColumns = Arrays.asList(quarterFinal, semiFinal, grandFinal);
        when(medalSeries.getRaceColumns()).thenAnswer(new Answer<Iterable<RaceColumnInSeries>>() {
            @Override
            public Iterable<RaceColumnInSeries> answer(InvocationOnMock invocation) throws Throwable {
                return medalSeriesColumns;
            }
        });
        when(quarterFinal.getSeries()).thenReturn(medalSeries);
        when(semiFinal.getSeries()).thenReturn(medalSeries);
        when(grandFinal.getSeries()).thenReturn(medalSeries);
        assertEquals(0, scoringScheme.getNumberOfCompetitorsBetterThanThoseSailingInRace(grandFinal));
        assertEquals(1, scoringScheme.getNumberOfCompetitorsBetterThanThoseSailingInRace(semiFinal));
        assertEquals(3, scoringScheme.getNumberOfCompetitorsBetterThanThoseSailingInRace(quarterFinal));
    }
}
