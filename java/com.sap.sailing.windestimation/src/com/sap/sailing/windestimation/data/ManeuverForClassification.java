package com.sap.sailing.windestimation.data;

public interface ManeuverForClassification {
    
    ManeuverTypeForClassification getManeuverType();
    double getAbsoluteTotalCourseChangeInDegrees();
    double getOversteeringInDegrees();
    double getSpeedLossRatio();
    double getSpeedGainRatio();
    double getMaximalTurningRateInDegreesPerSecond();
    double getDeviationFromOptimalTackAngleInDegrees();
    double getDeviationFromOptimalJibeAngleInDegrees();
    double getHighestAbsoluteDeviationOfBoatsCourseToBearingFromBoatToNextWaypointInDegrees();
    double getManeuverDurationInSeconds();
    double getMainCurveDurationInSeconds();
    double getRecoveryPhaseDurationInSeconds();
    double getTimeLossInSeconds();

}
