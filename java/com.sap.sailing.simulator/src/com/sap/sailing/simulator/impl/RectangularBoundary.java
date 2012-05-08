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
	
	private Bearing north;
	private Bearing south;
	private Bearing east;
	private Bearing west;
	
	private Distance width;
	private Distance height;
	
	public RectangularBoundary(Position p1, Position p2) {
				
		north = p2.getBearingGreatCircle(p1);
		south = north.reverse();
		east = north.add(TRUEEAST);
		west = north.add(TRUEWEST);
		
		Distance dist = p1.getDistance(p2);
		Position pp1 = p1.translateGreatCircle(getNorth(), dist.scale(0.1));
		Position pp2 = p2.translateGreatCircle(getSouth(), dist.scale(0.1)) ;
		
		height = p1.getDistance(pp2);
		width = height.scale(2);
		
		northWest = pp1.translateGreatCircle(west, width.scale(0.5));
		northEast = pp1.translateGreatCircle(east, width.scale(0.5));
		southEast = northEast.translateGreatCircle(south, height);
		southWest = northWest.translateGreatCircle(south, height);

	}

	@Override
	public Map<String, Position> getCorners() {

		Map<String, Position> map = new HashMap<String, Position>();
		map.put("NorthWest", northWest);
		map.put("SouthWest", southWest);
		map.put("SouthEast", southEast);
		map.put("NorthEast", northEast);
		
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
	public List<Position> extractLattice(int hPoints, int vPoints) {
		
		return extractLattice(width.scale(1.0/hPoints), height.scale(1.0/vPoints));
		
	}

	@Override
	public List<Position> extractLattice(Distance hStep, Distance vStep) {
		
		Bearing diagBearing = southWest.getBearingGreatCircle(northEast);
		Distance diag = southWest.getDistance(northEast);
		Position startPoint = southWest.translateGreatCircle(diagBearing, diag.scale(0.01));
		
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
		return width;
	}
	
	@Override
	public Distance getHeight() {
		return height;
	}

	@Override
	public Map<String, Double> getRelativeCoordinates(Position p) {
		if (!isWithinBoundaries(p)) return null;
		
		Map<String,Double> map = new HashMap<String,Double>();
		Position px = p.projectToLineThrough(southWest, getEast());
		Position py = p.projectToLineThrough(southWest, getNorth());
		
		map.put("X", px.getDistance(southWest).getMeters()/getWidth().getMeters());
		map.put("Y", py.getDistance(southWest).getMeters()/getHeight().getMeters());
		return map;
	}

	@Override
	public Position getRelativePoint(double x, double y) {
		
		Position point = southWest;
		point = point.translateGreatCircle(getEast(), getWidth().scale(x));
		point = point.translateGreatCircle(getNorth(), getHeight().scale(y));
		return point;
	}
	
}
