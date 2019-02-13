package com.sap.sailing.windestimation.aggregator.polarsfitting;

import java.io.Serializable;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class SpeedStatistics implements Serializable {

    private static final long serialVersionUID = 3722822093000803195L;
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
