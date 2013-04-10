package com.sap.sailing.odf.resultimport.impl;

import com.sap.sailing.odf.resultimport.PointsResult;

/**
 * A point result is a net result, after applying discards. Two points results are euqal if their points are equal.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class PointsResultImpl implements PointsResult {
    private final double points;
    
    public PointsResultImpl(double points) {
        super();
        this.points = points;
    }

    @Override
    public double getPoints() {
        return points;
    }

    @Override
    public String toString() {
        return ""+getPoints()+" points";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(points);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PointsResultImpl other = (PointsResultImpl) obj;
        if (Double.doubleToLongBits(points) != Double.doubleToLongBits(other.points))
            return false;
        return true;
    }
}
