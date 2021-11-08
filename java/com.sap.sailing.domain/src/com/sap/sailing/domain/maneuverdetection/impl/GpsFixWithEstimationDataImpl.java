package com.sap.sailing.domain.maneuverdetection.impl;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.maneuverdetection.GpsFixWithEstimationData;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.TimePoint;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class GpsFixWithEstimationDataImpl extends GPSFixMovingImpl implements GpsFixWithEstimationData {

    private static final long serialVersionUID = -6952863819352430365L;

    private Wind wind;
    private Bearing relativeBearingToNextMarkAfterManeuver;
    private Distance distanceToClosestMark;

    public GpsFixWithEstimationDataImpl(Position position, TimePoint timePoint, SpeedWithBearing speedWithBearing,
            Wind wind, Bearing relativeBearingToNextMarkAfterManeuver, Distance distanceToClosestMark) {
        super(position, timePoint, speedWithBearing);
        this.wind = wind;
        this.relativeBearingToNextMarkAfterManeuver = relativeBearingToNextMarkAfterManeuver;
        this.distanceToClosestMark = distanceToClosestMark;
    }

    @Override
    public Wind getWind() {
        return wind;
    }

    @Override
    public Bearing getRelativeBearingToNextMarkAfterManeuver() {
        return relativeBearingToNextMarkAfterManeuver;
    }

    @Override
    public Distance getDistanceToClosestMark() {
        return distanceToClosestMark;
    }

}
