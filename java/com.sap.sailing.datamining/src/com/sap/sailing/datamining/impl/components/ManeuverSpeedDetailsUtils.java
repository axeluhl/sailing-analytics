package com.sap.sailing.datamining.impl.components;

import java.util.function.Function;

import com.sap.sailing.domain.common.NauticalSide;

public class ManeuverSpeedDetailsUtils {

    public static NauticalSide determineNauticalSideByClosestAngleDistance(double firstBearingAngle,
            double nextBearingAngle) {
        if (firstBearingAngle < nextBearingAngle) {
            return nextBearingAngle - firstBearingAngle <= 180 ? NauticalSide.STARBOARD : NauticalSide.PORT;
        } else {
            return firstBearingAngle - nextBearingAngle <= 180 ? NauticalSide.PORT : NauticalSide.STARBOARD;
        }
    }

    public static Function<Integer, Integer> getNextTWAFunctionForManeuverDirection(NauticalSide maneuverDirection) {
        Function<Integer, Integer> forNextTWA = null;
        switch (maneuverDirection) {
        case STARBOARD:
            forNextTWA = currentTWA -> (currentTWA + 1) % 360;
            break;
        case PORT:
            forNextTWA = currentTWA -> (currentTWA - 1) < 0 ? currentTWA - 1 + 360 : currentTWA - 1;
            break;
        }
        return forNextTWA;
    }

    public static double[] flipManeuversDirection(double[] valuesPerTWA) {
        double[] newValuesPerTWA = new double[360];
        for (int i = 0; i < 360; i++) {
            newValuesPerTWA[(360 - i) % 360] = valuesPerTWA[i];
        }
        return newValuesPerTWA;
    }
}
