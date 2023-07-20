package com.sap.sailing.domain.leaderboard.impl;

import java.util.List;
import java.util.ListIterator;

import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sse.common.Util;

public abstract class LowPointWithEliminatingMedalSeriesWithPromotions extends LowPoint {
    private static final long serialVersionUID = 7759999270911627798L;
    
    /**
     * The last index for this array refers to the last medal race; e.g., the "Grand Final" race
     * in an iQFOil regatta with the medal series consisting of a "Quarter Final," a "Semi Final"
     * and a "Grand Final". The last-but-one index in the example would then refer to the Semi Final,
     * and so on. The array may be empty, meaning that no competitor is promoted into any medal
     * race. The field always has to refer to a valid array.
     */
    private final int[] numberOfPromotedCompetitorsIntoLastMedalRaces;

    /**
     * 
     * @param numberOfPromotedCompetitorsIntoLastMedalRaces
     *            The last index for this array refers to the last medal race; e.g., the "Grand Final" race in an iQFOil
     *            regatta with the medal series consisting of a "Quarter Final," a "Semi Final" and a "Grand Final". The
     *            last-but-one index in the example would then refer to the Semi Final, and so on. The array may be
     *            empty, meaning that no competitor is promoted into any medal race. The field always has to refer to a
     *            valid array.
     */
    public LowPointWithEliminatingMedalSeriesWithPromotions(int[] numberOfPromotedCompetitorsIntoLastMedalRaces) {
        super();
        this.numberOfPromotedCompetitorsIntoLastMedalRaces = numberOfPromotedCompetitorsIntoLastMedalRaces;
    }
    
    @Override
    public ScoringSchemeType getType() {
        return super.getType();
    }

    public int getNumberOfCompetitorsBetterThanThoseSailingInRace(RaceColumnInSeries medalRace) {
        final List<? extends RaceColumnInSeries> allMedalRacesInSeries = Util.asList(medalRace.getSeries().getRaceColumns());
        int result = 0;
        int indexInNumberOfPromotedCompetitorsIntoLastMedalRaces = numberOfPromotedCompetitorsIntoLastMedalRaces.length-1;
        if (allMedalRacesInSeries.size() > 1) {
            for (final ListIterator<? extends RaceColumnInSeries> i=allMedalRacesInSeries.listIterator(allMedalRacesInSeries.size()); i.hasPrevious() && i.previous() != medalRace; ) {
                result += numberOfPromotedCompetitorsIntoLastMedalRaces[indexInNumberOfPromotedCompetitorsIntoLastMedalRaces--];
            }
        }
        return result;
    }

    @Override
    public int compareByMedalRaceParticipation(int zeroBasedIndexOfLastMedalSeriesInWhichO1Scored,
            int zeroBasedIndexOfLastMedalSeriesInWhichO2Scored) {
        // TODO We'd like to consider those as "having participated" in a medal race that may not have a score but have been promoted already to a later medal race based on their opening series result;
        // For that we need to check all medal race columns and need to know the two competitors.
        // If promoted to a later medal race but not sailed in the one currently considered, consider as "has participated."
        // If both participated, move on to next medal race column, if any. If last medal race column, return 0.
        // If one competitor is considered as "has participated" and the other one not, prefer the one that "has participated" over the one that has not.
        return super.compareByMedalRaceParticipation(zeroBasedIndexOfLastMedalSeriesInWhichO1Scored, zeroBasedIndexOfLastMedalSeriesInWhichO2Scored);
    }
}
