package com.sap.sailing.datamining.impl.components;

import java.util.function.Function;

import com.sap.sailing.datamining.shared.ManeuverSpeedDetailsSettings;
import com.sap.sailing.domain.common.NauticalSide;

public class ManeuverSpeedDetailsUtils {
    
    public static NauticalSide determineNauticalSideByClosestAngleDistance(double firstBearingAngle, double nextBearingAngle) {
        if(firstBearingAngle < nextBearingAngle) {
            return nextBearingAngle - firstBearingAngle <= 180 ? NauticalSide.STARBOARD : NauticalSide.PORT;
        } else {
            return firstBearingAngle - nextBearingAngle <= 180 ? NauticalSide.PORT : NauticalSide.STARBOARD;
        }
    }
    
    public static Function<Integer, Integer> getNextTWAFunctionForManeuverDirection(
            NauticalSide toSide) {
        return getNextTWAFunctionForManeuverDirection(toSide, null);
    }
    
    public static Function<Integer, Integer> getNextTWAFunctionForManeuverDirection(
            NauticalSide toSide, ManeuverSpeedDetailsSettings settings) {
        NauticalSide direction = settings != null && settings.isNormalizeManeuverDirection() ? settings.getNormalizedManeuverDirection() : toSide;
        Function<Integer, Integer> forNextTWA = null;
        switch (direction) {
        case STARBOARD:
            forNextTWA = currentTWA -> (currentTWA + 1) % 360;
            break;
        case PORT:
            forNextTWA = currentTWA -> (currentTWA - 1) < 0 ? currentTWA - 1 + 360
                    : currentTWA - 1;
            break;
        }
        return forNextTWA;
    }
}
