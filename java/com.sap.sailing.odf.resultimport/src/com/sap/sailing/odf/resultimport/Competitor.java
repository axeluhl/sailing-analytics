package com.sap.sailing.odf.resultimport;

import com.sap.sailing.domain.common.CountryCode;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.odf.resultimport.CumulativeResult.CompetitorType;

public interface Competitor {

    CompetitorType getCompetitorType();

    CountryCode getCountryCode();

    Double getPointsInMedalRace();

    Iterable<Triple<Double, Integer, MaxPointsReason>> getPointsAndRanksAndMaxPointsAfterEachRace();
}
