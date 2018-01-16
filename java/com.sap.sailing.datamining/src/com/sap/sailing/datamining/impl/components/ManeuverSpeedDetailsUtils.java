package com.sap.sailing.datamining.impl.components;

import java.util.function.Function;

import com.sap.sailing.datamining.data.HasManeuverSpeedDetailsContext;
import com.sap.sailing.domain.common.NauticalSide;

/**
 * Utils for {@link HasManeuverSpeedDetailsContext} calculations.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSpeedDetailsUtils {

    /**
     * Determines the maneuver direction by evaluating the difference between two bearings.
     * 
     * @param firstBearingAngle
     *            The first bearing
     * @param nextBearingAngle
     *            The next bearing
     * @return The direction with the least course change between the two provided bearings
     */
    public static NauticalSide determineNauticalSideByClosestAngleDistance(double firstBearingAngle,
            double nextBearingAngle) {
        if (firstBearingAngle < nextBearingAngle) {
            return nextBearingAngle - firstBearingAngle <= 180 ? NauticalSide.STARBOARD : NauticalSide.PORT;
        } else {
            return firstBearingAngle - nextBearingAngle <= 180 ? NauticalSide.PORT : NauticalSide.STARBOARD;
        }
    }

    /**
     * Gets a function which either increments or decrements its TWA parameter encoded as [0; 359] depending on the
     * provided maneuver direction.
     * 
     * @param maneuverDirection
     *            The maneuver direction which indicates whether the resulting function is going to increment or
     *            decrement its parameter
     * @return The function which either increments, or decrements its paramter
     */
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

    /**
     * Adapts the element indices in a way that the recorded maneuver is performed with the same value trend in the
     * opposite direction. This method is supposed to be used for direction normalization of maneuvers.
     * 
     * @param valuesPerTWA
     *            The values array with TWAs encoded as [0; 359]
     * @return The provided array with adapted indices for direction flip
     */
    public static double[] flipManeuversDirection(double[] valuesPerTWA) {
        double[] newValuesPerTWA = new double[360];
        for (int i = 0; i < 360; i++) {
            newValuesPerTWA[(360 - i) % 360] = valuesPerTWA[i];
        }
        return newValuesPerTWA;
    }
}
