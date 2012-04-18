package com.sap.sailing.simulator.impl;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.simulator.Boundaries;
import com.sap.sailing.simulator.BoundariesIterator;

public class RectangularBoundary implements Boundaries {

	private Position northWest;
	private Position southEast;
	private Position southWest;
	private Position northEast;
	
	private Distance height;
	private Distance width;
	
	static public enum Directions {UP, DOWN, RIGHT, LEFT};

	public RectangularBoundary(Position northWest, Position southEast) {
		super();
		
		this.northWest = northWest;
		this.southEast = southEast;
		
		this.southWest = northWest.projectToLineThrough(southEast, new DegreeBearingImpl(90));
		this.northEast = northWest.projectToLineThrough(southEast, new DegreeBearingImpl(0));
		
		this.height = northWest.getDistance(southWest);
		this.width  = northWest.getDistance(northEast);
	}

	@Override
	public Position[] getCorners() {

		return new Position[] { northWest, northEast, southEast, southWest };

	}

	@Override
	public boolean isWithinBoundaries(Position p) {
		
		Position northProjection = p.projectToLineThrough(northWest, new DegreeBearingImpl(90));
		Position southProjection = p.projectToLineThrough(southWest, new DegreeBearingImpl(90));
		Position westProjection = p.projectToLineThrough(northWest, new DegreeBearingImpl(0));
		Position eastProjection = p.projectToLineThrough(northEast, new DegreeBearingImpl(0));
		
		return 
				( height.compareTo(p.getDistance(northProjection)) >= 0 )
				&& ( height.compareTo(p.getDistance(southProjection)) >= 0 )
				&& ( width.compareTo(p.getDistance(eastProjection)) >= 0 )
				&& ( width.compareTo(p.getDistance(westProjection)) >=0 );
		
	}

	@Override
	public BoundariesIterator boundariesIterator() {
	
		return new BoundariesIteratorImpl(this); 
	}

	public static void main(String[] args) throws Exception {
		Boundaries bnd = new RectangularBoundary(new DegreePosition(40,30), new DegreePosition(30,40));
		
		BoundariesIterator bi = bnd.boundariesIterator();
		
		while(bi.hasNext()) {
			Position P = bi.next();
			System.out.println(P.toString()+" "+bnd.isWithinBoundaries(P));
		}
		
		

	}


	private class BoundariesIteratorImpl implements BoundariesIterator {
	
		private RectangularBoundary parentBoundary;
		
		private Position currentPosition;
		
		private Distance verticalStep;
		private Distance horizontalStep;
		
		private Bearing verticalBearing;
		private Bearing horizontalBearing;
		
		private Directions direction; 
		
		public BoundariesIteratorImpl(RectangularBoundary parent) {
			
			this.parentBoundary = parent;
			this.currentPosition = parentBoundary.getCorners()[3];
			
			this.verticalStep = parentBoundary.height.scale(0.1);
			this.horizontalStep = parentBoundary.width.scale(0.1);
			
			this.verticalBearing = new DegreeBearingImpl(0);
			this.horizontalBearing = new DegreeBearingImpl(90);
			
			direction = RectangularBoundary.Directions.RIGHT;
			
		}
		
		@Override
		public boolean hasUp() {
			
			return parentBoundary.isWithinBoundaries(currentPosition.translateGreatCircle(verticalBearing, verticalStep));
			
		}
	
		@Override
		public boolean hasDown() {
	
			return parentBoundary.isWithinBoundaries(currentPosition.translateGreatCircle(verticalBearing.reverse(), verticalStep));
		}
	
		@Override
		public boolean hasLeft() {

			return parentBoundary.isWithinBoundaries(currentPosition.translateGreatCircle(horizontalBearing.reverse(), horizontalStep));
		}
	
		@Override
		public boolean hasRight() {
			
			return parentBoundary.isWithinBoundaries(currentPosition.translateGreatCircle(horizontalBearing, horizontalStep));
		}
	
		@Override
		public boolean hasNext() {

			switch(direction) {
			case RIGHT: return hasRight();
			case LEFT: return hasLeft();
			case UP: return hasUp();
			case DOWN: return hasDown();
			default: return false;
			}
		}
	
		@Override
		public Position up() throws Exception {
		
			if (this.hasUp()) return (currentPosition=currentPosition.translateGreatCircle(verticalBearing, verticalStep));
			else throw new Exception("Nowhere to go UP!");
		}
	
		@Override
		public Position down() throws Exception {
			
			if (this.hasDown()) return (currentPosition=currentPosition.translateGreatCircle(verticalBearing.reverse(), verticalStep));
			else throw new Exception("Nowhere to go DOWN!");
		}
	
		@Override
		public Position left() throws Exception {
			
			if (this.hasLeft()) return (currentPosition=currentPosition.translateGreatCircle(horizontalBearing.reverse(), horizontalStep));
			else throw new Exception("Nowhere to go LEFT!");
		}
	
		@Override
		public Position right() throws Exception {
			
			if (this.hasRight()) return (currentPosition=currentPosition.translateGreatCircle(horizontalBearing, horizontalStep));
			else throw new Exception("Nowhere to go RIGHT!");
		}
	
		@Override
		public Position next() throws Exception {
			
			if (hasNext()) {
				switch(direction) {
				case RIGHT: right();
				if(!hasRight()) direction = RectangularBoundary.Directions.UP;
				break;
				case LEFT: left();
				if(!hasLeft()) direction = RectangularBoundary.Directions.UP;
				break;
				case UP: up();
				if (hasLeft()) direction = RectangularBoundary.Directions.LEFT;
				else direction = RectangularBoundary.Directions.RIGHT;
				break;
				case DOWN: down();
				}
				
				return currentPosition;
			}
			else throw new Exception("No NEXT point!");
			
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
		public Bearing getVerticalBearing() {
	
			return verticalBearing;
		}
	
		@Override
		public Bearing getHorizontalBearing() {
			
			return horizontalBearing;
		}
		
		@Override
		public void setHorizontalResolution(double xRes) {
			
			horizontalStep = parentBoundary.width.scale(1.0/xRes);
		}
		
		@Override
		public void setVerticalResolution(double yRes) {
			
			verticalStep = parentBoundary.height.scale(1.0/yRes);
		}
		
		@Override
		public void reset() {
			
			currentPosition = parentBoundary.getCorners()[3];
			direction = RectangularBoundary.Directions.RIGHT;
			
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
		public void setVerticalBearing(Bearing newVerticalBearing) {

			verticalBearing = newVerticalBearing;
		}

		@Override
		public void setHorizontalBearing(Bearing newHorizontalBearing) {

			horizontalBearing = newHorizontalBearing;
		}
		
	}

}
