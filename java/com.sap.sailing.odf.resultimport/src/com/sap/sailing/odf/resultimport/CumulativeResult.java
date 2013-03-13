package com.sap.sailing.odf.resultimport;

import com.sap.sailing.domain.common.CountryCode;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.impl.Util.Triple;

public interface CumulativeResult {
    public static enum CompetitorType { A /* athlete */ , T /* team */ };
    
    int getRank();
    
    Result getResult();
    
    int getSortOrder();
    
    CompetitorType getCompetitorType();
    
    /**
     * Somewhat like the sail number, however prefixed by the competition ID, e.g., "SAM007CRO01" meaning
     * "Sailing, boat class 007 (Star), country code CRO, boat 01"
     */
    String getCompetitorCode();
    
    CountryCode getCountryCode();
    
    Iterable<Triple<Double, Integer, MaxPointsReason>> getPointsAndRanksAfterEachRace();
    
    Double getPointsInMedalRace();
    
    /**
     * Can be expected to return a single person in case the {@link #getCompetitorType() competitor type} is {@link CompetitorType#A}, or a list
     * with a {@link Skipper} and {@link Crewmember}s otherwise.
     */
    Iterable<Athlete> getAthletes();
}
