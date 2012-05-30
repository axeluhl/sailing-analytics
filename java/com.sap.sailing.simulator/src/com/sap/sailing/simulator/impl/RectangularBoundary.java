package com.sap.sailing.simulator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.simulator.Boundary;

public class RectangularBoundary implements Boundary {

	private Position northWest;
	private Position southEast;
	private Position southWest;
	private Position northEast;
	
	private Position appNorthWest;
	private Position appSouthEast;
	private Position appSouthWest;
	private Position appNorthEast;
	
	
	private Bearing north;
	private Bearing south;
	private Bearing east;
	private Bearing west;
	
	private Distance width;
	private Distance height;
	
	private Distance appWidth;
	private Distance appHeight;
	
	double tolerance;
	
	public RectangularBoundary(Position p1, Position p2, double tlr) {
		
		tolerance = tlr;
		
		north = p2.getBearingGreatCircle(p1);
		south = north.reverse();
		east = north.add(TRUEEAST);
		west = north.add(TRUEWEST);
		
		appHeight = p1.getDistance(p2);
		appWidth = appHeight.scale(2);
		
		appNorthWest = p1.translateGreatCircle(west, appWidth.scale(0.5));
		appNorthEast = p1.translateGreatCircle(east, appWidth.scale(0.5));
		appSouthEast = appNorthEast.translateGreatCircle(south, appHeight);
		appSouthWest = appNorthWest.translateGreatCircle(south, appHeight);
		
		Distance diag = appNorthWest.getDistance(appSouthEast);
		
		Bearing diag1 = appSouthEast.getBearingGreatCircle(appNorthWest);
		northWest = appNorthWest.translateGreatCircle(diag1, diag.scale(tolerance));
		diag1 = diag1.reverse();
		southEast = appSouthEast.translateGreatCircle(diag1, diag.scale(tolerance));
		Bearing diag2 = appSouthWest.getBearingGreatCircle(appNorthEast);
		northEast = appNorthEast.translateGreatCircle(diag2, diag.scale(tolerance));
		diag2 = diag2.reverse();
		southWest = appSouthWest.translateGreatCircle(diag2, diag.scale(tolerance));
		
		height = northWest.getDistance(southWest);
		width = southWest.getDistance(southEast);
	}

	@Override
	public Map<String, Position> getCorners() {

		Map<String, Position> map = new HashMap<String, Position>();
		map.put("NorthWest", appNorthWest);
		map.put("SouthWest", appSouthWest);
		map.put("SouthEast", appSouthEast);
		map.put("NorthEast", appNorthEast);
		
		return map;

	}

	@Override
	public boolean isWithinBoundaries(Position p) {
		
		Position northProjection = p.projectToLineThrough(northWest, getEast());
		Position southProjection = p.projectToLineThrough(southWest, getEast());
		Position westProjection = p.projectToLineThrough(northWest, getNorth());
		Position eastProjection = p.projectToLineThrough(northEast, getNorth());
		
		Distance northSouth = northProjection.getDistance(southProjection);
		Distance eastWest = eastProjection.getDistance(westProjection);
		
		return 
				( northSouth.compareTo(p.getDistance(northProjection)) >= 0 )
				&& ( northSouth.compareTo(p.getDistance(southProjection)) >= 0 )
				&& ( eastWest.compareTo(p.getDistance(eastProjection)) >= 0 )
				&& ( eastWest.compareTo(p.getDistance(westProjection)) >=0 );
		
	}

	@Override
	public Position[][] extractGrid(int hPoints, int vPoints) {
		
		Distance hStep = appWidth.scale(1.0/(hPoints-1)); 
		Distance vStep = appHeight.scale(1.0/(vPoints-1));
		Position[][] grid = new Position[hPoints][vPoints];
		
		for (int i = 0; i < hPoints; i++) {
			for (int j = 0; j < vPoints; j++) {
				grid[i][j] = appSouthWest.
						translateGreatCircle(getNorth(), vStep.scale(j)).
						translateGreatCircle(getEast(), hStep.scale(i));
			}
		}
		
		return grid;
		
	}
	
	@Override
	public List<Position> extractLattice(int hPoints, int vPoints) {
		
		Position[][] grid = extractGrid(hPoints, vPoints);
		List<Position> lst = new ArrayList<Position>();
		for(Position[] line : grid) {
			for(Position p : line) {
				lst.add(p);
			}
		}
		return lst;
	}

	@Override
	public List<Position> extractLattice(Distance hStep, Distance vStep) {
		
		Position startPoint = appSouthWest;
		
		Bearing vBearing = getNorth();
		Bearing hBearing = getEast();
		boolean hMode = true;
		
		List<Position> lst = new ArrayList<Position>();
		
		Position current = startPoint;
		lst.add(current);
		Position next;
		
		while (true) {
			
			if (hMode) next = current.translateGreatCircle(hBearing, hStep);
			else next = current.translateGreatCircle(vBearing, vStep);
			
			if (isWithinBoundaries(next)) {
				current = next;
				lst.add(current);
				if (!hMode) {
					hMode = true;
					hBearing = hBearing.reverse();
				}
			}
			else {
				if(hMode) hMode = false;
				else break;
			}
			
		}
			
		return lst;
	}

	@Override
	public Bearing getNorth() {
		return north;
	}

	@Override
	public Bearing getSouth() {
		return south;
	}

	@Override
	public Bearing getEast() {
		return east;
	}

	@Override
	public Bearing getWest() {
		return west;
	}

	@Override
	public Distance getWidth() {
		return appWidth;
	}
	
	@Override
	public Distance getHeight() {
		return appHeight;
	}

	@Override
	public Map<String, Double> getRelativeCoordinates(Position p) {
		if (!isWithinBoundaries(p)) return null;
		
		Map<String,Double> map = new HashMap<String,Double>();
		Position px = p.projectToLineThrough(appSouthWest, getEast());
		Position py = p.projectToLineThrough(appSouthWest, getNorth());
		
		map.put("X", px.getDistance(appSouthWest).getMeters()/getWidth().getMeters());
		map.put("Y", py.getDistance(appSouthWest).getMeters()/getHeight().getMeters());
		return map;
	}

	@Override
	public Position getRelativePoint(double x, double y) {
		
		Position point = appSouthWest;
		point = point.translateGreatCircle(getEast(), getWidth().scale(x));
		point = point.translateGreatCircle(getNorth(), getHeight().scale(y));
		return point;
	}

	@Override
	public double getTolerance() {
		return tolerance;
	}
	
}
