package com.sap.sailing.domain.common.tracking.impl;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;

/**
 * A utility class that "compact" fix implementations such as {@link CompactGPSFixImpl} etc. can use
 * to obtain representations of latitudes, longitudes, knot speeds and degree bearings marshalled in
 * types that sacrifice very little accuracy from a real-world perspective, yet use a data type for
 * encoding that requires fewer bytes than a {@code double} value.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class CompactPositionHelper {
    private static final double LAT_SCALE = 90. / (double) (1 << 31);  // int
    private static final double LNG_SCALE = 180. / (double) (1 << 31); // int
    
    private static final double KNOT_SPEED_SCALE = 500. /* knots; what that we track would ever be faster? */ / (double) (1<<16); // short
    private static final double DEGREE_BEARING_SCALE = 360. / (double) (1<<16); // short

    // Positions
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
    
    // Speeds / Bearings
    public static double getKnotSpeed(short knotSpeedScaled) {
        return KNOT_SPEED_SCALE * knotSpeedScaled;
    }

    public static double getDegreeBearing(short degreeBearingScaled) {
        return DEGREE_BEARING_SCALE * degreeBearingScaled;
    }

    public static short getKnotSpeedScaled(Speed speed) {
        final double knotSpeedScaled = speed.getKnots() / KNOT_SPEED_SCALE;
        if (knotSpeedScaled > Short.MAX_VALUE || knotSpeedScaled < Short.MIN_VALUE) {
            throw new IllegalArgumentException("Speed "+speed+" cannot be compacted; "+speed.getKnots()+" does not fit into a signed short value");
        }
        return (short) knotSpeedScaled;
    }

    public static short getDegreeBearingScaled(Bearing bearing) {
        return (short) (bearing.getDegrees() / DEGREE_BEARING_SCALE);
    }
}
