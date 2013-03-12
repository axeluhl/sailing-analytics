package com.sap.sailing.odf.resultimport.impl;

import com.sap.sailing.odf.resultimport.PointsResult;

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
}
