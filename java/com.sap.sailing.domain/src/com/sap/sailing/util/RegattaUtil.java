package com.sap.sailing.util;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.impl.MeterDistance;

public class RegattaUtil {

    public static final Distance DEFAULT_BUOY_ZONE_RADIUS = new MeterDistance(15);

    public static Distance getCalculatedRegattaBuoyZoneRadius(Regatta regatta, BoatClass boatClass) {
        final double hullLengthRadiusFactor;
        if (regatta != null && regatta.getHullLengthRadiusFactor() != null) {
            hullLengthRadiusFactor = regatta.getHullLengthRadiusFactor();
        } else {
            hullLengthRadiusFactor = Regatta.DEFAULT_BUOY_ZONE_RADIUS_IN_HULL_LENGTHS;
        }
        final Distance boatHullLength = boatClass == null ? null : boatClass.getHullLength();
        final Distance buyZoneRadius = boatHullLength == null ? DEFAULT_BUOY_ZONE_RADIUS
                : boatHullLength.scale(hullLengthRadiusFactor);
        return buyZoneRadius;
    }
}
