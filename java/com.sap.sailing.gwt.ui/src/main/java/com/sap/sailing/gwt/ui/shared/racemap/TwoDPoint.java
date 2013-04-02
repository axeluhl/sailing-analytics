package com.sap.sailing.gwt.ui.shared.racemap;

public class TwoDPoint {
    private double x;
    private double y;

    public TwoDPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double distanceBetween(TwoDPoint point) {
        return distanceBetween(this.x, this.y, point.getX(), point.getY());
    }

    public double distanceBetween(double x, double y) {
        return distanceBetween(this.x, this.y, x, y);
    }

    public static double distanceBetween(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
    }

    public static double distanceBetween(TwoDPoint point1, TwoDPoint point2) {
        return distanceBetween(point1.getX(), point1.getY(), point2.getX(), point2.getY());
    }

    @Override
    public String toString() {
        return "Point @(" + this.x + "," + this.y + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        TwoDPoint other = (TwoDPoint) obj;
        return this.x == other.getX() && this.y == other.getY();
    }

    @Override
    public int hashCode() {
        return 31 * (new Double(this.x).hashCode()) * (new Double(this.y).hashCode());
    }

    public TwoDPoint getDistancedPoint(double length, TwoDPoint startPoint) {
        TwoDVector vector = new TwoDVector(startPoint, this).normalize().multiplyScalar(length);

        double x = this.getX() + vector.getRe();
        double y = this.getY() + vector.getIm();

        return new TwoDPoint(x, y);
    }

    public static TwoDPoint getBisectingPoint(TwoDPoint origin, TwoDPoint head1, TwoDPoint head2, double scale) {
        TwoDVector v1 = new TwoDVector(origin, head1);
        TwoDVector v2 = new TwoDVector(origin, head2);

        TwoDVector sum = (v1.normalize()).add(v2.normalize()).normalize().multiplyScalar(scale);

        return new TwoDPoint(sum.getRe() + origin.getX(), sum.getIm() + origin.getY());
    }

    public static TwoDPoint get90RotatedPoint(TwoDPoint origin, TwoDPoint head) {
        return getRotatedPoint(origin, head, 90);
    }

    public static TwoDPoint get270RotatedPoint(TwoDPoint origin, TwoDPoint head) {
        return getRotatedPoint(origin, head, 270);
    }

    public static TwoDPoint getRotatedPoint(TwoDPoint origin, TwoDPoint head, double degrees) {
        TwoDVector v = new TwoDVector(origin, head);
        v = v.rotate(degrees);

        return new TwoDPoint(v.getRe() + origin.getX(), v.getIm() + origin.getY());
    }

    public static TwoDPoint projectToLineByVector(TwoDPoint newOrigin, TwoDSegment line, TwoDVector vector) {

        double a = vector.getRe();
        double b = vector.getIm();
        double p = line.getLineSlope();
        double q = line.getLineIntercept();
        double x0 = newOrigin.getX();
        double y0 = newOrigin.getY();

        double x = ((q - y0) / b + x0 / a) / (1 / a - p / b);
        double y = p * x + q;

        return new TwoDPoint(x, y);
    }

    public static boolean isOnTheInside(TwoDPoint beforeOriginPoint, TwoDPoint originPoint, TwoDPoint afterOriginPoint, TwoDPoint newOriginPoint) {

        double xA = beforeOriginPoint.getX();
        double yA = beforeOriginPoint.getY();
        double xB = originPoint.getX();
        double yB = originPoint.getY();
        double xC = afterOriginPoint.getX();
        double yC = afterOriginPoint.getY();
        double xD = newOriginPoint.getX();
        double yD = newOriginPoint.getY();

        boolean firstCondition = (yD - yA) >= ((yB - yA) * (xD - xA) / (xB - xA));
        boolean secondCondition = (yD - yB) <= ((yC - yB) * (xD - xB) / (xC - xB));

        return firstCondition && secondCondition;

    }

    // A //B //C //Bfirst
    public static TwoDSegment getBeforeNew(TwoDPoint beforePoint, TwoDPoint origin, TwoDPoint afterPoint, TwoDPoint newOrigin) {
        TwoDSegment AB = new TwoDSegment(beforePoint, origin);
        double slopeAB = AB.getLineSlope();
        double interceptAB = AB.getLineIntercept();

        TwoDSegment BC = new TwoDSegment(origin, afterPoint);
        double slopeBC = BC.getLineSlope();
        double interceptBC = BC.getLineIntercept();

        double temp1 = origin.x + newOrigin.x;
        double temp2 = origin.y + newOrigin.y - interceptAB - interceptBC;

        double beforeFx = (temp1 - temp2 / slopeBC) / (1 - slopeAB / slopeBC);
        double beforeFy = slopeAB * beforeFx + interceptAB;

        double afterFx = (temp1 - temp2 / slopeAB) / (1 - slopeBC / slopeAB);
        double afterFy = slopeBC * afterFx + interceptBC;

        TwoDPoint beforeF = new TwoDPoint(beforeFx, beforeFy);
        TwoDPoint afterF = new TwoDPoint(afterFx, afterFy);

        return new TwoDSegment(beforeF, afterF);
    }

    public static boolean areInClockwiseOrder(TwoDPoint A, TwoDPoint B, TwoDPoint C) {
        return (C.y - A.y) * (B.x - A.x) > (B.y - A.y) * (C.x - A.x);
    }

    public static boolean areIntersecting(TwoDPoint A, TwoDPoint B, TwoDPoint C, TwoDPoint D) {
        return (areInClockwiseOrder(A, C, D) != areInClockwiseOrder(B, C, D)) && (areInClockwiseOrder(A, B, C) != areInClockwiseOrder(A, B, D));
    }
}
