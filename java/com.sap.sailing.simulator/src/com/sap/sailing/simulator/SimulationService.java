package com.sap.sailing.simulator;

import java.util.Map;


public interface SimulationService {

    Map<PathType, Path> getAllPaths(SimulationParameters simuPars);
    
    Map<PathType, Path> getAllPathsEvenTimed(SimulationParameters simuPars, long millisecondsStep);

}
