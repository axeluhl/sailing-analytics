package com.sap.sailing.simulator.impl;

import java.util.List;

import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PathGenerator;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.TimedPositionWithSpeed;

public class PathGeneratorBase implements PathGenerator {

    // private static Logger logger = Logger.getLogger("com.sap.sailing");
    private SimulationParameters simulationParameters;
    
    public PathGeneratorBase() {
        simulationParameters = null;
    }
    
    public PathGeneratorBase(SimulationParameters params) {
        simulationParameters = params;
    }

    public void setSimulationParameters(SimulationParameters params) {
        simulationParameters = params;
    }

    public SimulationParameters getSimulationParameters() {
        return simulationParameters;
    }

    @Override
    public Path getPath() {
        return null;
    }

    public List<TimedPositionWithSpeed> getPathEvenTimed(long millisecondsStep) {

        Path path = this.getPath();
        return path.getEvenTimedPath(millisecondsStep);

    }

}
