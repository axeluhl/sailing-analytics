package com.sap.sse.datamining.test.domain.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.sap.sse.datamining.test.domain.Test_BoatClass;
import com.sap.sse.datamining.test.domain.Test_Race;
import com.sap.sse.datamining.test.domain.Test_Regatta;
import com.sap.sse.datamining.test.domain.Test_Series;

public class Test_RegattaImpl extends Test_NamedImpl implements Test_Regatta {

    private Test_BoatClass boatClass;
    private int year;
    private Collection<Test_Series> series;
    private Collection<Test_Race> races;

    public Test_RegattaImpl(String name, Test_BoatClass boatClass, int year, Test_Race... races) {
        this(name, boatClass, year, new ArrayList<Test_Series>(), races);
    }

    public Test_RegattaImpl(String name, Test_BoatClass boatClass, int year, Collection<Test_Series> series, Test_Race... races) {
        super(name);
        this.boatClass = boatClass;
        this.year = year;
        this.series = series;
        this.races = Arrays.asList(races);
    }
    
    @Override
    public Test_BoatClass getBoatClass() {
        return boatClass;
    }
    
    @Override
    public int getYear() {
        return year;
    }
    
    @Override
    public Collection<Test_Series> getSeries() {
        return series;
    }
    
    @Override
    public Collection<Test_Race> getRaces() {
        return races;
    }

}
