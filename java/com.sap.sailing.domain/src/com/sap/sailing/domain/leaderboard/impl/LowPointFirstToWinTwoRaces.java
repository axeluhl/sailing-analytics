package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.ScoringScheme;

/**
 * This class is a derivation of the LowPointScoring scheme. The following additional rules apply:
 * <ol>
 * <li>The first participant in the medal series that wins {@link #getTargetAmountOfMedalRaceWins()} races is ranked as
 * the first.</li>
 * <li>Having 1 point in the carry forward column of the medal series explicitly counts as an additional win which
 * requires the winner of the qualification to only need one additional win in the medals.</li>
 * <li>If none of the participants in the medal series reached the target amount of 2 wins yet, the ranking is done
 * based on the points only.</li>
 * <li>If there is a carry forward column in the medal series, the carry forward points are used as the primary tie
 * breaking criteria in case two participants in the medal series have the same overall score.</li>
 * <li>If there is no carry forward column in the medal series or the carry forward score is not sufficient, the points
 * of the last medal race are used as a secondary tie breaking criteria.</li>
 * <li>This Scheme will change the default scoring factor for medal races to 1, usually 2 is used (see
 * {@link ScoringScheme#DEFAULT_MEDAL_RACE_FACTOR})</li>
 * <ol>
 */
public class LowPointFirstToWinTwoRaces extends LowPoint {
    private static final long serialVersionUID = 7072175334160798617L;

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.LOW_POINT_FIRST_TO_WIN_TWO_RACES;
    }

    @Override
    public boolean isMedalWinAmountCriteria() {
        return true;
    }
    
    @Override
    public boolean isCarryForwardInMedalsCriteria() {
        return true;
    }
    
    @Override
    public boolean isLastMedalRaceCriteria() {
        return true;
    }
    
    /**
     * If {@link #isMedalWinAmountCriteria()} returns {@code true}, this will be the amount of races that must be won,
     * in order to win the medal series instantly
     */
    private int getTargetAmountOfMedalRaceWins() {
        return 2;
    }
    
    @Override
    public double getScoreFactor(RaceColumn raceColumn) {
        Double factor = raceColumn.getExplicitFactor();
        if (factor == null) {
            factor = 1.0;
        }
        return factor;
    }

    /**
     * If one of the competitors reached the target of races
     * won (defined by {@link ScoringScheme#getTargetAmountOfMedalRaceWins()} it is ranked better than the other. If
     * none of the competitors reached the target, they are ranked equally even if one has more wins. This is because
     * only reaching the exact number of wins counts for this criteria but not the general comparison by the number of
     * wins. If both competitors exactly reached the target, they are also ranked equally (this can e.g. occur while
     * entering score corrections).
     */
    @Override
    public int compareByMedalRacesWon(int numberOfMedalRacesWonO1, int numberOfMedalRacesWonO2) {
        final int result;
        final int targetAmount = getTargetAmountOfMedalRaceWins();
        if (numberOfMedalRacesWonO1 >= targetAmount || numberOfMedalRacesWonO2 >= targetAmount) {
            result = Integer.compare(numberOfMedalRacesWonO2, numberOfMedalRacesWonO1);
        } else {
            result = 0;
        }
        return result;
    }
}
