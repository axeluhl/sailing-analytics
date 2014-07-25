package com.sap.sailing.simulator.windfield.impl;


import com.sap.sailing.simulator.Grid;
import com.sap.sailing.simulator.windfield.WindControlParameters;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;
import com.sap.sailing.simulator.windfield.WindFieldGeneratorFactory;

public class WindFieldGeneratorFactoryImpl implements WindFieldGeneratorFactory {

    @Override
    public WindFieldGenerator createWindFieldGenerator(String patternName, Grid boundary,
            WindControlParameters windParameters) {
        if (patternName.equals("BLASTS")) {
            return new WindFieldGeneratorBlastImpl(boundary, windParameters);
        }
        if (patternName.equals("OSCILLATIONS")) {
            return new WindFieldGeneratorOscillationImpl(boundary, windParameters);
        }
        if (patternName.equals("OSCILLATION_WITH_BLASTS")) {
            return new WindFieldGeneratorCombined(boundary, windParameters);
        }
        if (patternName.equals("MEASURED")) {
            return new WindFieldGeneratorMeasured(boundary, windParameters);
        }
        
        return null;
    }

}
