package com.sap.sailing.domain.common;
/**
 * The TackType describes how the boat is driving in relation to the next mark.
 */
public enum TackType {
    /**
     * The longtack takes you back towards the Windline that goes though the next mark. It is the longer tack in a leg.
     */
    LONGTACK,
    /**
     * The shorttack takes you away from this Windline. It is the shorter tack in a leg.
     */
    SHORTTACK,
    /**
     * If its undefinable, like besides the regatta time or the timepoint doesnt fit with the current leg time frame
     */
    NONE
}
