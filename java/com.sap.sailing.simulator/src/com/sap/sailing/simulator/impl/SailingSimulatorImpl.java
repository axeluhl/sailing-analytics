package com.sap.sailing.simulator.impl;

import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.SailingSimulator;
import com.sap.sailing.simulator.SimulationParameters;

public class SailingSimulatorImpl implements SailingSimulator {

	SimulationParameters simulationParamaters;
	
	@Override
	public void setSimulationParameters(SimulationParameters params) {
		simulationParamaters = params;
	}

	@Override
	public SimulationParameters getSimulationParameters() {
		return simulationParamaters;
	}

	@Override
	public Path getOptimumPath() {
		// TODO Auto-generated method stub
		return null;
	}
}
