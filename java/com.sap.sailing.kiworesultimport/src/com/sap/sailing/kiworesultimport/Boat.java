package com.sap.sailing.kiworesultimport;

public interface Boat extends Named {
    String getSailingNumber();

    Integer getPosition();

    String getPreis();
    
    Crew getCrew();
    
    Races getRaces();
}
