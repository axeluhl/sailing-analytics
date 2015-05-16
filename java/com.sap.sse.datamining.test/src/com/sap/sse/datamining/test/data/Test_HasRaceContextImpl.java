package com.sap.sse.datamining.test.data;

import com.sap.sse.datamining.test.domain.Test_BoatClass;
import com.sap.sse.datamining.test.domain.Test_Race;
import com.sap.sse.datamining.test.domain.Test_Regatta;

public class Test_HasRaceContextImpl implements Test_HasRaceContext {

    private Test_Regatta regatta;
    private Test_Race race;
    private Test_BoatClass boatClass;
    private int year;

    public Test_HasRaceContextImpl(Test_Regatta regatta, Test_Race race, Test_BoatClass boatClass, int year) {
        this.regatta = regatta;
        this.race = race;
        this.boatClass = boatClass;
        this.year = year;
    }

    @Override
    public Test_Regatta getRegatta() {
        return regatta;
    }

    @Override
    public Test_Race getRace() {
        return race;
    }

    @Override
    public Test_BoatClass getBoatClass() {
        return boatClass;
    }

    @Override
    public int getYear() {
        return year;
    }

}
