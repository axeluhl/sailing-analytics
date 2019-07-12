package com.sap.sailing.domain.leaderboard.impl;

import java.util.Comparator;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sse.common.Util.Pair;

public class LowPointTieBreakBasedOnLastSeriesOnly extends LowPoint {
    private static final long serialVersionUID = -2837189757937229157L;

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.LOW_POINT_TIE_BREAK_BASED_ON_LAST_SERIES_ONLY;
    }

    /**
     * Obtains a comparator that compares two non-discarded, non medal-race scores according the rule 21.9:
     * <p>
     * 
     * <em>"21.9 RRS A8 is changed as follows: 21.9.1 For boats assigned to compete in a medal race, ties in the regatta
     * score will be broken by the medal race score. This changes RRS A8. 21.9.2 For tied boats with the same points
     * score in the medal race, ties will be broken applying RRS A8 to the opening series scores. 21.9.3 For boats that
     * sailed a Qualifying Series and a Final Series, ties will be broken using A8 but using the scores in the Final
     * Series before using any scores in the Qualifying Series when applying A8.1. This applies also to boats that were
     * tied after the medal race and had the same score in the medal race."</em>
     * <p>
     * 
     * For this, the position of the series in the regatta is determined, and if one score comes from the last non-medal
     * series and the other does not, the one from the last non-medal series is considered better. Otherwise, the
     * {@link #getScoreComparator(boolean) score comparator} is used to compare the scores numerically.
     */
    @Override
    protected Comparator<Pair<RaceColumn, Double>> getRuleA8_1ScoreComparator(boolean nullScoresAreBetter) {
        return (Pair<RaceColumn, Double> p1, Pair<RaceColumn, Double> p2)->{
            final int result;
            if (isFromLastNonMedalSeries(p1) != isFromLastNonMedalSeries(p2)) {
                result = isFromLastNonMedalSeries(p1) ? -1 // p1 is better, so less, than p2
                        : 1; // p1 is worse, so greater, than p2
            } else {
                result = getScoreComparator(nullScoresAreBetter).compare(p1.getB(), p2.getB());
            }
            return result;
        };
    }

    private boolean isFromLastNonMedalSeries(Pair<RaceColumn, Double> raceColumnAndNetPoints) {
        final RaceColumn raceColumn = raceColumnAndNetPoints.getA();
        final boolean result;
        if (raceColumn instanceof RaceColumnInSeries) {
            final Series series = ((RaceColumnInSeries) raceColumn).getSeries();
            final Regatta regatta = series.getRegatta();
            boolean found = false;
            boolean lastNonMedal = true;
            for (final Series seriesInRegatta : regatta.getSeries()) {
                if (!found) {
                    if (seriesInRegatta == series) {
                        found = true;
                    }
                } else if (!seriesInRegatta.isMedal()) {
                    lastNonMedal = false; // already found, and seriesInRegatta is a non-medal series that comes after series
                }
            }
            assert found;
            result = lastNonMedal;
        } else {
            result = !raceColumn.isMedalRace(); 
        }
        return result;
    }
}
