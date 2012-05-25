package com.sap.sailing.simulator.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.MeterDistance;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
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
		
		return createHeuristic();
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
		Boundary boundary = simulationParameters.getBoundaries();
		WindField wf = simulationParameters.getWindField();
		PolarDiagram pd = simulationParameters.getBoatPolarDiagram();
		Position start = simulationParameters.getCourse().get(0);
		Position end = simulationParameters.getCourse().get(1);
		TimePoint startTime = new MillisecondsTimePoint(0);
		List<TimedPositionWithSpeed> lst = new ArrayList<TimedPositionWithSpeed>();
	
		Position currentPosition = start;
		TimePoint currentTime = startTime;
		
		//while there is more than 5% of the total distance to the finish 
		while ( currentPosition.getDistance(end).compareTo(start.getDistance(end).scale(0.05)) > 0) {
			
			TimePoint nextTime = new MillisecondsTimePoint(currentTime.asMillis() + 30000);
			
			pd.setWind(wf.getWind(new TimedPositionWithSpeedSimple(currentPosition)));
			
			Bearing lft = pd.optimalDirectionsDownwind()[0];
			Bearing rght = pd.optimalDirectionsUpwind()[1];
			Bearing direct = currentPosition.getBearingGreatCircle(end);
			
			SpeedWithBearing sdirect = pd.getSpeedAtBearing(direct);
			SpeedWithBearing slft= pd.getSpeedAtBearing(lft);
			SpeedWithBearing srght = pd.getSpeedAtBearing(rght);
			
			Position pdirect = sdirect.travelTo(currentPosition, currentTime, nextTime);
			Position plft = slft.travelTo(currentPosition, currentTime, nextTime);
			Position prght = srght.travelTo(currentPosition, currentTime, nextTime);
			
			Distance ddirect = pdirect.getDistance(end);
			Distance dlft = plft.getDistance(end);
			Distance drght = prght.getDistance(end);
			
			if(ddirect.compareTo(dlft)<=0 && ddirect.compareTo(drght)<=0) {
				lst.add(new TimedPositionWithSpeedImpl(nextTime, pdirect, sdirect));
				currentPosition = pdirect;
			}
				
			if(dlft.compareTo(ddirect)<=0 && dlft.compareTo(drght)<=0) {
				lst.add(new TimedPositionWithSpeedImpl(nextTime, plft, slft));
				currentPosition = plft;
			}
				
			if(drght.compareTo(dlft)<=0 && drght.compareTo(ddirect)<=0) {
				lst.add(new TimedPositionWithSpeedImpl(nextTime, prght, srght));
				currentPosition = prght;
			}
			
			currentTime = nextTime;
			
		}
		
		return new PathImpl(lst);
	}
	
}
