package com.sap.sailing.simulator;

import com.sap.sailing.simulator.impl.SparsePolarDataException;

public interface PathGenerator {

    void setSimulationParameters(SimulationParameters params);

    SimulationParameters getSimulationParameters();

    Path getPath() throws SparsePolarDataException;

    Path getPathEvenTimed(long stepMilliseconds) throws SparsePolarDataException;
    
    public boolean isTimedOut();

}
