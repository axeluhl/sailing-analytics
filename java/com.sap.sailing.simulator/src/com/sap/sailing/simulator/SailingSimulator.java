package com.sap.sailing.simulator;

import java.util.List;
import java.util.Map;

public interface SailingSimulator {

    void setSimulationParameters(SimulationParameters params, int selectedRaceIndex);

    SimulationParameters getSimulationParameters();

    // Path getGPSTrack();

    Path getLegGPSTrack(int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex);

    Map<String, Path> getAllPaths();

    Map<String, Path> getAllPathsForLeg(int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex);

    Map<String, Path> getAllPathsEvenTimed(long millisecondsStep);

    Map<String, Path> getAllLegPathsEvenTimed(long millisecondsStep, int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex);

    Path getRaceCourse();

    List<String> getLegsNames(int selectedRaceIndex);

    List<String> getRacesNames();

    Path getLeg(int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex);

    List<String> getComeptitorsNames(int selectedRaceIndex);
}
