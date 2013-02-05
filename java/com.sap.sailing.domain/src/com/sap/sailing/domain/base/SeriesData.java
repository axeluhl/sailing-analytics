package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Named;

public interface SeriesData extends Named {
	
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
