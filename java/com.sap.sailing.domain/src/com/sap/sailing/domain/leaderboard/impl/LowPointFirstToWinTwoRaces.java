package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.ScoringSchemeType;

/**
 * This class is a derivation of the LowPointScoring scheme. The first participant in the medal series, that wins two
 * races is ranked as the first. The participant that has the score 1 in the preSeries carry column only must reach one
 * additional victory. This Scheme will change the default scoring factor for medal races to 1, usually 2 is used
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
    
    @Override
    public int getTargetAmountOfMedalRaceWins() {
        return 2;
    }
    
    @Override
    public double getScoreFactor(RaceColumn a) {
        Double factor = a.getExplicitFactor();
        if(factor == null) {
            factor = 1.0;
        }
        return factor;
    }
}
