package com.sap.sailing.domain.masterdataimport;

public class SingleScoreCorrectionMasterData {

    private String competitorId;
    private Double explicitScoreCorrection;
    private String maxPointsReason;

    public SingleScoreCorrectionMasterData(String competitorId, Double explicitScoreCorrection, String maxPointsReason) {
        this.competitorId = competitorId;
        this.explicitScoreCorrection = explicitScoreCorrection;
        this.maxPointsReason = maxPointsReason;
    }

    public String getCompetitorId() {
        return competitorId;
    }

    public Double getExplicitScoreCorrection() {
        return explicitScoreCorrection;
    }

    public String getMaxPointsReason() {
        return maxPointsReason;
    }

}
