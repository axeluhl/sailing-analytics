package com.sap.sailing.domain.common.tracking.impl;

import com.sap.sailing.domain.common.Position;

public class CompactPositionHelper {
    private static final double LAT_SCALE = 90. / (double) (1 << 31);
    private static final double LNG_SCALE = 180. / (double) (1 << 31);

    public static double getLatDeg(int latDegScaled) {
        return LAT_SCALE * latDegScaled;
    }

    public static double getLngDeg(int lngDegScaled) {
        return LNG_SCALE * lngDegScaled;
    }

    public static int getLngDegScaled(Position position) {
        return (int) (position.getLngDeg() / LNG_SCALE);
    }

    public static int getLatDegScaled(Position position) {
        return (int) (position.getLatDeg() / LAT_SCALE);
    }
}
