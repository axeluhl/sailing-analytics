package com.sap.sailing.simulator;

import java.util.Map;

public interface SailingSimulator {

    void setSimulationParameters(SimulationParameters params);

    SimulationParameters getSimulationParameters();

    Map<String, Path> getAllPaths();

    // Map<String, List<TimedPositionWithSpeed>> getAllPathsEvenTimed(long millisecondsStep);

    Map<String, Path> getAllPathsEvenTimed(long millisecondsStep);

    Path getRaceCourse();
}
