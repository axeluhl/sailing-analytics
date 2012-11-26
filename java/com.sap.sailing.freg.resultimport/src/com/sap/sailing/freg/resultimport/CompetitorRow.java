package com.sap.sailing.freg.resultimport;


public interface CompetitorRow {

    Iterable<CompetitorEntry> getRankAndMaxPointsReasonAndPointsAndDiscarded();

    Double getTotalPointsBeforeDiscarding();

    Double getScoreAfterDiscarding();

    Iterable<String> getNames();
    
    /**
     * Concatenates the {@link #getNames() names} with a "+"
     */
    String getTeamName();

    String getSailID();

    Integer getTotalRank();
    
    String getClubName();

}
