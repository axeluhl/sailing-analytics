package com.sap.sailing.simulator;

import java.util.Map;

public interface SailingSimulator {
	
	void setSimulationParameters(SimulationParameters params);
	SimulationParameters getSimulationParameters();
	
	Path getOptimumPath();
	Map<String, Path> getAllPaths();

}
