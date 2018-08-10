package com.sap.sailing.windestimation.data;

public interface ManeuverForClassification {

    ManeuverTypeForClassification getManeuverType();

    double getAbsoluteTotalCourseChangeInDegrees();
    
    double getSpeedInSpeedOutRatio();

    double getOversteeringInDegrees();

    double getSpeedLossRatio();

    double getSpeedGainRatioWithinMainCurve();
    
    double getLowestSpeedVsExitingSpeedRatio();

    double getMaximalTurningRateInDegreesPerSecond();

    Double getDeviationFromOptimalTackAngleInDegrees();

    Double getDeviationFromOptimalJibeAngleInDegrees();

    Double getHighestAbsoluteDeviationOfBoatsCourseToBearingFromBoatToNextWaypointInDegrees();

    double getManeuverDurationInSeconds();

    double getMainCurveDurationInSeconds();

    double getRecoveryPhaseDurationInSeconds();

    double getTimeLossInSeconds();

    boolean isClean();

    ManeuverCategory getManeuverCategory();

}
