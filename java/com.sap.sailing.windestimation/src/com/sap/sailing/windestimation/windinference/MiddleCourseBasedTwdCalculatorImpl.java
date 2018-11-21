package com.sap.sailing.windestimation.windinference;

import com.sap.sailing.windestimation.classifier.maneuver.ManeuverWithEstimatedType;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sse.common.Bearing;

public class MiddleCourseBasedTwdCalculatorImpl implements TwdFromManeuverCalculator {

    @Override
    public Bearing getTwd(ManeuverWithEstimatedType maneuverWithEstimatedType) {
        Bearing twd = null;
        if (maneuverWithEstimatedType.getManeuverType() == ManeuverTypeForClassification.TACK
                || maneuverWithEstimatedType.getManeuverType() == ManeuverTypeForClassification.JIBE) {
            ManeuverForEstimation maneuver = maneuverWithEstimatedType.getManeuver();
            twd = maneuver.getMiddleCourse();
            if (maneuverWithEstimatedType.getManeuverType() == ManeuverTypeForClassification.JIBE) {
                twd = twd.reverse();
            }
        }
        return twd;
    }

}
