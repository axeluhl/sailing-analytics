package com.sap.sailing.windestimation.windinference;

import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Speed;

/**
 * TWS calculation strategy which returns zero TWS for all {@link #getWindSpeed(ManeuverForEstimation, Bearing)} calls.
 * This implementation is supposed to be used in cases when no TWS should be determined.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class DummyBasedTwsCalculatorImpl implements TwsFromManeuverCalculator {

    @Override
    public Speed getWindSpeed(ManeuverForEstimation maneuver, Bearing windCourse) {
        return Speed.NULL;
    }

}
