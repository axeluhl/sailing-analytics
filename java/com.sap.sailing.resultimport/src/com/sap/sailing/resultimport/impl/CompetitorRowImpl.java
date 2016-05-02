package com.sap.sailing.resultimport.impl;

import com.sap.sailing.resultimport.CompetitorEntry;
import com.sap.sailing.resultimport.CompetitorRow;

public class CompetitorRowImpl implements CompetitorRow {
    private final Integer totalRank;
    private final String sailID;
    private final Iterable<String> names;
    private final Double scoreAfterDiscarding;
    private final Double realTotalPointsBeforeDiscarding;
    private final Iterable<CompetitorEntry> rankAndMaxPointsReasonAndPointsAndDiscarded;
    
    public CompetitorRowImpl(Integer totalRank, String sailID, Iterable<String> names, Double scoreAfterDiscarding,
            Double realTotalPointsBeforeDiscarding, Iterable<CompetitorEntry> rankAndMaxPointsReasonAndPointsAndDiscarded) {
        super();
        this.totalRank = totalRank;
        this.sailID = sailID;
        this.names = names;
        this.scoreAfterDiscarding = scoreAfterDiscarding;
        this.realTotalPointsBeforeDiscarding = realTotalPointsBeforeDiscarding;
        this.rankAndMaxPointsReasonAndPointsAndDiscarded = rankAndMaxPointsReasonAndPointsAndDiscarded;
    }

    @Override
    public Integer getTotalRank() {
        return totalRank;
    }

    @Override
    public String getSailID() {
        return sailID;
    }

    @Override
    public Iterable<String> getNames() {
        return names;
    }

    @Override
    public Double getScoreAfterDiscarding() {
        return scoreAfterDiscarding;
    }

    @Override
    public Double getRealTotalPointsBeforeDiscarding() {
        return realTotalPointsBeforeDiscarding;
    }

    @Override
    public Iterable<CompetitorEntry> getRankAndMaxPointsReasonAndPointsAndDiscarded() {
        return rankAndMaxPointsReasonAndPointsAndDiscarded;
    }

    @Override
    public String getCompetitorName() {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (String name : getNames()) {
            if (first) {
                first = false;
            } else {
                result.append("+");
            }
            result.append(name);
        }
        return result.toString();
    }
}
