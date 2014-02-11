package com.sap.sailing.domain.masterdataimport;

import java.io.Serializable;

public class SingleScoreCorrectionMasterData {

    private Serializable competitorId;
    private Double explicitScoreCorrection;
    private String maxPointsReason;

    public SingleScoreCorrectionMasterData(Serializable competitorId, Double explicitScoreCorrection,
            String maxPointsReason) {
        this.competitorId = competitorId;
        this.explicitScoreCorrection = explicitScoreCorrection;
        this.maxPointsReason = maxPointsReason;
    }

    public Serializable getCompetitorId() {
        return competitorId;
    }

    public Double getExplicitScoreCorrection() {
        return explicitScoreCorrection;
    }

    public String getMaxPointsReason() {
        return maxPointsReason;
    }

}
