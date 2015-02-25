package com.sap.sailing.server.simulation;

import com.sap.sailing.domain.common.LegIdentifier;
import com.sap.sailing.simulator.SimulationResults;


public interface SimulationService {
    
    SimulationResults getSimulationResults(LegIdentifier legIdentifier);
    
}
