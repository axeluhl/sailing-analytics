package com.sap.sailing.freg.resultimport;

import java.util.List;

public interface CompetitorRow {

    List<CompetitorEntry> getRankAndMaxPointsReasonAndPointsAndDiscarded();

    Double getTotalPointsBeforeDiscarding();

    Double getScoreAfterDiscarding();

    List<String> getNames();

    String getSailID();

    Integer getTotalRank();

}
