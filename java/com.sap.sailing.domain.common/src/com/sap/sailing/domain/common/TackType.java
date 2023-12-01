package com.sap.sailing.domain.common;
/**
 * The TackType describes how the boat is driving in relation to the next mark.
 */
public enum TackType {
    /**
     * The long tack takes you back towards the windline that goes though the next mark. It is the longer tack in a leg.
     */
    LONGTACK,
    /**
     * The short tack takes you away from the windline that goes though the next mark. It is the shorter tack in a leg.
     */
    SHORTTACK
}
