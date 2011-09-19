package com.sap.sailing.gwt.ui.client;

import java.util.List;

/**
 * A user may select a set of detail columns to be displayed when a leg column is expanded.
 * An instance of a class implementing this interface can tell which column types are requested
 * to be displayed.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface LegDetailSelectionProvider {
    static enum LegDetailColumnType {
        DISTANCE_TRAVELED, AVERAGE_SPEED_OVER_GROUND_IN_KNOTS, RANK_GAIN; 
    }
    
    List<LegDetailColumnType> getLegDetailsToShow();
}
