package com.sap.sailing.simulator.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
			
			pd.setWind(wf.getWind(new TimedPositionWithSpeedImpl(nextTime, currentPosition, null)));
			
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
	
	private Path createDjikstra() {
		//retrieve simulation parameters
		Boundary boundary = simulationParameters.getBoundaries();
		WindField windField = simulationParameters.getWindField();
		PolarDiagram polarDiagram = simulationParameters.getBoatPolarDiagram();
		Position start = simulationParameters.getCourse().get(0);
		Position end = simulationParameters.getCourse().get(1);
		TimePoint startTime = new MillisecondsTimePoint(0);
		
		//the solution path
		LinkedList<TimedPositionWithSpeed> lst = new LinkedList<TimedPositionWithSpeed>();
				
		//initiate grid
		int gridv = 10; // number of vertical grid steps
		int gridh = 30; // number of horizontal grid steps
		Position[][] sailGrid = boundary.extractGrid(gridh, gridv);
		
		//create adjacency graph including start and end 
		Map<Position,List<Position>> graph = new HashMap<Position, List<Position>>();
		graph.put(start, Arrays.asList(sailGrid[0]));
		for(int i = 0; i < gridv-1; i++) {
			for(Position p : sailGrid[i]) {
				graph.put(p, Arrays.asList(sailGrid[i+1]));
			}	
		}
		for(Position p: sailGrid[gridv-1]) {
			graph.put(p, Arrays.asList(end));
		}
		
		//create backwards adjacency graph, required to reconstruct the optimal path
		Map<Position, List<Position>> backGraph = new HashMap<Position, List<Position>>();
		backGraph.put(end, Arrays.asList(sailGrid[gridv-1]));
		for(int i = gridv-2; i>0; i--) {
			for(Position p: sailGrid[i]) {
				backGraph.put(p, Arrays.asList(sailGrid[i-1]));
			}
		}
		for(Position p : sailGrid[0]) {
			backGraph.put(p, Arrays.asList(start));
		}
		
		//create tentative distance matrix
		Map<Position, Double> tentativeDistances = new HashMap<Position, Double>();
		for(Position p : graph.keySet()) {
			tentativeDistances.put(p, Double.POSITIVE_INFINITY);
		}
		tentativeDistances.put(start, 0.0);
		
		//create set of unvisited nodes
		List<Position> unvisited = new ArrayList<Position>(graph.keySet());		
		
		//set the initial node as current
		Position currentPosition = start;
		TimePoint currentTime = startTime;
		
		//search loop
		while(unvisited.contains(end)) {
			
			TimedPosition currentTimedPosition = new TimedPositionImpl(currentTime, currentPosition);
			SpeedWithBearing currentWind = windField.getWind(currentTimedPosition);
			polarDiagram.setWind(currentWind);
			
			List<Position> unvisitedNeighbours = graph.get(currentPosition);
			unvisitedNeighbours.retainAll(unvisited);
			for(Position p : unvisitedNeighbours) {
				Bearing bearingToP = currentPosition.getBearingGreatCircle(p);
				Distance distanceToP = currentPosition.getDistance(p);
				Speed speedToP = polarDiagram.getSpeedAtBearing(bearingToP);
				//multiplied by 1000 to have milliseconds
				Double timeToP = 1000 * (distanceToP.getMeters() / speedToP.getMetersPerSecond());
				Double tentativeDistanceToP = currentTime.asMillis() + timeToP;
				if (tentativeDistanceToP < tentativeDistances.get(p)) {
					tentativeDistances.put(p, tentativeDistanceToP);
				}
			}
			unvisited.remove(currentPosition);
			
			Map<Position, Double> unvisitedTentativeDistances =  new HashMap<Position, Double>(tentativeDistances);
			unvisitedTentativeDistances.entrySet().retainAll(unvisited);
			Double minTentativeDistance = Collections.min(unvisitedTentativeDistances.values());
			for (Position p : unvisitedTentativeDistances.keySet()) {
				if( unvisitedTentativeDistances.get(p) == minTentativeDistance ) {
					currentPosition = p;
					currentTime = new MillisecondsTimePoint(minTentativeDistance.longValue());
					break;
				}
			}
			
		}	
		
		//reconstruct the optimal path by going from start to end
		currentPosition = end;
		Position nextPosition = end;
		while(nextPosition!=start) {
			List<Position> currentPredecessors = backGraph.get(currentPosition);
			
			currentTime = new MillisecondsTimePoint(tentativeDistances.get(currentPosition).longValue());
			TimedPosition currentTimedPosition = new TimedPositionImpl(currentTime, end);
			SpeedWithBearing currentWind = windField.getWind(currentTimedPosition);
			polarDiagram.setWind(currentWind);
			SpeedWithBearing currentSpeed;
			//TimedPositionWithSpeed = new TimedPositionWithSpeedImpl(currentTime, currentPosition, );
		
		}
		return new PathImpl(lst);
	}
	
}
