package com.sap.sailing.freg.resultimport.impl;

import java.util.List;

import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.freg.resultimport.CompetitorRow;

public class CompetitorRowImpl implements CompetitorRow {
    private final Integer totalRank;
    private final String sailID;
    private final List<String> names;
    private final Double scoreAfterDiscarding;
    private final Double totalPointsBeforeDiscarding;
    private final List<Triple<Integer, String, Pair<Double, Boolean>>> rankAndMaxPointsReasonAndPointsAndDiscarded;
    
    public CompetitorRowImpl(Integer totalRank, String sailID, List<String> names, Double scoreAfterDiscarding,
            Double totalPointsBeforeDiscarding,
            List<Triple<Integer, String, Pair<Double, Boolean>>> rankAndMaxPointsReasonAndPointsAndDiscarded) {
        super();
        this.totalRank = totalRank;
        this.sailID = sailID;
        this.names = names;
        this.scoreAfterDiscarding = scoreAfterDiscarding;
        this.totalPointsBeforeDiscarding = totalPointsBeforeDiscarding;
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
    public List<String> getNames() {
        return names;
    }

    @Override
    public Double getScoreAfterDiscarding() {
        return scoreAfterDiscarding;
    }

    @Override
    public Double getTotalPointsBeforeDiscarding() {
        return totalPointsBeforeDiscarding;
    }

    @Override
    public List<Triple<Integer, String, Pair<Double, Boolean>>> getRankAndMaxPointsReasonAndPointsAndDiscarded() {
        return rankAndMaxPointsReasonAndPointsAndDiscarded;
    }
    
}
