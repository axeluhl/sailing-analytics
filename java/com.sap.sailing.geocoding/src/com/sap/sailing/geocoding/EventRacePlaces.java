package com.sap.sailing.geocoding;

import java.util.List;

public interface EventRacePlaces {

    String getName();
    List<RacePlaceOrder> getRacePlaces();
    void addRacePlace(RacePlaceOrder racePlace);
    
}
