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
        DISTANCE_TRAVELED, AVERAGE_SPEED_OVER_GROUND_IN_KNOTS, RANK_GAIN, CURRENT_SPEED_OVER_GROUND_IN_KNOTS,
        ESTIMATED_TIME_TO_NEXT_WAYPOINT_IN_SECONDS, VELOCITY_MADE_GOOD_IN_KNOTS, GAP_TO_LEADER_IN_SECONDS,
        WINDWARD_DISTANCE_TO_GO_IN_METERS;
    }

    List<LegDetailColumnType> getLegDetailsToShow();
}
