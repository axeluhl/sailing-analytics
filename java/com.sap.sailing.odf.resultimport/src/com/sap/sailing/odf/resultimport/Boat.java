package com.sap.sailing.odf.resultimport;

public interface Boat extends Named {
    String getSailingNumber();

    Integer getRank();

    String getPrice();
    
    Crew getCrew();
    
    Iterable<BoatResultInRace> getResultsInRaces();
    
    BoatResultInRace getResultsInRace(int raceNumberOneBased);

    Double getTotalPoints();
}
