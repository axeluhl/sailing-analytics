package com.sap.sailing.server.interfaces;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.sap.sailing.domain.common.LegIdentifier;
import com.sap.sailing.domain.common.PathType;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.SimulationResults;

public interface SimulationService {

    long getSimulationResultsVersion(LegIdentifier legIdentifier);
    
    SimulationResults getSimulationResults(LegIdentifier legIdentifier);

    Map<PathType, Path> getAllPathsEvenTimed(SimulationParameters simuPars, long millisecondsStep)
            throws InterruptedException, ExecutionException;

}
