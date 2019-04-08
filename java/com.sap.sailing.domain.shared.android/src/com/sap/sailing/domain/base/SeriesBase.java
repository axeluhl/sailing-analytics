package com.sap.sailing.domain.base;

import com.sap.sse.common.Renamable;

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
    
    /**
     * For a multi-{@link #getFleets() fleet} series, the races of different fleets may or may not run in parallel. If
     * they can not run in parallel, the fleet's ordering as returned by {@link #getFleets()} is assumed to be the order
     * in which the fleets' races are to be run.
     * <p>
     * 
     * For example, if a league event defines a series with three fleets, Yellow, Blue and Red, and does not allow for
     * parallel fleet races and the series has race columns F1, F2, ..., then the execution order of those races is
     * assumed to be F1-Yellow, F1-Blue, F1-Red, F2-Yellow, F2-Blue, F2-Red, ...
     * <p>
     * 
     * If the fleets are allowed to run their races in parallel, no assumption about the race execution order can be
     * made except that within a fleet the races are usually run in the order as defined by the {@link #getRaceColumns()
     * race columns}.
     */
    boolean isFleetsCanRunInParallel();
}
