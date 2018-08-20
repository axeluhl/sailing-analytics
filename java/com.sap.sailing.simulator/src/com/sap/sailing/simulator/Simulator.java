package com.sap.sailing.simulator;

import java.util.List;

import com.sap.sailing.domain.common.PathType;
import com.sap.sailing.simulator.impl.SparseSimulationDataException;


public interface Simulator {

    void setSimulationParameters(SimulationParameters params, SimulatorUISelection selection);

    SimulationParameters getSimulationParameters();

    Path getPath(PathType pathType) throws SparseSimulationDataException;
    
    Path getRaceCourse();

    List<String> getLegsNames(int selectedRaceIndex);

    List<String> getRacesNames();

    List<String> getCompetitorsNames(int selectedRaceIndex);

    Path getLeg(int selectedCompetitorIndex, int selectedLegIndex);

    Path getLegGPSTrack(SimulatorUISelection selection);

}
