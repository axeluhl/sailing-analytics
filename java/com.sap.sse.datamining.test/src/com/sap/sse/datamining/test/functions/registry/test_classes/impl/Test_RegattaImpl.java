package com.sap.sse.datamining.test.functions.registry.test_classes.impl;

import java.util.Arrays;
import java.util.Collection;

import com.sap.sse.datamining.test.functions.registry.test_classes.Test_BoatClass;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Race;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Regatta;

public class Test_RegattaImpl extends Test_NamedImpl implements Test_Regatta {

    private Test_BoatClass boatClass;
    private Collection<Test_Race> races;

    public Test_RegattaImpl(String name, Test_BoatClass boatClass, Test_Race... races) {
        super(name);
        this.boatClass = boatClass;
        this.races = Arrays.asList(races);
    }
    
    @Override
    public Test_BoatClass getBoatClass() {
        return boatClass;
    }
    
    @Override
    public Collection<Test_Race> getRaces() {
        return races;
    }

}
