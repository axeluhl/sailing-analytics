package com.sap.sailing.domain.base;

import java.util.List;

import com.sap.sailing.domain.common.RacePlaceOrder;

public interface EventRacePlaces {

    String getName();
    List<RacePlaceOrder> getRacePlaces();
    void addRacePlace(RacePlaceOrder racePlace);
    
}
