package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Renamable;

/**
 * Base interface for series consisting of static information describing the Series.
 */
public interface SeriesBase extends Renamable {

    /**
     * Returns the fleets of this series, in ascending order, better fleets first.
     */
    Iterable<? extends Fleet> getFleets();

    /**
     * Tells whether this is the "last" / "medal" race series, usually having only one race. This may have implications
     * on the scoring scheme (usually, medal races scores are doubled and cannot be discarded).
     */
    boolean isMedal();
}
