package com.sap.sailing.domain.markpassingcalculation.splining;

/**
 * A straight line, represented by a location and a direction vector.
 * @author Martin Hanysz
 *
 */
public class StraightLine {
	private final Vector2D locationVector;
	private final Vector2D directionVector;
	
	/**
	 * Instantiates a {@link StraightLine} through the given location vector and
	 * going into the direction of the given direction vector.
	 * @param locationVector - a {@link Vector2D} representing a known point of the {@link StraightLine}
	 * @param directionVector - a {@link Vector2D} representing the direction of the {@link StraightLine}
	 */
	public StraightLine(Vector2D locationVector, Vector2D directionVector) {
		this.locationVector = locationVector;
		this.directionVector = directionVector;
	}
	
	/**
	 * Returns the location vector of this straight line.
	 * @return the location vector of this straight line
	 */
	public Vector2D getLocationVector() {
		return locationVector;
	}
	
	/**
	 * Returns the direction vector of this straight line.
	 * @return the direction vector of this straight line
	 */
	public Vector2D getDirectionVector() {
		return directionVector;
	}

	/**
	 * Calculates the shortest distance from this straight line to the point identified by the given {@link Vector2D}.
	 * @param point - a {@link Vector2D} representing the point to calculate the distance to
	 * @return the shortest distance to the given point
	 */
	public double getDistanceToPoint(Vector2D point) {
		Vector2D normal = getDirectionVector().getPerpendicularVector();
		// y = z - ((z-r)*n)/(n*n) * n with z = point
		Vector2D perpendicularFoot = point.subtract(normal.multiply(point.subtract(getDirectionVector()).dotProduct(normal) / normal.dotProduct(normal)));
		// d = |y - z|
		return perpendicularFoot.getDistanceToPoint(point);
	}
	
	@Override
	public String toString() {
		String p = "p = (" + getLocationVector().x() + "/" + getLocationVector().y() + ")";
		String d = "d = (" + getDirectionVector().x() + "/" + getDirectionVector().y() + ")";
		return "StraightLine: " + p + ", " + d;
	}
}