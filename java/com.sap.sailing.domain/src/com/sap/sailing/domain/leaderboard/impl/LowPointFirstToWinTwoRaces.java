package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.ScoringSchemeType;

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
    public boolean doesCountAsWinInMedalRace(Double score, RaceColumn raceColumn) {
        if(score != null) {
            double minScore = 1 * getScoreFactor(raceColumn);
            return score == minScore;
        } else {
            return false;
        }
    }
    
    @Override
    public double getScoreFactor(RaceColumn a) {
        Double factor = a.getExplicitFactor();
        if(factor == null) {
            factor = 1.0;
        }
        return factor;
    }
    
    @Override
    public double getTargetAmountOfMedalRaceWinsScore() {
        return 2.0;
    }
    
}
