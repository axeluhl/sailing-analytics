package com.sap.sailing.datamining.impl.components;

import java.util.function.Function;

import com.sap.sailing.datamining.shared.ManeuverSpeedDetailsSettings;
import com.sap.sailing.domain.common.NauticalSide;

public class ManeuverSpeedDetailsUtils {
    public static Function<Integer, Integer> getNextTWAFunctionForManeuverDirection(
            NauticalSide toSide, ManeuverSpeedDetailsSettings settings) {
        NauticalSide direction = settings.isNormalizeManeuverDirection() ? settings.getNormalizedManeuverDirection() : toSide;
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
