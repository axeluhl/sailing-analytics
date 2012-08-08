package com.sap.sailing.simulator;

import java.util.List;

public interface PathGenerator {
	
	void setSimulationParameters(SimulationParameters params);
	SimulationParameters getSimulationParameters();
	
	Path getPath();
	List<TimedPositionWithSpeed> getPathEvenTimed(long millisecondsStep);
	
}
