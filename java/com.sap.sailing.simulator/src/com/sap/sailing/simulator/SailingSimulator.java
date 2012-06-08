package com.sap.sailing.simulator;

public interface SailingSimulator {
	
	void setSimulationParameters(SimulationParameters params);
	SimulationParameters getSimulationParameters();
	
	Path getOptimumPath();
	
	Path getOpputunisticPath();

}
