package com.sap.sailing.domain.maneuverdetection;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface GpsFixWithEstimationData extends GPSFixMoving {
    
    Wind getWind();

    Bearing getRelativeBearingToNextMarkAfterManeuver();

    Distance getDistanceToClosestMark();
}
