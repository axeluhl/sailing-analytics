package com.sap.sailing.simulator.impl;

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
	
	public RectangularBoundary(Position p1, Position p2) {

	}

	@Override
	public Map<String, Position> getCorners() {

		return null;

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Position> extractLattice(Distance hStep, Distance vstep) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bearing getNorth() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bearing getSouth() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bearing getEast() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bearing getWest() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Distance getWidth() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Distance getHeight() {
		return northWest.getDistance(southWest);
	}
	

}
