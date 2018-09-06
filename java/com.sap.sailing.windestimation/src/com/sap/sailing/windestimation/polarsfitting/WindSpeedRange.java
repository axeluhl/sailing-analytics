package com.sap.sailing.windestimation.polarsfitting;

public class WindSpeedRange {

    private final double upperSpeed;
    private final double lowerSpeed;
    private final double confidence;

    public WindSpeedRange(double upperSpeed, double lowerSpeed) {
        this(upperSpeed, lowerSpeed, 1.0);
    }

    private WindSpeedRange(double upperSpeed, double lowerSpeed, double confidence) {
        this.upperSpeed = upperSpeed;
        this.lowerSpeed = lowerSpeed;
        this.confidence = confidence;
    }

    public double getUpperSpeed() {
        return upperSpeed;
    }

    public double getLowerSpeed() {
        return lowerSpeed;
    }

    public double getMiddleSpeed() {
        return (upperSpeed + lowerSpeed) / 2.0;
    }

    public double getSpeedDifference() {
        return upperSpeed - lowerSpeed;
    }

    public double getConfidence() {
        return confidence;
    }

    public double getDeviationOfSpeedFromRange(double speed) {
        double lowerSpeedDeviation = lowerSpeed - speed;
        double upperSpeedDeviation = speed - upperSpeed;
        if (lowerSpeedDeviation > 0 || upperSpeedDeviation > 0) {
            return Math.max(lowerSpeedDeviation, upperSpeedDeviation);
        }
        return 0;
    }

    public WindSpeedRange intersect(WindSpeedRange other) {
        double newUpperSpeed = Math.min(upperSpeed, other.upperSpeed);
        double newLowerSpeed = Math.max(lowerSpeed, other.lowerSpeed);
        double newConfidence = (confidence + other.confidence) / 2;
        if (newUpperSpeed < newLowerSpeed) {
            newConfidence *= 1 / (1 + (newLowerSpeed - newUpperSpeed) / 5);
            double temp = newLowerSpeed;
            newLowerSpeed = newUpperSpeed;
            newUpperSpeed = temp;
        }
        return new WindSpeedRange(newUpperSpeed, newLowerSpeed, newConfidence);
    }

    public WindSpeedRange extend(WindSpeedRange other) {
        double newUpperSpeed = Math.max(upperSpeed, other.upperSpeed);
        double newLowerSpeed = Math.min(lowerSpeed, other.lowerSpeed);
        double newConfidence = (confidence + other.confidence) / 2;
        return new WindSpeedRange(newUpperSpeed, newLowerSpeed, newConfidence);
    }

}
