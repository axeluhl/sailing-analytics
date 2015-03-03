package com.sap.sailing.polars.mining;


public class AverageAngleContainer {
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
