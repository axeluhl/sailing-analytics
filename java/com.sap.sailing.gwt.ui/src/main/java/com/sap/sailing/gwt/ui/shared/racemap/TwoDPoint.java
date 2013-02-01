package com.sap.sailing.gwt.ui.shared.racemap;

public class TwoDPoint {
    private final double x;
    private final double y;

    public TwoDPoint(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double distanceBetween(final TwoDPoint point) {
        return distanceBetween(this.x, this.y, point.getX(), point.getY());
    }

    public double distanceBetween(final double x, final double y) {
        return distanceBetween(this.x, this.y, x, y);
    }

    public static double distanceBetween(final double x1, final double y1, final double x2, final double y2) {
        return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
    }

    public static double distanceBetween(final TwoDPoint point1, final TwoDPoint point2) {
        return distanceBetween(point1.getX(), point1.getY(), point2.getX(), point2.getY());
    }

    @Override
    public String toString() {
        return "Point @(" + this.x + "," + this.y + ")";
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        final TwoDPoint other = (TwoDPoint) obj;
        return this.x == other.getX() && this.y == other.getY();
    }

    @Override
    public int hashCode() {
        return 31 * (new Double(this.x).hashCode()) * (new Double(this.y).hashCode());
    }

    public static TwoDPoint getDistancedPoint(final TwoDPoint distancedFromPoint, final double length, final TwoDPoint startPoint) {
        final TwoDVector vector = new TwoDVector(startPoint, distancedFromPoint).normalize().multiplyScalar(length);

        final double x = distancedFromPoint.getX() + vector.getRe();
        final double y = distancedFromPoint.getY() + vector.getIm();

        return new TwoDPoint(x, y);
    }

    public static TwoDPoint getBisectingPoint(final TwoDPoint origin, final TwoDPoint head1, final TwoDPoint head2, final double scale) {
        final TwoDVector v1 = new TwoDVector(origin, head1);
        final TwoDVector v2 = new TwoDVector(origin, head2);

        final TwoDVector sum = (v1.normalize()).add(v2.normalize()).normalize().multiplyScalar(scale);

        return new TwoDPoint(sum.getRe() + origin.getX(), sum.getIm() + origin.getY());
    }

    public static TwoDPoint get90RotatedPoint(final TwoDPoint origin, final TwoDPoint head) {
        return getRotatedPoint(origin, head, 90);
    }

    public static TwoDPoint get270RotatedPoint(final TwoDPoint origin, final TwoDPoint head) {
        return getRotatedPoint(origin, head, 270);
    }

    public static TwoDPoint getRotatedPoint(final TwoDPoint origin, final TwoDPoint head, final double degrees) {
        TwoDVector v = new TwoDVector(origin, head);
        v = v.rotate(degrees);

        return new TwoDPoint(v.getRe() + origin.getX(), v.getIm() + origin.getY());
    }

    public static TwoDPoint getCorrectProjection(final TwoDPoint origin, final TwoDPoint head1, final TwoDPoint head2, final TwoDPoint newOrigin) {
        final TwoDSegment oh1 = new TwoDSegment(origin, head1);
        final TwoDPoint p1 = oh1.projectionOfPointOnLine(newOrigin);
        final double d1 = newOrigin.distanceBetween(p1);

        final TwoDSegment oh2 = new TwoDSegment(origin, head2);
        final TwoDPoint p2 = oh2.projectionOfPointOnLine(newOrigin);
        final double d2 = newOrigin.distanceBetween(p2);

        return (d1 < d2) ? p1 : p2;
    }

    public static TwoDPoint projectToLineByVector(final TwoDPoint newOrigin, final TwoDSegment line, final TwoDVector vector) {
        final double a = vector.getRe();
        final double b = vector.getIm();
        final double p = line.getLineSlope();
        final double q = line.getLineIntercept();
        final double x0 = newOrigin.getX();
        final double y0 = newOrigin.getY();

        final double x = ((q - y0) / b + x0 / a) / (1 / a - p / b);
        final double y = p * x + q;

        return new TwoDPoint(x, y);
    }

    public static boolean isOnTheInside(final TwoDPoint beforeOriginPoint, final TwoDPoint originPoint, final TwoDPoint afterOriginPoint, final TwoDPoint newOriginPoint) {

        final double xA = beforeOriginPoint.getX();
        final double yA = beforeOriginPoint.getY();
        final double xB = originPoint.getX();
        final double yB = originPoint.getY();
        final double xC = afterOriginPoint.getX();
        final double yC = afterOriginPoint.getY();
        final double xD = newOriginPoint.getX();
        final double yD = newOriginPoint.getY();

        final boolean firstCondition = (yD - yA) >= ((yB - yA) * (xD - xA) / (xB - xA));
        final boolean secondCondition = (yD - yB) <= ((yC - yB) * (xD - xB) / (xC - xB));

        return firstCondition && secondCondition;

    }

    // A //B //C //Bfirst
    public static TwoDSegment getBeforeNew(final TwoDPoint beforePoint, final TwoDPoint origin, final TwoDPoint afterPoint,
            final TwoDPoint newOrigin) {
        final TwoDSegment AB = new TwoDSegment(beforePoint, origin);
        final double slopeAB = AB.getLineSlope();
        final double interceptAB = AB.getLineIntercept();

        final TwoDSegment BC = new TwoDSegment(origin, afterPoint);
        final double slopeBC = BC.getLineSlope();
        final double interceptBC = BC.getLineIntercept();

        final double temp1 = origin.x + newOrigin.x;
        final double temp2 = origin.y + newOrigin.y - interceptAB - interceptBC;

        final double beforeFx = (temp1 - temp2 / slopeBC) / (1 - slopeAB / slopeBC);
        final double beforeFy = slopeAB * beforeFx + interceptAB;

        final double afterFx = (temp1 - temp2 / slopeAB) / (1 - slopeBC / slopeAB);
        final double afterFy = slopeBC * afterFx + interceptBC;

        final TwoDPoint beforeF = new TwoDPoint(beforeFx, beforeFy);
        final TwoDPoint afterF = new TwoDPoint(afterFx, afterFy);

        return new TwoDSegment(beforeF, afterF);
    }

    public static boolean areInClockwiseOrder(final TwoDPoint A, final TwoDPoint B, final TwoDPoint C) {
        return (C.y - A.y) * (B.x - A.x) > (B.y - A.y) * (C.x - A.x);
    }

    public static boolean areIntersecting(final TwoDPoint A, final TwoDPoint B, final TwoDPoint C, final TwoDPoint D) {
        return (areInClockwiseOrder(A, C, D) != areInClockwiseOrder(B, C, D)) && (areInClockwiseOrder(A, B, C) != areInClockwiseOrder(A, B, D));
    }
}
