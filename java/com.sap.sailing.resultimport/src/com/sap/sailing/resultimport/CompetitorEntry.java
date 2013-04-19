package com.sap.sailing.resultimport;

public interface CompetitorEntry {
    boolean isDiscarded();

    Double getScore();

    String getMaxPointsReason();

    Integer getRank();
}
