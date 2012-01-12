package com.sap.sailing.domain.base;

import java.util.List;

public interface EventRacePlaces {

    String getName();
    List<RacePlaceOrder> getRacePlaces();
    void addRacePlace(RacePlaceOrder racePlace);
    
}
