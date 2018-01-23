package com.sap.sailing.windestimation.impl.maneuvergraph;

public class SingleManeuverClassificationResult {

    private final double lowestSpeedWithBeginningSpeedRatio;
    private final double highestSpeedWithBeginningSpeedRatio;
    private final double enteringExitingSpeedRatio;
    private final double courseChangeDeg;
    private final double[] presumedManeuverTypeLikelihoodsByAngleAnalysis;
    private final double[] presumedManeuverTypeLikelihoodsBySpeedAnalysis;
    private final double presumedTwsInKnotsIfTack;
    private final double presumedTwsInKnotsIfJibe;

    public SingleManeuverClassificationResult(double lowestSpeedWithBeginningSpeedRatio,
            double highestSpeedWithBeginningSpeedRatio, double enteringExitingSpeedRatio, double courseChangeDeg,
            double[] presumedManeuverTypeLikelihoodsByAngleAnalysis,
            double[] presumedManeuverTypeLikelihoodsBySpeedAnalysis,
            double presumedTwsInKnotsIfTack, double presumedTwsInKnotsIfJibe) {
        this.lowestSpeedWithBeginningSpeedRatio = lowestSpeedWithBeginningSpeedRatio;
        this.highestSpeedWithBeginningSpeedRatio = highestSpeedWithBeginningSpeedRatio;
        this.enteringExitingSpeedRatio = enteringExitingSpeedRatio;
        this.courseChangeDeg = courseChangeDeg;
        this.presumedManeuverTypeLikelihoodsByAngleAnalysis = presumedManeuverTypeLikelihoodsByAngleAnalysis;
        this.presumedManeuverTypeLikelihoodsBySpeedAnalysis = presumedManeuverTypeLikelihoodsBySpeedAnalysis;
        this.presumedTwsInKnotsIfTack = presumedTwsInKnotsIfTack;
        this.presumedTwsInKnotsIfJibe = presumedTwsInKnotsIfJibe;
    }

    public double getLowestSpeedWithBeginningSpeedRatio() {
        return lowestSpeedWithBeginningSpeedRatio;
    }

    public double getEnteringExitingSpeedRatio() {
        return enteringExitingSpeedRatio;
    }

    public double getCourseChangeDeg() {
        return courseChangeDeg;
    }

    public double getHighestSpeedWithBeginningSpeedRatio() {
        return highestSpeedWithBeginningSpeedRatio;
    }

    public double[] getPresumedManeuverTypeLikelihoodsByAngleAnalysis() {
        return presumedManeuverTypeLikelihoodsByAngleAnalysis;
    }

    public double[] getPresumedManeuverTypeLikelihoodsBySpeedAnalysis() {
        return presumedManeuverTypeLikelihoodsBySpeedAnalysis;
    }

    public double getPresumedTwsInKnotsIfTack() {
        return presumedTwsInKnotsIfTack;
    }

    public double getPresumedTwsInKnotsIfJibe() {
        return presumedTwsInKnotsIfJibe;
    }

    public double getLikelihoodForPointOfSailBeforeManeuver(PresumedPointOfSail pointOfSailBeforeManeuver, boolean markPassingIsNeighbour) {
        double likelihoodSums = 0;
        double sumandsCount = 0;
        //TODO generate likelihood for pointOfSailBefore and after
        for(PresumedManeuverType maneuverType : PresumedManeuverType.values()) {
            if(presumedManeuverTypeLikelihoodsByAngleAnalysis[maneuverType.ordinal()] != 0) {
                switch(maneuverType) {
                case TACK:
                    if(courseChangeDeg < 0 && pointOfSailBeforeManeuver == PresumedPointOfSail.UPWIND_STARBOARD) {
                        likelihoodSums += presumedManeuverTypeLikelihoodsByAngleAnalysis[maneuverType.ordinal()] + presumedManeuverTypeLikelihoodsBySpeedAnalysis[maneuverType.ordinal()];
                        
                    }
                }
            }
        }
        return 0;
    }

}
