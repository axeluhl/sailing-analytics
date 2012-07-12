package com.sap.sailing.freg.resultimport;

import java.util.List;

import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;

public interface CompetitorRow {

    List<Triple<Integer, String, Pair<Double, Boolean>>> getRankAndMaxPointsReasonAndPointsAndDiscarded();

    Double getTotalPointsBeforeDiscarding();

    Double getScoreAfterDiscarding();

    List<String> getNames();

    String getSailID();

    Integer getTotalRank();

}
