package com.sap.sailing.simulator.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.simulator.Boundary;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.WindField;

public class SimulationParametersImpl implements SimulationParameters {

	List<Position> course;
	PolarDiagram polarDiagram;
	WindField windField;
	Map<String, Double> settings;
	
	public SimulationParametersImpl(List<Position> crs, PolarDiagram pd, WindField wf) {
		course = crs;
		polarDiagram = pd;
		windField = wf;
		
		settings = new HashMap<String, Double>();
		
		settings.put("Heuristic.targetTolerance[double]", 0.05);
		settings.put("Heuristic.timeResolution[long]", 30000.0);
		settings.put("Djikstra.gridv[int]", 30.0);
		settings.put("Djikstra.gridh[int]", 200.0);
	}
	
	@Override
	public List<Position> getCourse() {
		return course;
	}

	@Override
	public PolarDiagram getBoatPolarDiagram() {
		return polarDiagram;
	}

	@Override
	public WindField getWindField() {
		return windField;
	}

	@Override
	public Boundary getBoundaries() {
		return windField.getBoundaries();
	}

	@Override
	public Map<String, Double> getSettings() {
		return settings;
	}

	@Override
	public void setProperty(String name, Double value) {
		settings.put(name, value);
	}

	@Override
	public Double getProperty(String name) {
		return settings.get(name);
	}
	
}
