package com.sap.sailing.windestimation;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.tracking.impl.WindWithConfidenceImpl;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sse.common.Bearing;

public class WindTrackEstimatorAdapter implements WindTrackEstimator {

    private final AverageWindEstimator windEstimator;
    private List<ManeuverForEstimation> usefulManeuvers;

    public WindTrackEstimatorAdapter(AverageWindEstimator windEstimator, List<ManeuverForEstimation> usefulManeuvers) {
        this.windEstimator = windEstimator;
        this.usefulManeuvers = usefulManeuvers;
    }

    @Override
    public List<WindWithConfidence<Void>> estimateWindTrack() {
        List<WindWithConfidence<Void>> result = new ArrayList<>();
        WindWithConfidence<Void> wind = windEstimator.estimateAverageWind();
        if (wind != null) {
            for (ManeuverForEstimation maneuver : usefulManeuvers) {
                if (maneuver.isClean()) {
                    Bearing twaBefore = wind.getObject().getFrom()
                            .getDifferenceTo(maneuver.getSpeedWithBearingBefore().getBearing());
                    Bearing twaAfter = wind.getObject().getFrom()
                            .getDifferenceTo(maneuver.getSpeedWithBearingAfter().getBearing());
                    if (twaBefore.getDegrees() * twaAfter.getDegrees() < 0
                            && Math.abs(Math.abs(twaBefore.getDegrees()) - Math.abs(twaAfter.getDegrees())) <= 20) {
                        Wind newWind = new WindImpl(maneuver.getManeuverPosition(), maneuver.getManeuverTimePoint(),
                                wind.getObject());
                        result.add(new WindWithConfidenceImpl<Void>(newWind, wind.getConfidence(), null,
                                newWind.getKnots() > 0));
                    }
                }
            }
        }
        return result;
    }

}
