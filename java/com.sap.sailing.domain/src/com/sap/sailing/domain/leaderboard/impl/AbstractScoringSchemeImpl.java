package com.sap.sailing.domain.leaderboard.impl;

import java.io.Serializable;
import java.util.Comparator;

import com.sap.sailing.domain.leaderboard.ScoringScheme;

public abstract class AbstractScoringSchemeImpl implements ScoringScheme {
    private static final long serialVersionUID = 6830414905539642446L;
    
    private class ScoreComparator implements Comparator<Double>, Serializable {
        private static final long serialVersionUID = -2767385186133743330L;
        
        private final boolean nullScoresAreBetter;
        
        public ScoreComparator(boolean nullScoresAreBetter) {
            this.nullScoresAreBetter = nullScoresAreBetter;
        }

        @Override
        public int compare(Double o1, Double o2) {
            // null means did not enlist in the race or race hasn't started for that competitor yet; null
            // sorts "worse" than non-null.
            int result;
            if (o1 == null) {
                if (o2 == null) {
                    result = 0;
                } else {
                    result = nullScoresAreBetter ? -1 : 1;
                }
            } else {
                if (o2 == null) {
                    result = nullScoresAreBetter ? 1 : -1;
                } else {
                    result = o1.compareTo(o2) * (isHigherBetter() ? -1 : 1);
                }
            }
            return result;
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
    public Comparator<Double> getScoreComparator(boolean nullScoresAreBetter) {
        return new ScoreComparator(nullScoresAreBetter);
    }
}
