package com.sap.sailing.simulator;

import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.common.Position;

public interface SimulationParameters {
	
        char getMode();
    
        public void setCourse(List<Position> course);

	List<Position> getCourse();
	
	PolarDiagram getBoatPolarDiagram();
	
	WindFieldGenerator getWindField();
	
	Boundary getBoundaries();
	
	Map<String,Double> getSettings();
	
	void setProperty(String name, Double value);
	
	Double getProperty(String name);

}
