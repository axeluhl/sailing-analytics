package com.sap.sailing.windestimation.data;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverTypeForClassification;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.TimePoint;

public class LabelledManeuverForEstimation extends ManeuverForEstimation {

    private final ManeuverTypeForClassification maneuverType;
    private final Wind wind;

    public LabelledManeuverForEstimation(TimePoint maneuverTimePoint, Position maneuverPosition, Bearing middleCourse,
            SpeedWithBearing speedWithBearingBefore, SpeedWithBearing speedWithBearingAfter,
            Bearing courseAtLowestSpeed, SpeedWithBearing averageSpeedWithBearingBefore,
            SpeedWithBearing averageSpeedWithBearingAfter, double courseChangeInDegrees,
            double courseChangeWithinMainCurveInDegrees, double maxTurningRateInDegreesPerSecond,
            Double deviationFromOptimalTackAngleInDegrees, Double deviationFromOptimalJibeAngleInDegrees,
            double speedLossRatio, double speedGainRatio, double lowestSpeedVsExitingSpeedRatio, boolean clean,
            boolean cleanBefore, boolean cleanAfter, ManeuverCategory maneuverCategory, double scaledSpeedBefore,
            double scaledSpeedAfter, BoatClass boatClass, boolean markPassing, Double relativeBearingToNextMarkBefore,
            Double relativeBearingToNextMarkAfter, ManeuverTypeForClassification maneuverType, Wind wind) {
        super(maneuverTimePoint, maneuverPosition, middleCourse, speedWithBearingBefore, speedWithBearingAfter,
                courseAtLowestSpeed, averageSpeedWithBearingBefore, averageSpeedWithBearingAfter, courseChangeInDegrees,
                courseChangeWithinMainCurveInDegrees, maxTurningRateInDegreesPerSecond,
                deviationFromOptimalTackAngleInDegrees, deviationFromOptimalJibeAngleInDegrees, speedLossRatio,
                speedGainRatio, lowestSpeedVsExitingSpeedRatio, clean, cleanBefore, cleanAfter, maneuverCategory,
                scaledSpeedBefore, scaledSpeedAfter, boatClass, markPassing, relativeBearingToNextMarkBefore,
                relativeBearingToNextMarkAfter);
        this.maneuverType = maneuverType;
        this.wind = wind;
    }

    public ManeuverTypeForClassification getManeuverType() {
        return maneuverType;
    }

    public Wind getWind() {
        return wind;
    }

}
