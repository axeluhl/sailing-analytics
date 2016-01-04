package com.sap.sailing.simulator;

import com.sap.sailing.simulator.impl.SparseSimulationDataException;

public interface PathGenerator {

    void setSimulationParameters(SimulationParameters params);

    SimulationParameters getSimulationParameters();

    Path getPath() throws SparseSimulationDataException;

    Path getPathEvenTimed(long stepMilliseconds) throws SparseSimulationDataException;
    
    public boolean isTimedOut();

}
