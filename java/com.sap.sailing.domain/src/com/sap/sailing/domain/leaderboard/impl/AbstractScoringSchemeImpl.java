package com.sap.sailing.domain.leaderboard.impl;

import java.io.Serializable;
import java.util.Comparator;

import com.sap.sailing.domain.leaderboard.ScoringScheme;

public abstract class AbstractScoringSchemeImpl implements ScoringScheme {
    private static final long serialVersionUID = 6830414905539642446L;
    
    private class ScoreComparator implements Comparator<Double>, Serializable {
        private static final long serialVersionUID = -2767385186133743330L;

        @Override
        public int compare(Double o1, Double o2) {
            return Double.valueOf(o1).compareTo(Double.valueOf(o2)) * (isHigherBetter() ? -1 : 1);
        }
    }
    
    private final boolean higherIsBetter;
    
    public AbstractScoringSchemeImpl(boolean higherIsBetter) {
        this.higherIsBetter = higherIsBetter;
    }    

    @Override
    public boolean isHigherBetter() {
        return higherIsBetter;
    }

    @Override
    public Comparator<Double> getScoreComparator() {
        return new ScoreComparator();
    }
}
