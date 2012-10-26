package com.sap.sailing.simulator.windfield;

import com.sap.sailing.simulator.Boundary;
import com.sap.sailing.simulator.windfield.impl.WindFieldGeneratorFactoryImpl;

public interface WindFieldGeneratorFactory {

    static WindFieldGeneratorFactory INSTANCE = new WindFieldGeneratorFactoryImpl();

    public WindFieldGenerator createWindFieldGenerator(String patternName, Boundary boundary,
            WindControlParameters windParameters);
}
