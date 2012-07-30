package com.sap.sailing.resultimport;


public interface CompetitorRow {
    Iterable<CompetitorEntry> getRankAndMaxPointsReasonAndPointsAndDiscarded();

    Double getTotalPointsBeforeDiscarding();

    Double getScoreAfterDiscarding();

    Iterable<String> getNames();

    String getTeamName();

    String getSailID();

    Integer getTotalRank();
    
    String getClubName();
}
