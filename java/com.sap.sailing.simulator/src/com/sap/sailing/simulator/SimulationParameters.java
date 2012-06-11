package com.sap.sailing.simulator;

import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.simulator.WindField;

public interface SimulationParameters {
	
	List<Position> getCourse();
	
	PolarDiagram getBoatPolarDiagram();
	
	WindField getWindField();
	
	Boundary getBoundaries();
	
	Map<String,Double> getSettings();

}
