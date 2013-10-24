package com.sap.sailing.domain.swisstimingadapter.impl;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.swisstimingadapter.RaceType;

public class RaceTypeImpl implements RaceType {

    private final OlympicRaceCode raceCode;
    private final BoatClass boatClass;

    public RaceTypeImpl(OlympicRaceCode raceCode, BoatClass boatClass) {
        this.raceCode = raceCode;
        this.boatClass = boatClass;
    }
    
    @Override
    public final OlympicRaceCode getRaceCode() {
        return raceCode;
    }

    @Override
    public BoatClass getBoatClass() {
        return boatClass;
    }
    
}
    
