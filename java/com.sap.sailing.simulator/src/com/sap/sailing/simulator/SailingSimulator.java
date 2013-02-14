package com.sap.sailing.simulator;

import java.util.List;
import java.util.Map;

public interface SailingSimulator {

    void setSimulationParameters(SimulationParameters params);

    SimulationParameters getSimulationParameters();

    Path getGPSTrack();

    Map<String, Path> getAllPaths();

    Map<String, Path> getAllPathsEvenTimed(long millisecondsStep);

    Path getRaceCourse();

    List<String> getLegsNames(int boatClassIndex);

    Path getLeg(int legIndex, int competitorIndex, int boatClassIndex);
}
