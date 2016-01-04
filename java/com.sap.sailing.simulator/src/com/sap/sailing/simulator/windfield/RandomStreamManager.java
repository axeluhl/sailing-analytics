package com.sap.sailing.simulator.windfield;

import java.io.Serializable;

import com.sap.sailing.simulator.windfield.impl.BlastRandom;

public interface RandomStreamManager extends Serializable {

    /**
     * Initialise the random streams managed by the class
     */
    public void initialize();
    
    /**
     * 
     * @param name
     * @return the RandomStream identified by the name
     */
    public BlastRandom getRandomStream(String name);
    
    /**
     * Reset all the random streams managed by the class
     */
    public void reset();
}
