package com.sap.sailing.windestimation.aggregator.polarsfitting;

public class SpeedStatistics {

    private double sumOfSpeeds = 0;
    private int speedsCount = 0;

    public void addSpeed(double speed) {
        sumOfSpeeds += speed;
        speedsCount++;
    }

    public double getAvgSpeed() {
        return sumOfSpeeds / speedsCount;
    }
    
    public int getSpeedsCount() {
        return speedsCount;
    }

}
