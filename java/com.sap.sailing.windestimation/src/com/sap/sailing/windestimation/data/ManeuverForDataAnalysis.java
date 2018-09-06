package com.sap.sailing.windestimation.data;

public class ManeuverForDataAnalysis {

    private final ManeuverTypeForDataAnalysis maneuverType;
    private final double absoluteTotalCourseChangeInDegrees;
    private final double speedInSpeedOutRatio;
    private final double oversteeringInDegrees;
    private final double speedLossRatio;
    private final double speedGainRatio;
    private final double maximalTurningRateInDegreesPerSecond;
    private final Double deviationFromOptimalTackAngleInDegrees;
    private final Double deviationFromOptimalJibeAngleInDegrees;
    private final Double relativeBearingToNextMarkBefore;
    private final Double relativeBearingToNextMarkAfter;
    private final double mainCurveDurationInSeconds;
    private final double maneuverDurationInSeconds;
    private final double recoveryPhaseDurationInSeconds;
    private final double timeLossInSeconds;
    private final boolean clean;
    private final ManeuverCategory maneuverCategory;
    private final double lowestSpeedVsExitingSpeedRatio;
    private final double twaBeforeInDegrees;
    private final double twaAfterInDegrees;
    private final double twsInKnots;
    private final double speedBeforeInKnots;
    private final double speedAfterInKnots;
    private final double twaAtMiddleCourseInDegrees;
    private final boolean starboardManeuver;
    private final double twaAtLowestSpeedInDegrees;
    private final double twaAtMaxTurningRateInDegrees;
    private final double absoluteTotalCourseChangeWithinMainCurveInDegrees;
    private final double twaAtMiddleCourseMainCurveInDegrees;
    private final double scaledSpeedBeforeInKnots;
    private final double scaledSpeedAfterInKnots;

    public ManeuverForDataAnalysis(ManeuverTypeForDataAnalysis maneuverType, double absoluteTotalCourseChangeInDegrees,
            double absoluteTotalCourseChangeWithinMainCurveInDegrees, double speedInSpeedOutRatio,
            double oversteeringInDegrees, double speedLossRatio, double speedGainRatio,
            double lowestSpeedVsExitingSpeedRatio, double maximalTurningRateInDegreesPerSecond,
            Double deviationFromOptimalTackAngleInDegrees, Double deviationFromOptimalJibeAngleInDegrees,
            Double relativeBearingToNextMarkBefore, Double relativeBearingToNextMarkAfter,
            double mainCurveDurationInSeconds, double maneuverDurationInSeconds, double recoveryPhaseDurationInSeconds,
            double timeLossInSeconds, boolean clean, ManeuverCategory maneuverCategory, double twaBeforeInDegrees,
            double twaAfterInDegrees, double twsInKnots, double speedBeforeInKnots, double speedAfterInKnots,
            double twaAtMiddleCourseInDegrees, double twaAtMiddleCourseMainCurveInDegrees,
            double twaAtLowestSpeedInDegrees, double twaAtMaxTurningRateInDegrees, boolean starboardManeuver,
            double scaledSpeedBeforeInKnots, double scaledSpeedAfterInKnots) {
        this.maneuverType = maneuverType;
        this.absoluteTotalCourseChangeInDegrees = absoluteTotalCourseChangeInDegrees;
        this.absoluteTotalCourseChangeWithinMainCurveInDegrees = absoluteTotalCourseChangeWithinMainCurveInDegrees;
        this.speedInSpeedOutRatio = speedInSpeedOutRatio;
        this.oversteeringInDegrees = oversteeringInDegrees;
        this.speedLossRatio = speedLossRatio;
        this.speedGainRatio = speedGainRatio;
        this.lowestSpeedVsExitingSpeedRatio = lowestSpeedVsExitingSpeedRatio;
        this.maximalTurningRateInDegreesPerSecond = maximalTurningRateInDegreesPerSecond;
        this.deviationFromOptimalTackAngleInDegrees = deviationFromOptimalTackAngleInDegrees;
        this.deviationFromOptimalJibeAngleInDegrees = deviationFromOptimalJibeAngleInDegrees;
        this.relativeBearingToNextMarkBefore = relativeBearingToNextMarkBefore;
        this.relativeBearingToNextMarkAfter = relativeBearingToNextMarkAfter;
        this.mainCurveDurationInSeconds = mainCurveDurationInSeconds;
        this.maneuverDurationInSeconds = maneuverDurationInSeconds;
        this.recoveryPhaseDurationInSeconds = recoveryPhaseDurationInSeconds;
        this.timeLossInSeconds = timeLossInSeconds;
        this.clean = clean;
        this.maneuverCategory = maneuverCategory;
        this.twaBeforeInDegrees = twaBeforeInDegrees;
        this.twaAfterInDegrees = twaAfterInDegrees;
        this.twsInKnots = twsInKnots;
        this.speedBeforeInKnots = speedBeforeInKnots;
        this.speedAfterInKnots = speedAfterInKnots;
        this.twaAtMiddleCourseInDegrees = twaAtMiddleCourseInDegrees;
        this.twaAtMiddleCourseMainCurveInDegrees = twaAtMiddleCourseMainCurveInDegrees;
        this.twaAtLowestSpeedInDegrees = twaAtLowestSpeedInDegrees;
        this.twaAtMaxTurningRateInDegrees = twaAtMaxTurningRateInDegrees;
        this.starboardManeuver = starboardManeuver;
        this.scaledSpeedBeforeInKnots = scaledSpeedBeforeInKnots;
        this.scaledSpeedAfterInKnots = scaledSpeedAfterInKnots;
    }

    public double getAbsoluteTotalCourseChangeInDegrees() {
        return absoluteTotalCourseChangeInDegrees;
    }

    public double getAbsoluteTotalCourseChangeWithinMainCurveInDegrees() {
        return absoluteTotalCourseChangeWithinMainCurveInDegrees;
    }

    public double getSpeedInSpeedOutRatio() {
        return speedInSpeedOutRatio;
    }

    public double getOversteeringInDegrees() {
        return oversteeringInDegrees;
    }

    public double getSpeedLossRatio() {
        return speedLossRatio;
    }

    public double getSpeedGainRatioWithinMainCurve() {
        return speedGainRatio;
    }

    public double getLowestSpeedVsExitingSpeedRatio() {
        return lowestSpeedVsExitingSpeedRatio;
    }

    public double getMaximalTurningRateInDegreesPerSecond() {
        return maximalTurningRateInDegreesPerSecond;
    }

    public Double getDeviationFromOptimalTackAngleInDegrees() {
        return deviationFromOptimalTackAngleInDegrees;
    }

    public Double getDeviationFromOptimalJibeAngleInDegrees() {
        return deviationFromOptimalJibeAngleInDegrees;
    }

    public double getMainCurveDurationInSeconds() {
        return mainCurveDurationInSeconds;
    }

    public double getManeuverDurationInSeconds() {
        return maneuverDurationInSeconds;
    }

    public double getRecoveryPhaseDurationInSeconds() {
        return recoveryPhaseDurationInSeconds;
    }

    public double getTimeLossInSeconds() {
        return timeLossInSeconds;
    }

    public ManeuverTypeForDataAnalysis getManeuverType() {
        return maneuverType;
    }

    public Double getRelativeBearingToNextMarkBefore() {
        return relativeBearingToNextMarkBefore;
    }

    public Double getRelativeBearingToNextMarkAfter() {
        return relativeBearingToNextMarkAfter;
    }

    public boolean isClean() {
        return clean;
    }

    public ManeuverCategory getManeuverCategory() {
        return maneuverCategory;
    }

    public double getTwaBeforeInDegrees() {
        return twaBeforeInDegrees;
    }

    public double getTwaAfterInDegrees() {
        return twaAfterInDegrees;
    }

    public double getTwsInKnots() {
        return twsInKnots;
    }

    public double getSpeedBeforeInKnots() {
        return speedBeforeInKnots;
    }

    public double getSpeedAfterInKnots() {
        return speedAfterInKnots;
    }

    public double getTwaAtMiddleCourseInDegrees() {
        return twaAtMiddleCourseInDegrees;
    }

    public double getTwaAtMiddleCourseMainCurveInDegrees() {
        return twaAtMiddleCourseMainCurveInDegrees;
    }

    public double getTwaAtLowestSpeedInDegrees() {
        return twaAtLowestSpeedInDegrees;
    }

    public double getTwaAtMaxTurningRateInDegrees() {
        return twaAtMaxTurningRateInDegrees;
    }

    public boolean isStarboardManeuver() {
        return starboardManeuver;
    }

    public double getScaledSpeedBeforeInKnots() {
        return scaledSpeedBeforeInKnots;
    }

    public double getScaledSpeedAfterInKnots() {
        return scaledSpeedAfterInKnots;
    }

}
