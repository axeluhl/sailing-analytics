package com.sap.sailing.simulator;

public interface PathGenerator {

    void setSimulationParameters(SimulationParameters params);

    SimulationParameters getSimulationParameters();

    Path getPath(int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex);

    Path getPathEvenTimed(long stepMilliseconds, int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex);
}
