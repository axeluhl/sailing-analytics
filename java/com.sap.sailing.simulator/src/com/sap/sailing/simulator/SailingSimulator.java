package com.sap.sailing.simulator;

import java.util.Map;

public interface SailingSimulator {

    void setSimulationParameters(SimulationParameters params);

    SimulationParameters getSimulationParameters();

    Path getGPSTrack();

    Map<String, Path> getAllPaths();

    Map<String, Path> getAllPathsEvenTimed(long millisecondsStep);

    Path getRaceCourse();
}
