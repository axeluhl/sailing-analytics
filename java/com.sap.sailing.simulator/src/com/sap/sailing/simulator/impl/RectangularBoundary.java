package com.sap.sailing.simulator.impl;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.simulator.Boundaries;
import com.sap.sailing.simulator.BoundariesIterator;

public class RectangularBoundary implements Boundaries {

	private Position northWest;
	private Position southEast;
	private Position southWest;
	private Position northEast;
	
	static public Bearing NORTH = new DegreeBearingImpl(0);
	static public Bearing SOUTH = new DegreeBearingImpl(180);
	static public Bearing EAST = new DegreeBearingImpl(90);
	static public Bearing WEST = new DegreeBearingImpl(270);

	public RectangularBoundary(Position northWest, Position southEast) {
		
		this.northWest = northWest;
		this.southEast = southEast;
		
		this.southWest = northWest.projectToLineThrough(southEast, EAST);
		this.northEast = northWest.projectToLineThrough(southEast, NORTH);
		
	}

	@Override
	public Position[] getCorners() {

		return new Position[] { northWest, northEast, southEast, southWest };

	}

	@Override
	public boolean isWithinBoundaries(Position p) {
		
		Position northProjection = p.projectToLineThrough(northWest, EAST);
		Position southProjection = p.projectToLineThrough(southWest, EAST);
		Position westProjection = p.projectToLineThrough(northWest, NORTH);
		Position eastProjection = p.projectToLineThrough(northEast, NORTH);
		
		Distance northSouth = northProjection.getDistance(southProjection);
		Distance eastWest = eastProjection.getDistance(westProjection);
		
		return 
				( northSouth.compareTo(p.getDistance(northProjection)) >= 0 )
				&& ( northSouth.compareTo(p.getDistance(southProjection)) >= 0 )
				&& ( eastWest.compareTo(p.getDistance(eastProjection)) >= 0 )
				&& ( eastWest.compareTo(p.getDistance(westProjection)) >=0 );
		
	}

	@Override
	public BoundariesIteratorImpl boundariesIterator() {
	
		return new BoundariesIteratorImpl(this); 
	}

	public Distance getHeight() {
		return northWest.getDistance(southWest);
	}
	
	public Distance getBottomWidth() {
		return southWest.getDistance(southEast);
	}
	
	public Distance getTopWidth() {
		return northWest.getDistance(northEast);
	}

	private class BoundariesIteratorImpl implements BoundariesIterator {
	
		private RectangularBoundary parentBoundary;
		
		private Position currentPosition;
		
		private Distance verticalStep;
		private Distance horizontalStep;
		
		private Bearing verticalBearing;
		private Bearing horizontalBearing;
		
		private boolean horizontalTranslation;
		
		public BoundariesIteratorImpl(RectangularBoundary parent) {
			
			this.parentBoundary = parent;
			
			this.verticalStep = parentBoundary.getHeight().scale(0.1);
			this.horizontalStep = parentBoundary.getTopWidth().scale(0.1);
			
			this.verticalBearing = NORTH;
			this.horizontalBearing = EAST;
			
			this.currentPosition = parentBoundary.southWest.translateGreatCircle(
					parentBoundary.southWest.getBearingGreatCircle(parentBoundary.northEast), 
					parentBoundary.southWest.getDistance(parentBoundary.northEast).scale(0.01));
			
			this.horizontalTranslation = true;
			
		}
		
	
		@Override
		public boolean hasNext() {

			if (horizontalTranslation) return isWithinBoundaries(currentPosition.translateGreatCircle(horizontalBearing, horizontalStep));
			else return isWithinBoundaries(currentPosition.translateGreatCircle(verticalBearing, verticalStep));
		}
	
		@Override
		public Position next() {
			
			if (hasNext()) {
				
				if (horizontalTranslation) {				
					currentPosition = currentPosition.translateGreatCircle(horizontalBearing, horizontalStep);
					horizontalTranslation = isWithinBoundaries(currentPosition.translateGreatCircle(horizontalBearing, horizontalStep));
					return currentPosition;
				}
				
				else {
					currentPosition = currentPosition.translateGreatCircle(verticalBearing, verticalStep);
					horizontalTranslation = true;
					horizontalBearing = horizontalBearing.reverse();
					return currentPosition;
				}
			}
			
			else return null;
			
		}
	
		@Override
		public Distance getVerticalStep() {
			
			return verticalStep;
		}
	
		@Override
		public Distance getHorizontalStep() {
			
			return horizontalStep;
		}
			
		@Override
		public void setHorizontalResolution(double xRes) {
			
			horizontalStep = parentBoundary.getTopWidth().scale(1.0/xRes);
		}
		
		@Override
		public void setVerticalResolution(double yRes) {
			
			verticalStep = parentBoundary.getHeight().scale(1.0/yRes);
		}
		
		@Override
		public void setVerticalStep(Distance newVerticalStep) {

			verticalStep = newVerticalStep;
		}

		@Override
		public void setHorizontalStep(Distance newHorizontalStep) {

			horizontalStep = newHorizontalStep;
		}

		@Override
		public void reset() {		
			verticalBearing = NORTH;
			horizontalBearing = EAST;
			horizontalTranslation = true;
			currentPosition = parentBoundary.southWest.translateGreatCircle(
					parentBoundary.southWest.getBearingGreatCircle(parentBoundary.northEast), 
					parentBoundary.southWest.getDistance(parentBoundary.northEast).scale(0.01));
		}
		
	}

}
