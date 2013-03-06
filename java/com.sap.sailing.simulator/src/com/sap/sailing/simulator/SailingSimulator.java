package com.sap.sailing.simulator;

import java.util.List;
import java.util.Map;

public interface SailingSimulator {

    void setSimulationParameters(SimulationParameters params);

    SimulationParameters getSimulationParameters();

    Path getGPSTrack();

    Path getLegGPSTrack(int legIndex, int competitorIndex);

    Map<String, Path> getAllPaths();

    Map<String, Path> getAllPathsForLeg(int legIndex, int competitorIndex);

    Map<String, Path> getAllPathsEvenTimed(long millisecondsStep);

    Map<String, Path> getAllLegPathsEvenTimed(long millisecondsStep, int legIndex, int competitorIndex);

    Path getRaceCourse();

    List<String> getLegsNames();

    List<String> getRacesNames();

    Path getLeg(int legIndex, int competitorIndex);
}
