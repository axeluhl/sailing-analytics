package com.sap.sailing.simulator.impl;

import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PathGenerator;
import com.sap.sailing.simulator.SimulationParameters;

public class PathGeneratorBase implements PathGenerator {

    // private static Logger logger = Logger.getLogger("com.sap.sailing");
    private SimulationParameters simulationParameters;

    public PathGeneratorBase() {
        simulationParameters = null;
    }

    public PathGeneratorBase(final SimulationParameters params) {
        simulationParameters = params;
    }

    @Override
    public void setSimulationParameters(final SimulationParameters params) {
        simulationParameters = params;
    }

    @Override
    public SimulationParameters getSimulationParameters() {
        return simulationParameters;
    }

    @Override
    public Path getPath() {
        return null;
    }

    @Override
    public Path getPathEvenTimed(final long millisecondsStep) {

        final Path path = this.getPath();
        return path.getEvenTimedPath(millisecondsStep);
    }
}
