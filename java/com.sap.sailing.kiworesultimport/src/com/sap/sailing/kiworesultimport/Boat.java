package com.sap.sailing.kiworesultimport;

public interface Boat extends Named {
    String getSailingNumber();

    Integer getRank();

    String getPrice();
    
    Crew getCrew();
    
    Iterable<Race> getRaces();
    
    Race getRace(int raceNumberOneBased);

    Double getTotalPoints();
}
