package com.sap.sailing.windestimation.windinference;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.tracking.impl.WindWithConfidenceImpl;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverWithEstimatedType;
import com.sap.sailing.windestimation.util.WindUtil;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Speed;

public class WindTrackCalculatorImpl implements WindTrackCalculator {

    private final TwdFromManeuverCalculator twdCalculator;
    private final TwsFromManeuverCalculator twsCalculator;

    public WindTrackCalculatorImpl(TwdFromManeuverCalculator twdCalculator, TwsFromManeuverCalculator twsCalculator) {
        this.twdCalculator = twdCalculator;
        this.twsCalculator = twsCalculator;
    }

    @Override
    public List<WindWithConfidence<Void>> getWindTrackFromManeuverClassifications(
            List<ManeuverWithEstimatedType> improvedManeuverClassifications) {
        List<WindWithConfidence<Void>> windFixes = new ArrayList<>();
        for (ManeuverWithEstimatedType maneuverWithEstimatedType : improvedManeuverClassifications) {
            Bearing windCourse = twdCalculator.getTwd(maneuverWithEstimatedType);
            if (windCourse != null) {
                windCourse = windCourse.reverse();
                ManeuverForEstimation maneuver = maneuverWithEstimatedType.getManeuver();
                Speed avgWindSpeed = twsCalculator.getWindSpeed(maneuver, windCourse);
                Wind wind = new WindImpl(maneuver.getManeuverPosition(), maneuver.getManeuverTimePoint(),
                        new KnotSpeedWithBearingImpl(avgWindSpeed.getKnots(), windCourse));
                windFixes.add(new WindWithConfidenceImpl<Void>(wind, maneuverWithEstimatedType.getConfidence(), null,
                        avgWindSpeed.getKnots() > 0));
            }
        }
        windFixes = WindUtil.getWindFixesWithMedianTws(windFixes);
        return windFixes;
    }

}
