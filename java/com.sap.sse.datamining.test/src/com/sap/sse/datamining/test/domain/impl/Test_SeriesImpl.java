package com.sap.sse.datamining.test.domain.impl;

import java.util.Arrays;
import java.util.Collection;

import com.sap.sse.datamining.test.domain.Test_Regatta;
import com.sap.sse.datamining.test.domain.Test_Series;
import com.sap.sse.datamining.test.domain.Test_Race;

public class Test_SeriesImpl extends Test_NamedImpl implements Test_Series {
    
    private final Test_Regatta regatta;
    private final Collection<Test_Race> races;

    public Test_SeriesImpl(String name, Test_Regatta regatta, Test_Race... races) {
        super(name);
        this.regatta = regatta;
        this.races = Arrays.asList(races);
    }
    
    @Override
    public Test_Regatta getRegatta() {
        return regatta;
    }

    @Override
    public Collection<Test_Race> getRaces() {
        return races;
    }

}
