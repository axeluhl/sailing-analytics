package com.sap.sse.datamining.test.domain.impl;

import java.util.Collection;

import com.sap.sse.datamining.test.domain.Test_Competitor;
import com.sap.sse.datamining.test.domain.Test_Leg;
import com.sap.sse.datamining.test.domain.Test_Race;

public class Test_RaceImpl extends Test_NamedImpl implements Test_Race {

    private Collection<Test_Competitor> competitors;
    private Collection<Test_Leg> legs;

    public Test_RaceImpl(String name, Collection<Test_Competitor> competitors, Collection<Test_Leg> legs) {
        super(name);
        this.competitors = competitors;
        this.legs = legs;
    }
    
    @Override
    public Collection<Test_Competitor> getCompetitors() {
        return competitors;
    }
    
    @Override
    public Collection<Test_Leg> getLegs() {
        return legs;
    }

}
