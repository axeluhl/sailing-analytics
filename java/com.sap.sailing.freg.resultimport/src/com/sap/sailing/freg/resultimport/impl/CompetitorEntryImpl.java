package com.sap.sailing.freg.resultimport.impl;

import com.sap.sailing.freg.resultimport.CompetitorEntry;

public class CompetitorEntryImpl implements CompetitorEntry {
    private final Integer rank;
    private final String maxPointsReason;
    private final Double score;
    private final boolean discarded;

    public CompetitorEntryImpl(Integer rank, String maxPointsReason, Double score, boolean discarded) {
        super();
        this.rank = rank;
        this.maxPointsReason = maxPointsReason;
        this.score = score;
        this.discarded = discarded;
    }

    @Override
    public Integer getRank() {
        return rank;
    }

    @Override
    public String getMaxPointsReason() {
        return maxPointsReason;
    }

    @Override
    public Double getScore() {
        return score;
    }

    @Override
    public boolean isDiscarded() {
        return discarded;
    }
}
