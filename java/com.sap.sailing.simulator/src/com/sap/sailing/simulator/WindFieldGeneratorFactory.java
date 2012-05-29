package com.sap.sailing.simulator;

import com.sap.sailing.simulator.impl.WindFieldGeneratorFactoryImpl;

public interface WindFieldGeneratorFactory {

    static WindFieldGeneratorFactory INSTANCE = new WindFieldGeneratorFactoryImpl();

    public WindFieldGenerator createWindFieldGenerator(String patternName, Boundary boundary,
            WindControlParameters windParameters);
}
