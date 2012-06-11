package com.sap.sailing.simulator.impl;

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
	
	public SimulationParametersImpl(List<Position> crs, PolarDiagram pd, WindField wf) {
		course = crs;
		polarDiagram = pd;
		windField = wf;
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
		return null;
	}

}
