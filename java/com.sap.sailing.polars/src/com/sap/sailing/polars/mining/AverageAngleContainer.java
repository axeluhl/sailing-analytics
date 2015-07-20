package com.sap.sailing.polars.mining;

import java.io.Serializable;


public class AverageAngleContainer implements Serializable {
    private static final long serialVersionUID = -6370647955050312847L;
    private int dataCount = 0;
    private double angleSumDeg = 0.0;
    private double averageAngleDeg = 0.0;

    public void addFix(double roundedAngleDeg) {
        dataCount++;
        angleSumDeg += roundedAngleDeg;
        averageAngleDeg = angleSumDeg / (double) dataCount;
    }

    Double getAverageAngleDeg() {
        return averageAngleDeg;
    }

}
