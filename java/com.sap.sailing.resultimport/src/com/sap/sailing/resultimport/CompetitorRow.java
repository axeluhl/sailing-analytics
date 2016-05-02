package com.sap.sailing.resultimport;


public interface CompetitorRow {
    Iterable<CompetitorEntry> getRankAndMaxPointsReasonAndPointsAndDiscarded();

    Double getRealTotalPointsBeforeDiscarding();

    Double getScoreAfterDiscarding();

    Iterable<String> getNames();

    String getCompetitorName();

    String getSailID();

    Integer getTotalRank();
}
