package com.sap.sailing.simulator.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.simulator.Boundary;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.SailingSimulator;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.WindField;

public class SailingSimulatorImpl implements SailingSimulator {

	SimulationParameters simulationParameters;
	
	@Override
	public void setSimulationParameters(SimulationParameters params) {
		simulationParameters = params;
	}

	@Override
	public SimulationParameters getSimulationParameters() {
		return simulationParameters;
	}

	@Override
	public Path getOptimumPath() {
		Boundary boundary = simulationParameters.getBoundaries();
		WindField wf = simulationParameters.getWindField();
		PolarDiagram pd = simulationParameters.getBoatPolarDiagram();
		Position start = simulationParameters.getCourse().get(0);
		Position end = simulationParameters.getCourse().get(1);
		TimePoint startTime = new MillisecondsTimePoint(0);
		List<TimedPositionWithSpeed> lst = new ArrayList<TimedPositionWithSpeed>();
		
		pd.setWind(wf.getWind(new TimedPositionWithSpeedSimple(start)));
		Bearing direct = start.getBearingGreatCircle(end);
		TimedPositionWithSpeed p1 = new TimedPositionWithSpeedImpl(startTime, start, pd.getSpeedAtBearing(direct));
		TimedPositionWithSpeed p2 = new TimedPositionWithSpeedImpl(new MillisecondsTimePoint(3600000), end, null);
		lst.add(p1);
		lst.add(p2);
		
		//algorithm goes here
		
		return new PathImpl(lst);
	}
}
