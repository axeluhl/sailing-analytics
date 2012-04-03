package com.sap.sailing.simulator;

import com.sap.sailing.domain.base.Course;
import com.sap.sailing.simulator.BoatSimulationParameters;
import com.sap.sailing.simulator.WindField;
import com.sap.sailing.simulator.Boundaries;

public interface SimulationParameters {
	
	Course getCourse();
	
	BoatSimulationParameters getBoatSimulationParameters();
	
	WindField getWindField();
	
	Boundaries getBoundaries();

}
