package com.sap.sailing.domain.test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.MarkPassing;

/**
 * This class tests a mark passing detection algorithm that interpolates the boats' positions between
 * their GPS fixes.
 * @author Martin Hanysz
 *
 */
public class MarkPassingSplineBasedTest extends AbstractMarkPassingTest {
	
	/**
	 * A matrix of doubles.
	 * @author Martin Hanysz
	 *
	 */
	private class DoubleMartix {
		// values of the matrix row by row
		private List<List<Double>> matrix = new ArrayList<List<Double>>();
		
		/**
		 * Construct and initialize an {@link DoubleMartix} of dimensionX to dimensionY values.
		 * @param dimensionX - how many values in x dimension (length of one row)
		 * @param dimensionY - how many values in y direction (length of one column)
		 * @param values - initial values of the matrix ordered row by row
		 */
		public DoubleMartix(int dimensionX, int dimensionY, double ... values) {
			if (values.length != dimensionX * dimensionY) {
				throw new IllegalArgumentException("Given dimensions of matrix does not match the number of given initial values.");
			}
			for (int y = 0; y < dimensionY; y++) {
				// add one row
				matrix.add(new ArrayList<Double>());
				// fill the row
				for (int x = 0; x < dimensionX; x++) {
					matrix.get(y).add(values[y * dimensionX + x]);
				}
			}
		}
		
		public double get(int x, int y) {
			return matrix.get(y).get(x);
		}

		public int getRowCount() {
			return matrix.size();
		}
		
		public int getColumnCount() {
			return matrix.get(0).size();
		}

		public String toString() {
			String result = "DoubleMatrix: ";
			for (List<Double> row : matrix) {
				result += row.toString();
			}
			return result;
		}

		public List<Double> getColumn(int i) {
			if (i > matrix.get(0).size()) {
				throw new IllegalArgumentException("The matrix does not contain the given amount of columns");
			}
			ArrayList<Double> column = new ArrayList<Double>();
			for (List<Double> row : matrix) {
				column.add(row.get(i));
			}
			return column;
		}
		
		public List<Double> getRow(int i) {
			if (i > matrix.size()) {
				throw new IllegalArgumentException("The matrix does not contain the given amount of rows");
			}
			return matrix.get(i);
		}
	}
	
	private class Vector2D {
		private List<Double> components;
		
		public Vector2D(double ... components) {
			this.components = new ArrayList<Double>(2);
			for (int i = 0; i < 2; i++) {
				this.components.add(components[i]);
			}
		}
		
		public Vector2D(List<Double> components) {
			this.components = new ArrayList<Double>();
			for (int i = 0; i < 2; i++) {
				this.components.add(components.get(i));
			}
		}
		
		public Vector2D getPerpendicularVector() {
			return new Vector2D(-this.y(), this.y());
		}
		
		public Vector2D add(Vector2D v) {
			return new Vector2D(x() + v.x(), y() + v.y());
		}
		
		public Vector2D subtract(Vector2D v) {
			return new Vector2D(x() - v.x(), y() - v.y());
		}
		
		public Vector2D multiply(double scalar) {
			return new Vector2D(x() * scalar, y() * scalar);
		}
		
		public Vector2D divide(double scalar) {
			return multiply(1.0/scalar);
		}
		
		public double dotProduct(Vector2D v) {
			return x()*v.x() + y()*v.y();
		}
		
		public double getLength() {
			return Math.sqrt(Math.pow(x(), 2) + Math.pow(y(), 2));
		}
		
		public double getDistanceToPoint(Vector2D p) {
			return p.subtract(this).getLength();
		}
		
		public double get(int i) {
			return components.get(i);
		}
		
		public double x() {
			return components.get(0);
		}
		
		public double y() {
			return components.get(1);
		}

		public void set(int i, double d) {
			components.set(i, d);
		}
		
		public String toString() {
			return "GeometricalVector: " + components.toString();
		}

		public Vector2D normalize() {
			return divide(getLength());
		}
	}
	
	private class StraightLine {
		private final Vector2D locationVector;
		private final Vector2D directionVector;
		
		public StraightLine(Vector2D locationVector, Vector2D directionVector) {
			this.locationVector = locationVector;
			this.directionVector = directionVector;
		}
		
		public Vector2D getLocationVector() {
			return locationVector;
		}
		
		public Vector2D getDirectionVector() {
			return directionVector;
		}

		public double getDistanceToPoint(Vector2D point) {
			Vector2D normal = getDirectionVector().getPerpendicularVector();
			// y = z - ((z-r)*n)/(n*n) * n with z = point
			Vector2D perpendicularFoot = point.subtract(normal.multiply(point.subtract(getDirectionVector()).dotProduct(normal) / normal.dotProduct(normal)));
			// d = |y - z|
			return perpendicularFoot.getDistanceToPoint(point);
		}
		
		public String toString() {
			String p = "p = (" + getLocationVector().x() + "/" + getLocationVector().y() + ")";
			String d = "d = (" + getDirectionVector().x() + "/" + getDirectionVector().y() + ")";
			return "StraightLine: " + p + ", " + d;
		}
	}
	
	private class HermiteCurve {
		
		private final DoubleMartix hermiteBaseMatrix = new DoubleMartix(4, 4, 2, -3, 0, 1, -2, 3, 0, 0, 1, -2, 1, 0, 1, -1, 0, 0);
		private final DoubleMartix exponentMatrix = new DoubleMartix(4, 1, 3, 2, 1, 0);
		private final DoubleMartix geometryMatrix;
		
		public HermiteCurve(Vector2D point1, Vector2D point2, Vector2D tangent1, Vector2D tangent2) {
			geometryMatrix = new DoubleMartix(	/* dimensions */ 4, 2,
												/* 1st row */ point1.get(0), point2.get(0), tangent1.get(0), tangent2.get(0),
												/* 2nd row */ point1.get(1), point2.get(1), tangent1.get(1), tangent2.get(1));
		}
		
		public List<Vector2D> intersectWith(StraightLine line) {
			// If it doesn't they do not intersect and we can save the effort to calculate it by solving cubic polynomials
			if (!isIntersectingLine(line)) {
				return Collections.emptyList();
			}
			Vector2D l = line.getLocationVector();
			Vector2D n = line.getDirectionVector();
			Vector2D p1 = getFirstPoint();
			Vector2D p2 = getSecondPoint();
			Vector2D t1 = getTangentAtFirstPoint();
			Vector2D t2 = getTangentAtSecondPoint();
			
			// use a numerical method to find the values for t, where the hermite curve and the line intersect
			// cubic polynomial of the form: m3 * t^3 + m2 * t^2 + m1 * t + mo = 0
			double m3 = 2.0*p1.x()*n.x() - 2.0*p2.x()*n.x() + t1.x()*n.x() + t2.x()*n.x() + 2.0*p1.y()*n.y() - 2.0*p2.y()*n.y() + t1.y()*n.y() + t2.y()*n.y();
			double m2 = -3.0*p1.x()*n.x() + 3.0*p2.x()*n.x() - 2*t1.x()*n.x() - t2.x()*n.x() - 3.0*p1.y()*n.y() + 3.0*p2.y()*n.y() - 2*t1.y()*n.y() - t2.y()*n.y();
			double m1 = t1.x()*n.x() + t1.y()*n.y();
			double m0 = p1.x()*n.x() + p1.y()*n.y() - l.x()*n.x() + l.y()*n.y();
			
			// normalized cubic polynomial of the form: t^3 + a * t^2 + b * t + c = 0
			double a = m2 / m3;
			double b = m1 / m3;
			double c = m0 / m3;
			
			// reduced cubic polynomial of the form: z^3 + p * z + q = 0 with substitution x = z - a/3 in place
			double p = b - Math.pow(a, 2)/3;
			double q = 2.0 * Math.pow(a, 3)/27.0 - a*b/3.0 + c;
			// discriminant = 18abcd - 4b^3d + b^2c^2 - 4ac^3 - 27a^2d^2
			// double discriminant = 18 * a * b * c * d - 4 * Math.pow(b, 3) * d + Math.pow(b, 2) * Math.pow(c, 2) - 4 * a * Math.pow(c, 3) - 27 * Math.pow(a, 2) * Math.pow(d, 2);
			double discriminant = Math.pow(q/2.0, 2) + Math.pow(p/3.0, 3);

			List<Double> zResults = new ArrayList<Double>();
			if (discriminant > 0.0) {
				zResults.add(0.0);
			} else if (discriminant < 0.0) {
				// z2 = -sqrt(-4/3 * p) * cos(1/3 * arccos(-q/2 * sqrt(-27/p^3)) + pi/3)
				zResults.add(-Math.sqrt(-4.0/3.0*p) * Math.cos(1.0/3.0*Math.acos(-q/2.0*Math.sqrt(-27.0/Math.pow(p, 3))) + Math.PI/3.0));
				// z1 = sqrt(-4/3 * p) * cos(1/3 * arccos(-q/2 * sqrt(-27/p^3)))
				zResults.add(Math.sqrt(-4.0/3.0*p) * Math.cos(1.0/3.0*Math.acos(-q/2.0*Math.sqrt(-27.0/Math.pow(p, 3)))));
				// z3 = -sqrt(-4/3 * p) * cos(1/3 * arccos(-q/2 * sqrt(-27/p^3)) - pi/3)
				zResults.add(-Math.sqrt(-4.0/3.0*p) * Math.cos(1.0/3.0*Math.acos(-q/2.0*Math.sqrt(-27.0/Math.pow(p, 3))) - Math.PI/3.0));
			} else if (discriminant == 0.0) {
				// TODO does this ever happen? -> are doubles precise enough to allow this case?
				System.out.println("Discriminant is exactly zero!");
				if (p == 0.0 && q == 0.0) {
					zResults.add(0.0);
				} else {
					zResults.add(3.0*q/p);
					zResults.add((-3.0*q)/(2.0*p));
				}
			}
			
			// perform back substitution from z to x
			List<Vector2D> results = new ArrayList<Vector2D>();
			for (Double z : zResults) {
				Vector2D intersectionPoint = interpolateCurvePoint(z - a/3);
				results.add(intersectionPoint);
			}
			return results;
		}

		public Vector2D getTangentAtSecondPoint() {
			return new Vector2D(geometryMatrix.getColumn(3));
		}

		public Vector2D getTangentAtFirstPoint() {
			return new Vector2D(geometryMatrix.getColumn(2));
		}

		public Vector2D getSecondPoint() {
			return new Vector2D(geometryMatrix.getColumn(1));
		}

		public Vector2D getFirstPoint() {
			return new Vector2D(geometryMatrix.getColumn(0));
		}

		public boolean isIntersectingLine(StraightLine line) {
			if (	line.getDistanceToPoint(getFirstPoint()) == line.getDistanceToPoint(getSecondPoint()) + getFirstPoint().getDistanceToPoint(getSecondPoint()) ||
					line.getDistanceToPoint(getSecondPoint()) == line.getDistanceToPoint(getFirstPoint()) + getFirstPoint().getDistanceToPoint(getSecondPoint())) {
				// line passes both points at the same side -> no intersection
				return false;
			}
			return true;
		}

		public Vector2D interpolateCurvePoint(double t) {
			if (t < 0.0 || t > 1.0) {
				throw new IllegalArgumentException("Value of t is not in the interval of 0.0 to 1.0");
			}
			// calculate the hermite base functions 1 to 4
			List<Double> hermiteFunctionResults = new ArrayList<Double>();
			for (int i = 0; i < hermiteBaseMatrix.getRowCount(); i++) {
				double hermiteFunctionResult = 	(hermiteBaseMatrix.get(0,i) * Math.pow(t, exponentMatrix.get(0,0))) + 
												(hermiteBaseMatrix.get(1,i) * Math.pow(t, exponentMatrix.get(1,0))) +
												(hermiteBaseMatrix.get(2,i) * Math.pow(t, exponentMatrix.get(2,0))) +
												(hermiteBaseMatrix.get(3,i) * Math.pow(t, exponentMatrix.get(3,0)));
				hermiteFunctionResults.add(hermiteFunctionResult);
			}
			
			// multiply columns of geometry matrix (point and tangent vectors) with hermite base function results and add them
			Vector2D point = new Vector2D(0.0, 0.0);
			
			for (int j = 0; j < geometryMatrix.getColumnCount(); j++) {
				point.set(0, point.get(0) + (geometryMatrix.get(j, 0) * hermiteFunctionResults.get(j)));
				point.set(1, point.get(1) + (geometryMatrix.get(j, 1) * hermiteFunctionResults.get(j)));
			}
			return point;
		}
		
		public String toString() {
			String p1 = "p1 = (" + getFirstPoint().x() + "/" + getFirstPoint().y() + ")";
			String p2 = "p2 = (" + getSecondPoint().x() + "/" + getSecondPoint().y() + ")";
			String t1 = "t1 = (" + getTangentAtFirstPoint().x() + "/" + getTangentAtFirstPoint().y() + ")";
			String t2 = "t2 = (" + getTangentAtSecondPoint().x() + "/" + getTangentAtSecondPoint().y() + ")";
			return "Hermite Curve: " + p1 + ", " + p2 + ", " + t1 + ", " + t2;
		}
	}

	public MarkPassingSplineBasedTest() throws MalformedURLException, URISyntaxException {
		super();
	}

	/* (non-Javadoc)
	 * @see com.sap.sailing.domain.test.AbstractMarkPassingTest#computeMarkPassings()
	 */
	@Override
	Map<Competitor, Map<Waypoint, List<List<MarkPassing>>>> computeMarkPassings() {
		Map<Competitor, Map<Waypoint, List<List<MarkPassing>>>> markPassings = new HashMap<>();
		
		Vector2D point1 = new Vector2D(0.0, 0.0);
		Vector2D point2 = new Vector2D(1.0, 1.0);
		Vector2D tangent1 = new Vector2D(1.0, 0.0);
		Vector2D tangent2 = new Vector2D(0.0, 1.0);
		StraightLine line = new StraightLine(new Vector2D(0.0, 1.0), new Vector2D(1.0, -1.0));
		HermiteCurve curve = new HermiteCurve(point1, point2, tangent1, tangent2);
		List<Vector2D> intersections = curve.intersectWith(line);
		
		return markPassings;
	}
}
