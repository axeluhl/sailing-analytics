package com.sap.sailing.windestimation.data;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.TimePoint;

public class ManeuverForEstimation {

    private final TimePoint maneuverTimePoint;
    private final Position maneuverPosition;
    private final Bearing middleCourse;
    private final SpeedWithBearing speedWithBearingBefore;
    private final SpeedWithBearing speedWithBearingAfter;
    private final Bearing courseAtLowestSpeed;
    private final SpeedWithBearing averageSpeedWithBearingBefore;
    private final SpeedWithBearing averageSpeedWithBearingAfter;
    private final double courseChangeInDegrees;
    private final double courseChangeWithinMainCurveInDegrees;
    private final double maxTurningRateInDegreesPerSecond;
    private final Double deviationFromOptimalTackAngleInDegrees;
    private final Double deviationFromOptimalJibeAngleInDegrees;
    private final double speedLossRatio;
    private final double speedGainRatio;
    private final double lowestSpeedVsExitingSpeedRatio;
    private final boolean clean;
    private final boolean cleanBefore;
    private final boolean cleanAfter;
    private final ManeuverCategory maneuverCategory;
    private final double scaledSpeedBefore;
    private final double scaledSpeedAfter;
    private final BoatClass boatClass;
    private final boolean markPassing;
    private final Double relativeBearingToNextMarkBefore;
    private final Double relativeBearingToNextMarkAfter;
    private String regattaName;

    public ManeuverForEstimation(TimePoint maneuverTimePoint, Position maneuverPosition, Bearing middleCourse,
            SpeedWithBearing speedWithBearingBefore, SpeedWithBearing speedWithBearingAfter,
            Bearing courseAtLowestSpeed, SpeedWithBearing averageSpeedWithBearingBefore,
            SpeedWithBearing averageSpeedWithBearingAfter, double courseChangeInDegrees,
            double courseChangeWithinMainCurveInDegrees, double maxTurningRateInDegreesPerSecond,
            Double deviationFromOptimalTackAngleInDegrees, Double deviationFromOptimalJibeAngleInDegrees,
            double speedLossRatio, double speedGainRatio, double lowestSpeedVsExitingSpeedRatio, boolean clean,
            boolean cleanBefore, boolean cleanAfter, ManeuverCategory maneuverCategory, double scaledSpeedBefore,
            double scaledSpeedAfter, BoatClass boatClass, boolean markPassing, Double relativeBearingToNextMarkBefore,
            Double relativeBearingToNextMarkAfter, String regattaName) {
        this.maneuverTimePoint = maneuverTimePoint;
        this.maneuverPosition = maneuverPosition;
        this.middleCourse = middleCourse;
        this.speedWithBearingBefore = speedWithBearingBefore;
        this.speedWithBearingAfter = speedWithBearingAfter;
        this.courseAtLowestSpeed = courseAtLowestSpeed;
        this.averageSpeedWithBearingBefore = averageSpeedWithBearingBefore;
        this.averageSpeedWithBearingAfter = averageSpeedWithBearingAfter;
        this.courseChangeInDegrees = courseChangeInDegrees;
        this.courseChangeWithinMainCurveInDegrees = courseChangeWithinMainCurveInDegrees;
        this.maxTurningRateInDegreesPerSecond = maxTurningRateInDegreesPerSecond;
        this.deviationFromOptimalTackAngleInDegrees = deviationFromOptimalTackAngleInDegrees;
        this.deviationFromOptimalJibeAngleInDegrees = deviationFromOptimalJibeAngleInDegrees;
        this.speedLossRatio = speedLossRatio;
        this.speedGainRatio = speedGainRatio;
        this.lowestSpeedVsExitingSpeedRatio = lowestSpeedVsExitingSpeedRatio;
        this.clean = clean;
        this.cleanBefore = cleanBefore;
        this.cleanAfter = cleanAfter;
        this.maneuverCategory = maneuverCategory;
        this.scaledSpeedBefore = scaledSpeedBefore;
        this.scaledSpeedAfter = scaledSpeedAfter;
        this.boatClass = boatClass;
        this.markPassing = markPassing;
        this.relativeBearingToNextMarkBefore = relativeBearingToNextMarkBefore;
        this.relativeBearingToNextMarkAfter = relativeBearingToNextMarkAfter;
        this.regattaName = regattaName;
    }

    public TimePoint getManeuverTimePoint() {
        return maneuverTimePoint;
    }

    public Position getManeuverPosition() {
        return maneuverPosition;
    }

    public Bearing getMiddleCourse() {
        return middleCourse;
    }

    public SpeedWithBearing getSpeedWithBearingBefore() {
        return speedWithBearingBefore;
    }

    public SpeedWithBearing getSpeedWithBearingAfter() {
        return speedWithBearingAfter;
    }

    public Bearing getCourseAtLowestSpeed() {
        return courseAtLowestSpeed;
    }

    public SpeedWithBearing getAverageSpeedWithBearingBefore() {
        return averageSpeedWithBearingBefore;
    }

    public SpeedWithBearing getAverageSpeedWithBearingAfter() {
        return averageSpeedWithBearingAfter;
    }

    public double getCourseChangeInDegrees() {
        return courseChangeInDegrees;
    }

    public double getCourseChangeWithinMainCurveInDegrees() {
        return courseChangeWithinMainCurveInDegrees;
    }

    public double getMaxTurningRateInDegreesPerSecond() {
        return maxTurningRateInDegreesPerSecond;
    }

    public Double getDeviationFromOptimalTackAngleInDegrees() {
        return deviationFromOptimalTackAngleInDegrees;
    }

    public Double getDeviationFromOptimalJibeAngleInDegrees() {
        return deviationFromOptimalJibeAngleInDegrees;
    }

    public double getSpeedLossRatio() {
        return speedLossRatio;
    }

    public double getSpeedGainRatio() {
        return speedGainRatio;
    }

    public double getLowestSpeedVsExitingSpeedRatio() {
        return lowestSpeedVsExitingSpeedRatio;
    }

    public boolean isClean() {
        return clean;
    }

    public boolean isCleanBefore() {
        return cleanBefore;
    }

    public boolean isCleanAfter() {
        return cleanAfter;
    }

    public ManeuverCategory getManeuverCategory() {
        return maneuverCategory;
    }

    public double getScaledSpeedBefore() {
        return scaledSpeedBefore;
    }

    public double getScaledSpeedAfter() {
        return scaledSpeedAfter;
    }

    public BoatClass getBoatClass() {
        return boatClass;
    }

    public Double getRelativeBearingToNextMarkBefore() {
        return relativeBearingToNextMarkBefore;
    }

    public Double getRelativeBearingToNextMarkAfter() {
        return relativeBearingToNextMarkAfter;
    }

    public boolean isMarkPassing() {
        return markPassing;
    }

    public String getRegattaName() {
        return regattaName;
    }

}
