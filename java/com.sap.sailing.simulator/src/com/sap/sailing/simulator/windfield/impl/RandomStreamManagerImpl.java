package com.sap.sailing.simulator.windfield.impl;

import java.util.Map;
import java.util.Map.Entry;

import umontreal.iro.lecuyer.rng.RandomStream;

import com.sap.sailing.simulator.windfield.RandomStreamManager;

public abstract class RandomStreamManagerImpl implements RandomStreamManager {

    protected Map<String, RandomStream> randomStreamMap;
    
    public RandomStreamManagerImpl() {
        initialize();
    }
    
    @Override
    public RandomStream getRandomStream(String name) {
       return randomStreamMap.get(name);        
    }

    @Override
    public void reset() {
        for(Entry<String, RandomStream> entry : randomStreamMap.entrySet()) {
            entry.getValue().resetStartStream();
        }
    }
}
