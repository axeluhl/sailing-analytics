package com.sap.sailing.util;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.Distance;

public class RegattaUtil {

    public static final double DEFAULT_BUOY_ZONE_RADIUS = 15;

    public static double getCalculatedRegattaBuoyZoneRadius(Regatta regatta, BoatClass boatClass) {
        double hullLengthRadiusFactor = Regatta.DEFAULT_HULL_LENGHT_FACTOR;
        if (regatta != null) {
            hullLengthRadiusFactor = regatta.getHullLengthRadiusFactor();
        }

        Distance boatHullLength = boatClass == null ? null : boatClass.getHullLength();
        double buyZoneRadius = boatHullLength == null ? DEFAULT_BUOY_ZONE_RADIUS
                : boatHullLength.getMeters() * hullLengthRadiusFactor;
        return buyZoneRadius;
    }
}
