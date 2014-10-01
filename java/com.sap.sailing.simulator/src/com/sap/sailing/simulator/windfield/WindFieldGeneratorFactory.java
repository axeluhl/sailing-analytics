package com.sap.sailing.simulator.windfield;

import com.sap.sailing.simulator.Grid;
import com.sap.sailing.simulator.windfield.impl.WindFieldGeneratorFactoryImpl;

public interface WindFieldGeneratorFactory {

    static WindFieldGeneratorFactory INSTANCE = new WindFieldGeneratorFactoryImpl();

    public WindFieldGenerator createWindFieldGenerator(String patternName, Grid boundary,
            WindControlParameters windParameters);
}
