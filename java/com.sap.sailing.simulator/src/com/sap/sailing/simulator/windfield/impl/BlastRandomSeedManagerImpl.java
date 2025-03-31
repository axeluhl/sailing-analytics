package com.sap.sailing.simulator.windfield.impl;

import java.util.HashMap;

public class BlastRandomSeedManagerImpl extends RandomStreamManagerImpl {

    private static final long serialVersionUID = -6409398526247431352L;

    public enum BlastStream {
        SEED,
        CELL,
        SPEED,
        SIZE,
        BEARING
    }
    
    @Override
    public void initialize() {
       randomStreamMap = new HashMap<String, BlastRandom>();
        
       BlastRandom stream = new BlastRandom();
       randomStreamMap.put(BlastStream.SEED.name(),stream);
       
       stream = new BlastRandom();
       randomStreamMap.put(BlastStream.CELL.name(),stream);
       
       stream = new BlastRandom();
       randomStreamMap.put(BlastStream.SPEED.name(),stream);
      
       stream = new BlastRandom();
       randomStreamMap.put(BlastStream.SIZE.name(),stream);
       
       stream = new BlastRandom();
       randomStreamMap.put(BlastStream.BEARING.name(), stream);
    }

}
