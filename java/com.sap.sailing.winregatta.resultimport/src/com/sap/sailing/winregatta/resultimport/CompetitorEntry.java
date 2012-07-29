package com.sap.sailing.winregatta.resultimport;

public interface CompetitorEntry {

    boolean isDiscarded();

    Double getScore();

    String getMaxPointsReason();

    Integer getRank();

}
