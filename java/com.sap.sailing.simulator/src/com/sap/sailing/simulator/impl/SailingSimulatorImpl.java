package com.sap.sailing.simulator.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.CourseChangeImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.simulator.Boundary;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.SailingSimulator;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.TimedPosition;
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.WindField;
import com.sap.sailing.simulator.WindFieldGenerator;

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
		//use getAllPaths() instead
		
		return createHeuristic();
	}
	
	private static Logger logger = Logger.getLogger("com.sap.sailing");
	private Path createDummy() {
		Boundary boundary = simulationParameters.getBoundaries();
		WindFieldGenerator wf = simulationParameters.getWindField();
		PolarDiagram pd = simulationParameters.getBoatPolarDiagram();
		Position start = simulationParameters.getCourse().get(0);
		Position end = simulationParameters.getCourse().get(1);
		TimePoint startTime = wf.getStartTime();//new MillisecondsTimePoint(0);
		List<TimedPositionWithSpeed> lst = new ArrayList<TimedPositionWithSpeed>();
		
		pd.setWind(wf.getWind(new TimedPositionWithSpeedImpl(startTime, start, null)));
		Bearing direct = start.getBearingGreatCircle(end);
		TimedPositionWithSpeed p1 = new TimedPositionWithSpeedImpl(startTime, start, pd.getSpeedAtBearing(direct));
		TimedPositionWithSpeed p2 = new TimedPositionWithSpeedImpl(new MillisecondsTimePoint(startTime.asMillis()+5*30*1000), end, null);
		lst.add(p1);
		lst.add(p2);
		
		return new PathImpl(lst, wf);
	}
	
	private Path createHeuristic() {
		Boundary boundary = simulationParameters.getBoundaries();
		WindFieldGenerator wf = simulationParameters.getWindField();
		PolarDiagram pd = simulationParameters.getBoatPolarDiagram();
		Position start = simulationParameters.getCourse().get(0);
		Position end = simulationParameters.getCourse().get(1);
		TimePoint startTime = wf.getStartTime();//new MillisecondsTimePoint(0);
		List<TimedPositionWithSpeed> lst = new ArrayList<TimedPositionWithSpeed>();
	
		Position currentPosition = start;
		TimePoint currentTime = startTime;
		
		int prevDirection = -1;
		long turnloss = 4000;  // time lost when doing a turn
		long windpred = 3000;  // time used to predict wind, i.e. hypothetical sailors prediction
		TimePoint directTime;
                TimePoint leftTime;
                TimePoint rightTime;

                Wind wndStart = wf.getWind(new TimedPositionWithSpeedImpl(startTime, start, null));
                logger.fine("wndStart speed:"+wndStart.getKnots()+" angle:"+wndStart.getBearing().getDegrees());
                pd.setWind(wndStart);
                Bearing bearStart = currentPosition.getBearingGreatCircle(end);
                SpeedWithBearing spdStart = pd.getSpeedAtBearing(bearStart);
                lst.add(new TimedPositionWithSpeedImpl(startTime, start, spdStart));
                long timeStep =  wf.getTimeStep().asMillis();
                logger.info("Time step :" + timeStep);
		//while there is more than 5% of the total distance to the finish 
		while ( currentPosition.getDistance(end).compareTo(start.getDistance(end).scale(0.05)) > 0) {
			
			//TimePoint nextTime = new MillisecondsTimePoint(currentTime.asMillis() + 30000);
		        
		        long nextTimeVal = currentTime.asMillis() + timeStep;// + 30000;
                        TimePoint nextTime = new MillisecondsTimePoint(nextTimeVal);
		    
			Wind cWind = wf.getWind(new TimedPositionWithSpeedImpl(currentTime, currentPosition, null));
			logger.fine("cWind speed:"+cWind.getKnots()+" angle:"+cWind.getBearing().getDegrees());
			pd.setWind(cWind);

			// get wind of direction
                        Bearing wLft = pd.optimalDirectionsUpwind()[0];
                        Bearing wRght = pd.optimalDirectionsUpwind()[1];
                        Bearing wDirect = currentPosition.getBearingGreatCircle(end);
                        
                        SpeedWithBearing sWDirect = pd.getSpeedAtBearing(wDirect);
                        SpeedWithBearing sWLft= pd.getSpeedAtBearing(wLft);
                        SpeedWithBearing sWRght = pd.getSpeedAtBearing(wRght);
                        logger.fine("left boat speed:"+sWLft.getKnots()+" angle:"+sWLft.getBearing().getDegrees()+"  right boat speed:"+sWRght.getKnots()+" angle:"+sWRght.getBearing().getDegrees());
			
                        TimePoint wTime = new MillisecondsTimePoint(currentTime.asMillis()+windpred);
                        Position pWDirect = sWDirect.travelTo(currentPosition, currentTime, wTime);
                        Position pWLft = sWLft.travelTo(currentPosition, currentTime, wTime);
                        Position pWRght = sWRght.travelTo(currentPosition, currentTime, wTime);
			
                        logger.fine("current Pos:"+currentPosition.getLatDeg()+","+currentPosition.getLngDeg());
                        logger.fine("left    Pos:"+pWLft.getLatDeg()+","+pWLft.getLngDeg());
                        logger.fine("right   Pos:"+pWRght.getLatDeg()+","+pWRght.getLngDeg());
                                                
			// calculate next step
                        Wind dWind = wf.getWind(new TimedPositionWithSpeedImpl(currentTime, pWDirect, null));
                        logger.fine("dWind speed:"+dWind.getKnots()+" angle:"+dWind.getBearing().getDegrees());
                        pd.setWind(dWind);
                        Bearing direct = currentPosition.getBearingGreatCircle(end);
                        SpeedWithBearing sdirect = pd.getSpeedAtBearing(direct);

                        Wind lWind = wf.getWind(new TimedPositionWithSpeedImpl(currentTime, pWLft, null));
                        logger.fine("lWind speed:"+lWind.getKnots()+" angle:"+lWind.getBearing().getDegrees());
                        pd.setWind(lWind);
                        Bearing lft = pd.optimalDirectionsUpwind()[0];
                        SpeedWithBearing slft= pd.getSpeedAtBearing(lft);
                        
                        Wind rWind = wf.getWind(new TimedPositionWithSpeedImpl(currentTime, pWRght, null));
                        logger.fine("rWind speed:"+rWind.getKnots()+" angle:"+rWind.getBearing().getDegrees());
                        pd.setWind(rWind);
			Bearing rght = pd.optimalDirectionsUpwind()[1];			
			SpeedWithBearing srght = pd.getSpeedAtBearing(rght);
			
			logger.fine("left boat speed:"+slft.getKnots()+" angle:"+slft.getBearing().getDegrees()+"  right boat speed:"+srght.getKnots()+" angle:"+srght.getBearing().getDegrees());
			
			if (prevDirection==0) {
			    directTime = new MillisecondsTimePoint(nextTimeVal);
			    leftTime = new MillisecondsTimePoint(nextTimeVal - turnloss);
			    rightTime = new MillisecondsTimePoint(nextTimeVal - turnloss);
			} else if (prevDirection==1) {
                            directTime = new MillisecondsTimePoint(nextTimeVal - turnloss);
                            leftTime = new MillisecondsTimePoint(nextTimeVal);
                            rightTime = new MillisecondsTimePoint(nextTimeVal - turnloss);
                        } else if (prevDirection==2) {
                            directTime = new MillisecondsTimePoint(nextTimeVal - turnloss);
                            leftTime = new MillisecondsTimePoint(nextTimeVal - turnloss);
                            rightTime = new MillisecondsTimePoint(nextTimeVal);
                        } else {
                            directTime = new MillisecondsTimePoint(nextTimeVal);
                            leftTime = new MillisecondsTimePoint(nextTimeVal);
                            rightTime = new MillisecondsTimePoint(nextTimeVal);                            
                        }
			
			Position pdirect = sdirect.travelTo(currentPosition, currentTime, directTime);
			Position plft = slft.travelTo(currentPosition, currentTime, leftTime);
			Position prght = srght.travelTo(currentPosition, currentTime, rightTime);
			
			Distance ddirect = pdirect.getDistance(end);
			Distance dlft = plft.getDistance(end);
			Distance drght = prght.getDistance(end);
			
			if(ddirect.compareTo(dlft)<=0 && ddirect.compareTo(drght)<=0) {
				lst.add(new TimedPositionWithSpeedImpl(nextTime, pdirect, sdirect));
				currentPosition = pdirect;
				prevDirection = 0;
			}
				
			if(dlft.compareTo(ddirect)<=0 && dlft.compareTo(drght)<=0) {
				lst.add(new TimedPositionWithSpeedImpl(nextTime, plft, slft));
				currentPosition = plft;
				prevDirection = 1;
			}
				
			if(drght.compareTo(dlft)<=0 && drght.compareTo(ddirect)<=0) {
				lst.add(new TimedPositionWithSpeedImpl(nextTime, prght, srght));
				currentPosition = prght;
				prevDirection = 2;
			}
			currentTime = nextTime;
			
		}

                long nextTimeVal = currentTime.asMillis() + timeStep;//30000;
                TimePoint nextTime = new MillisecondsTimePoint(nextTimeVal);

                Wind wndEnd = wf.getWind(new TimedPositionWithSpeedImpl(nextTime, end, null));
                logger.fine("wndEnd speed:"+wndEnd.getKnots()+" angle:"+wndEnd.getBearing().getDegrees());
                pd.setWind(wndEnd);
                Bearing bearEnd = currentPosition.getBearingGreatCircle(end);
                SpeedWithBearing spdEnd = pd.getSpeedAtBearing(bearEnd);
                lst.add(new TimedPositionWithSpeedImpl(nextTime, end, spdEnd));
		
		return new PathImpl(lst, wf);
	}
	
	private Path createDjikstra() {
		//retrieve simulation parameters
		Boundary boundary = new RectangularBoundary(simulationParameters.getCourse().get(0),simulationParameters.getCourse().get(1));//simulationParameters.getBoundaries();
		WindFieldGenerator windField = simulationParameters.getWindField();
		PolarDiagram polarDiagram = simulationParameters.getBoatPolarDiagram();
		Position start = simulationParameters.getCourse().get(0);
		Position end = simulationParameters.getCourse().get(1);
		TimePoint startTime = windField.getStartTime();//new MillisecondsTimePoint(0);
		
		//the solution path
		LinkedList<TimedPositionWithSpeed> lst = new LinkedList<TimedPositionWithSpeed>();
				
		//initiate grid
		int gridv = simulationParameters.getProperty("Djikstra.gridv[int]").intValue(); // number of vertical grid steps
		int gridh = simulationParameters.getProperty("Djikstra.gridh[int]").intValue(); // number of horizontal grid steps
		Position[][] sailGrid = boundary.extractGrid(gridh, gridv);
		
		//create adjacency graph including start and end 
		Map<Position,List<Position>> graph = new HashMap<Position, List<Position>>();
		graph.put(start, Arrays.asList(sailGrid[1]));
		for(int i = 1; i < gridv-2; i++) {
			for(Position p : sailGrid[i]) {
				graph.put(p, Arrays.asList(sailGrid[i+1]));
			}	
		}
		for(Position p : sailGrid[gridv-2]) {
			graph.put(p, Arrays.asList(end));
		}
		
		/*
		//create backwards adjacency graph, required to reconstruct the optimal path
		Map<Position, List<Position>> backGraph = new HashMap<Position, List<Position>>();
		backGraph.put(end, Arrays.asList(sailGrid[gridv-2]));
		for(int i = gridv-2; i > 1; i--) {
			for(Position p: sailGrid[i]) {
				backGraph.put(p, Arrays.asList(sailGrid[i-1]));
			}
		}
		for(Position p : sailGrid[1]) {
			backGraph.put(p, Arrays.asList(start));
		}*/
		
		//create tentative distance matrix
		//additional to tentative distances, the matrix also contains the root of each position
		//that can be </null> if unavailable
		Map<Position, Pair<Long, Position>> tentativeDistances = new HashMap<Position, Pair<Long, Position>>();
		for(Position p : graph.keySet()) {
			tentativeDistances.put(p, new Pair<Long, Position>(Long.MAX_VALUE, null));
		}
		tentativeDistances.put(start, new Pair<Long, Position>(startTime.asMillis(), null));
		tentativeDistances.put(end,  new Pair<Long, Position>(Long.MAX_VALUE, null));
		
		//create set of unvisited nodes
		List<Position> unvisited = new ArrayList<Position>(graph.keySet());
		unvisited.add(end);
		
		//set the initial node as current
		Position currentPosition = start;
		TimePoint currentTime = startTime;
		Bearing previousBearing = null;
		
		//search loop
		//ends when the end is visited
		while(currentPosition != end) {	
			//set the polar diagram to the wind at the current position and time
			TimedPosition currentTimedPosition = new TimedPositionImpl(currentTime, currentPosition);
			SpeedWithBearing currentWind = windField.getWind(currentTimedPosition);
			polarDiagram.setWind(currentWind);
			
			//compute the tentative distance to all the unvisited neighbours of the current node
			//and replace it in the matrix if is smaller than the previous one
			List<Position> unvisitedNeighbours = new LinkedList<Position>(graph.get(currentPosition));
			unvisitedNeighbours.retainAll(unvisited);
			for(Position p : unvisitedNeighbours) {
				Bearing bearingToP = currentPosition.getBearingGreatCircle(p);
				Distance distanceToP = currentPosition.getDistance(p);
				Speed speedToP = polarDiagram.getSpeedAtBearing(bearingToP);
				//multiplied by 1000 to have milliseconds
				Long timeToP = (long) (1000 * (distanceToP.getMeters() / speedToP.getMetersPerSecond()));
				/*if (previousBearing != null) {
					Bearing windBearingFrom = currentWind.getBearing().reverse();
					if( (PolarDiagram49.bearingComparator.compare(bearingToP, windBearingFrom) > 0) 
							&& (PolarDiagram49.bearingComparator.compare(previousBearing, windBearingFrom) < 0) )
						timeToP = timeToP + 4000;
					if( (PolarDiagram49.bearingComparator.compare(bearingToP, windBearingFrom) < 0) 
							&& (PolarDiagram49.bearingComparator.compare(previousBearing, windBearingFrom) > 0) )
						timeToP = timeToP + 4000;
				}*/
						
				Long tentativeDistanceToP = currentTime.asMillis() + timeToP;
				if (tentativeDistanceToP < tentativeDistances.get(p).getA()) {
					tentativeDistances.put(p, new Pair<Long, Position>(tentativeDistanceToP, currentPosition));
				}
			}
			
			//mark current node as visited
			unvisited.remove(currentPosition);
			
			//select the unvisited node with the smallest tentative distance
			//and set it as current
			Long minTentativeDistance = Long.MAX_VALUE;
			for (Position p : unvisited) {
				if( tentativeDistances.get(p).getA() < minTentativeDistance ) {
					currentPosition = p;
					minTentativeDistance = tentativeDistances.get(p).getA();
					previousBearing = tentativeDistances.get(p).getB().getBearingGreatCircle(currentPosition);
					currentTime = new MillisecondsTimePoint(minTentativeDistance);
				}
			
			}
		}
		//I need to add the end point to the distances matrix
		//tentativeDistances.put(end,currentTime.asMillis());
		
		//at this point currentPosition = end
		//currentTime = total duration of the course
		
		//reconstruct the optimal path by going from start to end
		/*while(currentPosition != start) {
			TimedPositionWithSpeed currentTimedPositionWithSpeed = new TimedPositionWithSpeedImpl(currentTime, currentPosition, null );
			lst.addFirst(currentTimedPositionWithSpeed);
			System.out.println(boundary.getGridIndex(currentTimedPositionWithSpeed.getPosition()));
			List<Position> currentPredecessors = backGraph.get(currentPosition);
			Long minTime = Long.MAX_VALUE;
			for(Position p : currentPredecessors) {
				if(tentativeDistances.get(p) < minTime) {
					minTime = tentativeDistances.get(p);
					currentPosition = p;
					currentTime = new MillisecondsTimePoint(minTime);
				}
			}		
		}
		//I need to add the first point to the path
		lst.addFirst(new TimedPositionWithSpeedImpl(startTime, start, null));*/
		while (currentPosition != null) {
			currentTime = new MillisecondsTimePoint(tentativeDistances.get(currentPosition).getA()); 
			SpeedWithBearing windAtPoint = windField.getWind(new TimedPositionImpl(currentTime, currentPosition));
			TimedPositionWithSpeed current = new TimedPositionWithSpeedImpl(currentTime, currentPosition, windAtPoint);
			lst.addFirst(current);
			currentPosition = tentativeDistances.get(currentPosition).getB();
		}
		
		return new PathImpl(lst, windField);
	}
        
	@Override
	public Map<String, Path> getAllPaths() {
		Map<String, Path> allPaths = new HashMap<String, Path>();
		//allPaths.put("Dummy", createDummy());
		allPaths.put("Opportunistic", createHeuristic());
		allPaths.put("Omniscient", createDjikstra());
		return allPaths;
	}
	
	public Map<String, List<TimedPositionWithSpeed>> getAllPathsEvenTimed(long millisecondsStep) {
		
		Map<String, List<TimedPositionWithSpeed>> allPaths= new HashMap<String, List<TimedPositionWithSpeed>>();
		//allPaths.put("Dummy", createDummy().getEvenTimedPoints(millisecondsStep));
		allPaths.put("Opportunistic", createHeuristic().getEvenTimedPoints(millisecondsStep));
		allPaths.put("Omniscient", createDjikstra().getEvenTimedPoints(millisecondsStep));
		return allPaths;
	}

}
