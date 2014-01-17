package com.sap.sailing.domain.markpassingcalculation.splining;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An hermite curve that interpolates points of the curve between its known starting and end point
 * by using only these points and the tangents of the curve at these two points as input parameters.
 * @author Martin Hanysz
 *
 */
public class HermiteCurve {
	
	private final DoubleMatrix hermiteBaseMatrix = new DoubleMatrix(4, 4, 2, -2, 1, 1, -3, 3, -2, -1, 0, 0, 1, 0, 1, 0, 0, 0);
	private final List<Double> exponents = Arrays.asList(3.0, 2.0, 1.0, 0.0);
	private final DoubleMatrix geometryMatrix;
	
	/**
	 * Instantiates a hermite curve from the point identified by the given {@link Vector2D} point1 to the given {@link Vector2D} point2
	 * with the tangents given by the {@link Vector2D}s tangent1 and tangent2 at point1 and point 2 respectively.
	 * @param point1 - the {@link Vector2D} that represents the starting point of the hermite curve
	 * @param point2 - the {@link Vector2D} that represents the end point of the hermite curve
	 * @param tangent1 - the {@link Vector2D} that represents the tangent at the starting point of the hermite curve
	 * @param tangent2 - the {@link Vector2D} that represents the tangent at the end point of the hermite curve
	 */
	public HermiteCurve(Vector2D point1, Vector2D point2, Vector2D tangent1, Vector2D tangent2) {
		geometryMatrix = new DoubleMatrix(	/* dimensions */ 2, 4,
											point1.x(), point1.y(),
											point2.x(), point2.y(),
											tangent1.x(), tangent1.y(),
											tangent2.x(), tangent2.y());
	}
	
	/**
	 * Intersects this hermite curve with the given {@link StraightLine}.
	 * @param line - the {@link StraightLine} to intersect this hermite curve with
	 * @return the {@link List} of {@link Vector2D}s representing the intersection points if they exist, an empty {@link List} if no intersections exist
	 */
	public Map<Vector2D, Double> intersectWith(StraightLine line) {			
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
		// TODO find a quick & efficient way to estimate if the line intersects with the spline to save the effort if it doesn't.
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
			// System.out.println("Discriminant is exactly zero!");
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
		Map<Vector2D, Double> results = new HashMap<Vector2D, Double>();
		for (Double z : zResults) {
			double t = z - a/3;
			if (t >= 0.0 && t <= 1.0) {
				Vector2D intersectionPoint = interpolateCurvePoint(t);
				results.put(intersectionPoint, t);
			} else {
				// System.out.println("Calculated interpolation parameter is out of bounds (" + t + ")");
			}
		}
		return results;
	}

	/**
	 * Returns the {@link Vector2D} representing the tangent vector at the end point of this hermite curve.
	 * @return the {@link Vector2D} representing the tangent vector at the end point of this hermite curve
	 */
	public Vector2D getTangentAtSecondPoint() {
		return new Vector2D(geometryMatrix.getRow(3));
	}

	/**
	 * Returns the {@link Vector2D} representing the tangent vector at the starting point of this hermite curve.
	 * @return the {@link Vector2D} representing the tangent vector at the starting point of this hermite curve
	 */
	public Vector2D getTangentAtFirstPoint() {
		return new Vector2D(geometryMatrix.getRow(2));
	}

	/**
	 * Returns the {@link Vector2D} representing the end point of this hermite curve.
	 * @return the {@link Vector2D} representing the end point of this hermite curve
	 */
	public Vector2D getSecondPoint() {
		return new Vector2D(geometryMatrix.getRow(1));
	}

	/**
	 * Returns the {@link Vector2D} representing the starting point of this hermite curve.
	 * @return the {@link Vector2D} representing the starting point of this hermite curve
	 */
	public Vector2D getFirstPoint() {
		return new Vector2D(geometryMatrix.getRow(0));
	}

	/**
	 * Interpolates the point of the hermite curve at the interpolation parameter t with t being in the interval of <code>0.0</code> to <code>1.0</code>.
	 * Using t = 0.0 will yield the starting point of this hermite curve and t = 1.0 will yield the end point of it.
	 * @param t - the interpolation parameter to interpolate the point at
	 * @return the {@link Vector2D} representing the point of the hermite curve at the interpolation parameter t
	 */
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

	/**
	 * Returns the exponent matrix of this hermite curve for the given interpolation parameter t.
	 * It looks like <code>[t^3 t^2 t^1 t^0]</code>.
	 * @param t - the interpolation parameter to calculate the exponent matrix for
	 * @return the {@link DoubleMatrix} representing the exponent matrix for the interpolation parameter t
	 */
	private DoubleMatrix getExponentMatrix(double t) {
		ArrayList<Double> exponentMatrixValues = new ArrayList<Double>();
		for (double e : exponents) {
			exponentMatrixValues.add(Math.pow(t, e));
		}
		return new DoubleMatrix(4, 1, exponentMatrixValues);
	}
	
	@Override
	public String toString() {
		String p1 = "p1 = (" + getFirstPoint().x() + "/" + getFirstPoint().y() + ")";
		String p2 = "p2 = (" + getSecondPoint().x() + "/" + getSecondPoint().y() + ")";
		String t1 = "t1 = (" + getTangentAtFirstPoint().x() + "/" + getTangentAtFirstPoint().y() + ")";
		String t2 = "t2 = (" + getTangentAtSecondPoint().x() + "/" + getTangentAtSecondPoint().y() + ")";
		return "Hermite Curve: " + p1 + ", " + p2 + ", " + t1 + ", " + t2;
	}
}