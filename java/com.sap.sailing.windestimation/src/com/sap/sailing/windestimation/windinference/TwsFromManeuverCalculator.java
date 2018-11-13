package com.sap.sailing.windestimation.windinference;

import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Speed;

public interface TwsFromManeuverCalculator {

    Speed getWindSpeed(ManeuverForEstimation maneuver, Bearing windCourse);

}
