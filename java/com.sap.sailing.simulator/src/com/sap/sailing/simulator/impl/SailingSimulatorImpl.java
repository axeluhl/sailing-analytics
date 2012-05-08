package com.sap.sailing.simulator.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.simulator.Boundary;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.SailingSimulator;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.TimedPosition;
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.WindField;

public class SailingSimulatorImpl implements SailingSimulator {

	SimulationParameters simulationParameters;
	
	public SailingSimulatorImpl(SimulationParameters params) {
		simulationParameters = params;
	}
	
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
		
		//calls either createDummy or createHeuristic()
		
		return createDummy();
	}
	
	private Path createDummy() {
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
		
		return new PathImpl(lst);
	}
	
	private Path createHeuristic() {
		
		return null;
	}
	
	//SailingSimulator roundtrip
	public static void main(String args[]) {
		Position p1 = new DegreePosition(25.045792, -91.472168);
		Position p2 = new DegreePosition(26.076521,-89.681396);
		
		Boundary b = new RectangularBoundary(p1, p2);
		
		System.out.println(b.getCorners());
		
		WindField wf = new WindFieldImpl(b, 20, 45);
		PolarDiagram pd = new PolarDiagramImpl(1);
		List<Position> course= new ArrayList<Position>();
		course.add(p1);
		course.add(p2);
		SimulationParameters sp = new SimulationParametersImpl(course, pd, wf);
		SailingSimulator solver = new SailingSimulatorImpl(sp);
		
		Path pth = solver.getOptimumPath();
		
		for(TimedPosition p : pth.getEvenTimedPoints(360000)) {
			System.out.println("Position: " + p.getPosition() + " Wind: " + wf.getWind(new TimedPositionWithSpeedSimple(p.getPosition())));
		}
		//the null in the Wind output is the timestamp - this Wind is time-invariant!
	}
}
