package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Named;

public interface SeriesData extends Named {
	
    /**
     * Returns the fleets of this series, in ascending order, better fleets first.
     */
    Iterable<? extends Fleet> getFleets();
    
    /**
     * A series consists of one or more "race columns." Some people would just say "race," but we use the term "race" for
     * something that has a single start time and start line; so if each fleet in a series gets their own start for
     * something called "R2", those are as many "races" as we have fleets; therefore, we use "race column" instead to
     * describe all "races" named, e.g., "R3" in a series.
     */
    Iterable<? extends RaceColumnInSeries> getRaceColumns();

    /**
     * Tells whether this is the "last" / "medal" race series, usually having only one race. This may have implications
     * on the scoring scheme (usually, medal races scores are doubled and cannot be discarded).
     */
    boolean isMedal();
}
