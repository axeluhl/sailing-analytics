package com.sap.sailing.domain.base;

import com.sap.sse.common.Color;
import com.sap.sse.common.Named;

/**
 * For each fleet in a {@link Series} there is one {@link RaceDefinition race} per "race column." Competitor to fleet
 * assignment may vary per race in case of the fleets being unordered in their {@link Series}, or may be fixed in case
 * the fleets are ordered in their {@link Series}, as usually the case in finals and medal series.
 * <p>
 * 
 * Fleets within the same {@link Series} as well as all fleets within the same {@link RaceColumn#getFleets() RaceColumn}
 * can be mutually compared to each other. Comparing fleets from different series produces undefined results. For a
 * {@link Series}, either all distinct fleets compare different (a series with ordered fleets such as Gold, Silver), or
 * all distinct fleets compare equal (e.g., for a qualifying series with fleets Yellow and Blue).
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface Fleet extends Named, Comparable<Fleet> {

    /**
     * The comparability is implemented by an integer field. For fleets of a series to compare equal, the constructor
     * without this ordering criterion should be chosen (implicitly setting it to 0).
     */
    int getOrdering();
    
    /**
     * The color associated with the fleet.
     * Normally each fleet gets a color code to distinguish between the fleets like 'blue', 'yellow' etc.
     * @return
     */
    Color getColor();
}
