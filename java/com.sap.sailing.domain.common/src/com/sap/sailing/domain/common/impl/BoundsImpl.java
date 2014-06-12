package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.Bounds;
import com.sap.sailing.domain.common.Position;

public class BoundsImpl implements Bounds {
    private final Position sw;
    private final Position ne;
    private final boolean crossesDateLine;
    
    public BoundsImpl(Position sw, Position ne) {
        super();
        this.sw = sw;
        this.ne = ne;
        crossesDateLine = ne.getLngDeg() < sw.getLngDeg();
    }
    
    @Override
    public Position getNorthWest() {
        return new DegreePosition(getNorthEast().getLatDeg(), getSouthWest().getLngDeg());
    }

    @Override
    public Position getSouthEast() {
        return new DegreePosition(getSouthWest().getLatDeg(), getNorthEast().getLngDeg());
    }

    @Override
    public Position getNorthEast() {
        return ne;
    }

    @Override
    public Position getSouthWest() {
        return sw;
    }

    /**
     * Considering whether or not this bounds object crosses the date line, calculates the width in degrees longitude
     * going from <code>west</code> to <code>east</code>. The result is always a non-negative number.
     */
    private double getDistanceDeg(final double west, final double east) {
        final double diff = east-west;
        final double result;
        if (isCrossingDateLine(west, east)) {
            result = 360+diff; // diff is negative in this case
        } else {
            result = diff;
        }
        return result;
    }
    
    @Override
    public Bounds intersect(Bounds other) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bounds union(Bounds other) {
        final double minLatDeg = Math.min(getSouthWest().getLatDeg(), other.getSouthWest().getLatDeg());
        final double maxLatDeg = Math.max(getNorthEast().getLatDeg(), other.getNorthEast().getLatDeg());
        // For lng, we can go left or right around the earth; which way results in the smaller bounds? We have four options:
        final double[] west = new double[] { getSouthWest().getLngDeg(), other.getSouthWest().getLngDeg() };
        final double[] east = new double[] { getNorthEast().getLngDeg(), other.getNorthEast().getLngDeg() };
        double minLngDegDistance = Double.MAX_VALUE;
        double bestWest = 0;
        double bestEast = 0;
        for (int w=0; w<2; w++) {
            for (int e=0; e<2; e++) {
                double currentLngDegDistance;
                if (spansLngDeg(west[w], east[e], west[1 - w]) && spansLngDeg(west[w], east[e], east[1 - e])
                        && (currentLngDegDistance = Math.abs(getDistanceDeg(west[w], east[e]))) < minLngDegDistance) {
                    minLngDegDistance = currentLngDegDistance;
                    bestWest = west[w];
                    bestEast = east[e];
                }
            }
        }
        return new BoundsImpl(new DegreePosition(minLatDeg, bestWest), new DegreePosition(maxLatDeg, bestEast));
    }

    private boolean spansLngDeg(double westLngDeg, double eastLngDeg, double lngDeg) {
        return isCrossingDateLine(westLngDeg, eastLngDeg)
                ? (lngDeg >= westLngDeg && lngDeg <= 180) || (lngDeg >= -180 && lngDeg <= eastLngDeg)
                : westLngDeg <= lngDeg && lngDeg <= eastLngDeg;
    }

    private boolean isCrossingDateLine(double westLngDeg, double eastLngDeg) {
        return westLngDeg > eastLngDeg;
    }

    @Override
    public boolean intersects(Bounds other) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean contains(Position other) {
        return
                    // lat contained:
                other.getLatDeg() >= getSouthWest().getLatDeg() && other.getLatDeg() <= getNorthEast().getLatDeg() &&
                    // lng contained:
                (isCrossesDateLine()
                    // between SW and date line...
                    ? (other.getLngDeg() <= 180 && other.getLngDeg() >= getSouthWest().getLngDeg()) ||
                    // ...or between date line and NE
                      (other.getLngDeg() >= -180 && other.getLngDeg() <= getNorthEast().getLngDeg())
                    // these bounds are not crossing the date line; simple numeric comparison
                    : other.getLngDeg() >= getSouthWest().getLngDeg() && other.getLngDeg() <= getNorthEast().getLngDeg())
                ;
    }

    @Override
    public boolean contains(Bounds other) {
        return isCrossesDateLine() == other.isCrossesDateLine() && contains(other.getNorthEast()) && contains(other.getSouthWest());
    }

    @Override
    public boolean isCrossesDateLine() {
        return crossesDateLine;
    }
}
