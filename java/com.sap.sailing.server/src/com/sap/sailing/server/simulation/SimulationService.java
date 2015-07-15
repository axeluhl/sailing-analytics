package com.sap.sailing.server.simulation;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.sap.sailing.domain.common.LegIdentifier;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PathType;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.SimulationResults;

public interface SimulationService {

    int getSimulationResultsVersion(LegIdentifier legIdentifier);
    
    SimulationResults getSimulationResults(LegIdentifier legIdentifier);

    Map<PathType, Path> getAllPathsEvenTimed(SimulationParameters simuPars, long millisecondsStep)
            throws InterruptedException, ExecutionException;

}
