package com.sap.sailing.windestimation.data;

public class ManeuverForClassificationImpl implements ManeuverForClassification {

    private final ManeuverTypeForClassification maneuverType;
    private final double absoluteTotalCourseChangeInDegrees;
    private final double oversteeringInDegrees;
    private final double speedLossRatio;
    private final double speedGainRatio;
    private final double maximalTurningRateInDegreesPerSecond;
    private final Double deviationFromOptimalTackAngleInDegrees;
    private final Double deviationFromOptimalJibeAngleInDegrees;
    private final Double highestAbsoluteDeviationOfBoatsCourseToBearingFromBoatToNextWaypointInDegrees;
    private final double mainCurveDurationInSeconds;
    private final double maneuverDurationInSeconds;
    private final double recoveryPhaseDurationInSeconds;
    private final double timeLossInSeconds;

    public ManeuverForClassificationImpl(ManeuverTypeForClassification maneuverType,
            double absoluteTotalCourseChangeInDegrees, double oversteeringInDegrees, double speedLossRatio,
            double speedGainRatio, double maximalTurningRateInDegreesPerSecond,
            Double deviationFromOptimalTackAngleInDegrees, Double deviationFromOptimalJibeAngleInDegrees,
            Double highestAbsoluteDeviationOfBoatsCourseToBearingFromBoatToNextWaypointInDegrees,
            double mainCurveDurationInSeconds, double maneuverDurationInSeconds, double recoveryPhaseDurationInSeconds,
            double timeLossInSeconds) {
        this.maneuverType = maneuverType;
        this.absoluteTotalCourseChangeInDegrees = absoluteTotalCourseChangeInDegrees;
        this.oversteeringInDegrees = oversteeringInDegrees;
        this.speedLossRatio = speedLossRatio;
        this.speedGainRatio = speedGainRatio;
        this.maximalTurningRateInDegreesPerSecond = maximalTurningRateInDegreesPerSecond;
        this.deviationFromOptimalTackAngleInDegrees = deviationFromOptimalTackAngleInDegrees;
        this.deviationFromOptimalJibeAngleInDegrees = deviationFromOptimalJibeAngleInDegrees;
        this.highestAbsoluteDeviationOfBoatsCourseToBearingFromBoatToNextWaypointInDegrees = highestAbsoluteDeviationOfBoatsCourseToBearingFromBoatToNextWaypointInDegrees;
        this.mainCurveDurationInSeconds = mainCurveDurationInSeconds;
        this.maneuverDurationInSeconds = maneuverDurationInSeconds;
        this.recoveryPhaseDurationInSeconds = recoveryPhaseDurationInSeconds;
        this.timeLossInSeconds = timeLossInSeconds;
    }

    @Override
    public double getAbsoluteTotalCourseChangeInDegrees() {
        return absoluteTotalCourseChangeInDegrees;
    }

    @Override
    public double getOversteeringInDegrees() {
        return oversteeringInDegrees;
    }

    @Override
    public double getSpeedLossRatio() {
        return speedLossRatio;
    }

    @Override
    public double getSpeedGainRatio() {
        return speedGainRatio;
    }

    @Override
    public double getMaximalTurningRateInDegreesPerSecond() {
        return maximalTurningRateInDegreesPerSecond;
    }

    @Override
    public Double getDeviationFromOptimalTackAngleInDegrees() {
        return deviationFromOptimalTackAngleInDegrees;
    }

    @Override
    public Double getDeviationFromOptimalJibeAngleInDegrees() {
        return deviationFromOptimalJibeAngleInDegrees;
    }

    @Override
    public double getMainCurveDurationInSeconds() {
        return mainCurveDurationInSeconds;
    }

    @Override
    public double getManeuverDurationInSeconds() {
        return maneuverDurationInSeconds;
    }

    @Override
    public double getRecoveryPhaseDurationInSeconds() {
        return recoveryPhaseDurationInSeconds;
    }

    @Override
    public double getTimeLossInSeconds() {
        return timeLossInSeconds;
    }

    @Override
    public ManeuverTypeForClassification getManeuverType() {
        return maneuverType;
    }

    @Override
    public Double getHighestAbsoluteDeviationOfBoatsCourseToBearingFromBoatToNextWaypointInDegrees() {
        return highestAbsoluteDeviationOfBoatsCourseToBearingFromBoatToNextWaypointInDegrees;
    }

}
