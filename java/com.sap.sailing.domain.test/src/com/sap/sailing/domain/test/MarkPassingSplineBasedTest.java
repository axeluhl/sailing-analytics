package com.sap.sailing.domain.test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
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
	private class DoubleMatrix {
		// values of the matrix row by row
		private List<List<Double>> matrix = new ArrayList<List<Double>>();
		
		/**
		 * Construct and initialize an {@link DoubleMatrix} of dimensionX to dimensionY values.
		 * @param dimensionX - how many values in x dimension (length of one row)
		 * @param dimensionY - how many values in y direction (length of one column)
		 * @param values - initial values of the matrix ordered row by row
		 */
		public DoubleMatrix(int dimensionX, int dimensionY, double ... values) {
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
		
		public DoubleMatrix(int dimensionX, int dimensionY, List<Double> values) {
			if (values.size() != dimensionX * dimensionY) {
				throw new IllegalArgumentException("Given dimensions of matrix does not match the number of given initial values.");
			}
			for (int y = 0; y < dimensionY; y++) {
				// add one row
				matrix.add(new ArrayList<Double>());
				// fill the row
				for (int x = 0; x < dimensionX; x++) {
					matrix.get(y).add(values.get(y * dimensionX + x));
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

		public DoubleMatrix multiply(DoubleMatrix matrix) {
			if (this.getColumnCount() != matrix.getRowCount()) {
				throw new IllegalArgumentException("Matrices can NOT be multiplied. Row count of the given matrix ( " + matrix.toString() + " ) does NOT equal column count of this matrix (" + this.toString() + ").");
			}
			ArrayList<Double> resultValues = new ArrayList<Double>();
			for (int row = 0; row < this.getRowCount(); row++) {
				for (int col = 0; col < matrix.getColumnCount(); col++) {
					double value = 0.0;
					for (int i = 0; i < this.getColumnCount(); i++) {
						value += this.get(i, row) * matrix.get(col, i);
					}
					resultValues.add(value);
				}
			}
			
			return new DoubleMatrix(matrix.getColumnCount(), this.getRowCount(), resultValues);
		}
		
		public DoubleMatrix multiply(double scalar) {
			ArrayList<Double> resultValues = new ArrayList<Double>();
			for (int x = 0; x < getColumnCount(); x++) {
				for (int y = 0; y < getRowCount(); y++) {
					resultValues.add(get(x,y) * scalar);
				}
			}
			return new DoubleMatrix(getColumnCount(), getRowCount(), resultValues);
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
			return "Vector2D: " + components.toString();
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
		
		private final DoubleMatrix hermiteBaseMatrix = new DoubleMatrix(4, 4, 2, -2, 1, 1, -3, 3, -2, -1, 0, 0, 1, 0, 1, 0, 0, 0);
		private final List<Double> exponents = Arrays.asList(3.0, 2.0, 1.0, 0.0);
		private final DoubleMatrix geometryMatrix;
		
		public HermiteCurve(Vector2D point1, Vector2D point2, Vector2D tangent1, Vector2D tangent2) {
			geometryMatrix = new DoubleMatrix(	/* dimensions */ 2, 4,
												point1.x(), point1.y(),
												point2.x(), point2.y(),
												tangent1.x(), tangent1.y(),
												tangent2.x(), tangent2.y());
		}
		
		public List<Vector2D> intersectWith(StraightLine line) {			
			/*
			 * We start with the hermite spline as a parametric form h(x(t), y(t)) and the straight line in implicit form g(x,y) = 0 with
			 * x(t) being the result of the matrix multiplication of the exponent matrix (T), the hermite base matrix (Mh) and the first column
			 * (which contains the x components of the points and tangents) of the geometry matrix (C) and
			 * y(t) being the result of the same matrix multiplication but using the second row of C instead of the first.
			 * This results in:
			 * x(t) = (2*p0x - 2*p1x + m0x + m1x)*t^3 + (-3*p0x + 3*p1x - 2*m0x - m1x)*t^2 + m0x*t + p0x
			 * y(t) = (2*p0y - 2*p1y + m0y + m1y)*t^3 + (-3*p0y + 3*p1y - 2*m0y - m1y)*t^2 + m0y*t + p0y
			 * We form the implicit line formula by using this definition m(x - a) + n(y - b) = 0 where (m,n) is the perpendicular vector of the line and (a,b) is a known point of the line.
			 * By using (a,b) = (p2x, p2y) and (m,n) = (-dy, dx) (which is a vector perpendicular to d) we get:
			 * g(x, y) = -dy*(x - p2x) + dx*(y - p2y) with p2 being the location vector and d being the direction vector of the straight line
			 * 
			 * To find intersection points, we insert the parametric hermite spline formulas into g(x,y) = 0 -> f(t) = g(x(t), y(t)) = 0
			 * and find the roots of this equation using the approach known as "Cardanische Formeln".
			 * We assume that the numerical error caused by using double as a data type is negligible concerning the mark rounding times calculated from the intersection points.
			 * 
			 */
			// TODO find a quick & efficient way to estimate if the line intersects with the spline to safe the effort if it doesn't.
			// if (!isIntersectingLine(line)) {
			//	 return Collections.emptyList();
			// }
			
			Vector2D p0 = getFirstPoint();
			Vector2D p1 = getSecondPoint();
			Vector2D m0 = getTangentAtFirstPoint();
			Vector2D m1 = getTangentAtSecondPoint();
			Vector2D p2 = line.getLocationVector();
			Vector2D d = line.getDirectionVector();
			// cubic polynomial of the form: s3 * t^3 + s2 * t^2 + s1 * t + s0 = 0
			// sX is the substitution of all factors of t^X (calculated on the blackboard)
			double s3 = -d.y()*(2*p0.x() - 2*p1.x() + m0.x() + m1.x()) + d.x()*(2*p0.y() - 2*p1.y() + m0.y() + m1.y());
			double s2 = -d.y()*(-3*p0.x() + 3*p1.x() - 2*m0.x() - m1.x()) + d.x()*(-3*p0.y() + 3*p1.y() - 2*m0.y() - m1.y());
			double s1 = -d.y()*m0.x() + d.x()*m0.y();
			double s0 = -d.y()*p0.x() + d.x()*p0.y() + d.y()*p2.x() - d.x()*p2.y();
			
			// normalized cubic polynomial of the form: t^3 + a * t^2 + b * t + c = 0
			double a = s2 / s3;
			double b = s1 / s3;
			double c = s0 / s3;
			
			// reduced cubic polynomial of the form: z^3 + p * z + q = 0 with substitution x = z - a/3 in place
			double p = b - Math.pow(a, 2)/3;
			double q = 2.0 * Math.pow(a, 3)/27.0 - a*b/3.0 + c;
			// discriminant = (q/2)^2 + (p/3)^3
			double discriminant = Math.pow(q/2.0, 2) + Math.pow(p/3.0, 3);

			// use the "Cardanische Formeln" to find the roots of this polynomial
			List<Double> zResults = new ArrayList<Double>();
			if (discriminant > 0.0) {
				// According to Vieta's formulas u^3 and v^3 are solutions of t^2 + qt - (p^3 / 27) = 0.
				// u = ((-q / 2) + sqrt(discriminant))^(1/3)
				double u = Math.pow(-q/2 + Math.sqrt(discriminant), 1/3);
				// v = ((-q / 2) - sqrt(discriminant))^(1/3)
				double v = Math.pow(-q/2 - Math.sqrt(discriminant), 1/3);
				zResults.add(u + v);
			} else if (discriminant < 0.0) {
				// z2 = -sqrt(-4/3 * p) * cos(1/3 * arccos(-q/2 * sqrt(-27/p^3)) + pi/3)
				zResults.add(-Math.sqrt(-4.0/3.0*p) * Math.cos(1.0/3.0*Math.acos(-q/2.0*Math.sqrt(-27.0/Math.pow(p, 3))) + Math.PI/3.0));
				// z1 = sqrt(-4/3 * p) * cos(1/3 * arccos(-q/2 * sqrt(-27/p^3)))
				zResults.add(Math.sqrt(-4.0/3.0*p) * Math.cos(1.0/3.0*Math.acos(-q/2.0*Math.sqrt(-27.0/Math.pow(p, 3)))));
				// z3 = -sqrt(-4/3 * p) * cos(1/3 * arccos(-q/2 * sqrt(-27/p^3)) - pi/3)
				zResults.add(-Math.sqrt(-4.0/3.0*p) * Math.cos(1.0/3.0*Math.acos(-q/2.0*Math.sqrt(-27.0/Math.pow(p, 3))) - Math.PI/3.0));
			} else if (discriminant == 0.0) {
				System.out.println("Discriminant is exactly zero!");
				if (p == 0.0 && q == 0.0) {
					// z is always 0 in this case
					zResults.add(0.0);
				} else {
					// z1 = 3q / p
					zResults.add(3.0*q/p);
					// z2 = -3q / 2p
					zResults.add((-3.0*q)/(2.0*p));
				}
			}
			
			// perform back substitution from z to x
			List<Vector2D> results = new ArrayList<Vector2D>();
			for (Double z : zResults) {
				double t = z - a/3;
				if (t >= 0.0 && t <= 1.0) {
					Vector2D intersectionPoint = interpolateCurvePoint(t);
					results.add(intersectionPoint);
				} else {
					System.out.println("Calculated interpolation parameter is out of bounds (" + t + ")");
				}
			}
			return results;
		}

		public Vector2D getTangentAtSecondPoint() {
			return new Vector2D(geometryMatrix.getRow(3));
		}

		public Vector2D getTangentAtFirstPoint() {
			return new Vector2D(geometryMatrix.getRow(2));
		}

		public Vector2D getSecondPoint() {
			return new Vector2D(geometryMatrix.getRow(1));
		}

		public Vector2D getFirstPoint() {
			return new Vector2D(geometryMatrix.getRow(0));
		}

		public Vector2D interpolateCurvePoint(double t) {
			if (t < 0.0 || t > 1.0) {
				throw new IllegalArgumentException("Value of t (" + t + ") is not in the interval of 0.0 to 1.0");
			}
			
			/* 
			 * Points on the hermite spline are described by the matrix multiplication of the exponent matrix (T) multiplied with t,
			 * the hermite base matrix (Mh) and the geometry matrix (C).
			 * p(t) = T * Mh * C 
			 */
			DoubleMatrix exponentMatrix = getExponentMatrix(t);
			DoubleMatrix result = exponentMatrix.multiply(hermiteBaseMatrix.multiply(geometryMatrix));
			return new Vector2D(result.getRow(0));
		}

		private DoubleMatrix getExponentMatrix(double t) {
			ArrayList<Double> exponentMatrixValues = new ArrayList<Double>();
			for (double e : exponents) {
				exponentMatrixValues.add(Math.pow(t, e));
			}
			return new DoubleMatrix(4, 1, exponentMatrixValues);
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
		
		Vector2D point1 = new Vector2D(1.0, 0.0);
		Vector2D point2 = new Vector2D(0.0, 1.0);
		Vector2D tangent1 = new Vector2D(10.0, 0.0);
		Vector2D tangent2 = new Vector2D(-10.0, -10.0);
		StraightLine line = new StraightLine(new Vector2D(10.0, 10.0), new Vector2D(-1.0, -1.0));
		HermiteCurve curve = new HermiteCurve(point1, point2, tangent1, tangent2);
		List<Vector2D> intersections = curve.intersectWith(line);
		
		return markPassings;
	}
}
