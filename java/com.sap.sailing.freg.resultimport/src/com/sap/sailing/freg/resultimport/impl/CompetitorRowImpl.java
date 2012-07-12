package com.sap.sailing.freg.resultimport.impl;

import java.util.List;

import com.sap.sailing.freg.resultimport.CompetitorEntry;
import com.sap.sailing.freg.resultimport.CompetitorRow;

public class CompetitorRowImpl implements CompetitorRow {
    private final Integer totalRank;
    private final String sailID;
    private final List<String> names;
    private final Double scoreAfterDiscarding;
    private final Double totalPointsBeforeDiscarding;
    private final List<CompetitorEntry> rankAndMaxPointsReasonAndPointsAndDiscarded;
    private final String clubName;
    
    public CompetitorRowImpl(Integer totalRank, String sailID, List<String> names, Double scoreAfterDiscarding,
            Double totalPointsBeforeDiscarding, List<CompetitorEntry> rankAndMaxPointsReasonAndPointsAndDiscarded, String clubName) {
        super();
        this.totalRank = totalRank;
        this.sailID = sailID;
        this.names = names;
        this.scoreAfterDiscarding = scoreAfterDiscarding;
        this.totalPointsBeforeDiscarding = totalPointsBeforeDiscarding;
        this.rankAndMaxPointsReasonAndPointsAndDiscarded = rankAndMaxPointsReasonAndPointsAndDiscarded;
        this.clubName = clubName;
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
    public List<CompetitorEntry> getRankAndMaxPointsReasonAndPointsAndDiscarded() {
        return rankAndMaxPointsReasonAndPointsAndDiscarded;
    }

    @Override
    public String getClubName() {
        return clubName;
    }
    
}
