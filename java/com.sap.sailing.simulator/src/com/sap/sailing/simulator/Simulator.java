package com.sap.sailing.simulator;

import java.util.List;

import com.sap.sailing.simulator.impl.MaximumTurnTimes;
import com.sap.sailing.simulator.impl.SparsePolarDataException;


public interface Simulator {

    void setSimulationParameters(SimulationParameters params, SimulatorUISelection selection);

    SimulationParameters getSimulationParameters();

    Path getPath(PathType pathType, MaximumTurnTimes maxTurnTimes) throws SparsePolarDataException;
    
    Path getRaceCourse();

    List<String> getLegsNames(int selectedRaceIndex);

    List<String> getRacesNames();

    List<String> getCompetitorsNames(int selectedRaceIndex);

    Path getLeg(int selectedCompetitorIndex, int selectedLegIndex);

    Path getLegGPSTrack(SimulatorUISelection selection);

}
