package com.sap.sailing.windestimation.windinference;

import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Speed;

public class DummyBasedTwsCalculatorImpl implements TwsFromManeuverCalculator {

    @Override
    public Speed getWindSpeed(ManeuverForEstimation maneuver, Bearing windCourse) {
        return new KnotSpeedImpl(0.0);
    }

}
