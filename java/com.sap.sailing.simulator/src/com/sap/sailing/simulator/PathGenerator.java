package com.sap.sailing.simulator;

public interface PathGenerator {

    void setSimulationParameters(SimulationParameters params);

    SimulationParameters getSimulationParameters();

    Path getPath();

    Path getPathEvenTimed(long stepMilliseconds);
    
    public boolean isTimedOut();

}
