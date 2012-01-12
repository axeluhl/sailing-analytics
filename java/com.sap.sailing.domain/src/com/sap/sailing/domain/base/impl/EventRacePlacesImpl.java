package com.sap.sailing.domain.base.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.EventRacePlaces;
import com.sap.sailing.domain.base.RacePlaceOrder;

public class EventRacePlacesImpl implements EventRacePlaces {
    
    private String eventName;
    private List<RacePlaceOrder> racePlaces;

    public EventRacePlacesImpl(String eventName) {
        super();
        this.eventName = eventName;
        this.racePlaces = new ArrayList<RacePlaceOrder>();
    }

    @Override
    public String getName() {
        return eventName;
    }

    @Override
    public List<RacePlaceOrder> getRacePlaces() {
        return racePlaces;
    }

    @Override
    public void addRacePlace(RacePlaceOrder racePlace) {
        racePlaces.add(racePlace);
    }
    
    @Override
    public String toString() {
        return eventName + racePlaces.toString();
    }

}
