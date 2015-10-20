package com.sap.sailing.simulator.windfield.impl;

import java.util.Map;

import com.sap.sailing.simulator.windfield.RandomStreamManager;

public abstract class RandomStreamManagerImpl implements RandomStreamManager {

    private static final long serialVersionUID = -2066054080465695880L;
    protected Map<String, BlastRandom> randomStreamMap;
    
    public RandomStreamManagerImpl() {
        initialize();
    }
    
    @Override
    public BlastRandom getRandomStream(String name) {
       return randomStreamMap.get(name);        
    }

    @Override
    public void reset() {
        // if seeds are used, reset seeds
    }
}
