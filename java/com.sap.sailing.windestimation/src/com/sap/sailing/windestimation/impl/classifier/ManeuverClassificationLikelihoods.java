package com.sap.sailing.windestimation.impl.classifier;

public class ManeuverClassificationLikelihoods {

    private final double tackConfidence;
    private final double jibeConfidence;
    private final double markPassingLuvConfidence;
    private final double markPassingLeeConfidence;
    private final double headUpConfidence;
    private final double bearAwayConfidence;
    
    public ManeuverClassificationLikelihoods(double tackConfidence, double jibeConfidence,
            double markPassingLuvConfidence, double markPassingLeeConfidence, double headUpConfidence,
            double bearAwayConfidence) {
        this.tackConfidence = tackConfidence;
        this.jibeConfidence = jibeConfidence;
        this.markPassingLuvConfidence = markPassingLuvConfidence;
        this.markPassingLeeConfidence = markPassingLeeConfidence;
        this.headUpConfidence = headUpConfidence;
        this.bearAwayConfidence = bearAwayConfidence;
    }

    public double getTackConfidence() {
        return tackConfidence;
    }

    public double getJibeConfidence() {
        return jibeConfidence;
    }

    public double getMarkPassingLuvConfidence() {
        return markPassingLuvConfidence;
    }

    public double getMarkPassingLeeConfidence() {
        return markPassingLeeConfidence;
    }

    public double getHeadUpConfidence() {
        return headUpConfidence;
    }

    public double getBearAwayConfidence() {
        return bearAwayConfidence;
    }

}
