package com.sap.sailing.simulator.windfield.impl;

import java.util.HashMap;

import umontreal.iro.lecuyer.rng.LFSR113;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.rng.RandomStream;

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
       randomStreamMap = new HashMap<String, RandomStream>();
        
       RandomStream stream = new LFSR113(BlastStream.SEED.name());
       randomStreamMap.put(BlastStream.SEED.name(),stream);
       
       stream = new LFSR113(BlastStream.CELL.name());
       randomStreamMap.put(BlastStream.CELL.name(),stream);
       
       stream = new LFSR113(BlastStream.SPEED.name());
       randomStreamMap.put(BlastStream.SPEED.name(),stream);
      
       stream = new MRG32k3a(BlastStream.SIZE.name());
       randomStreamMap.put(BlastStream.SIZE.name(),stream);
       
       stream = new LFSR113(BlastStream.BEARING.name());
       randomStreamMap.put(BlastStream.BEARING.name(), stream);
    }

}
