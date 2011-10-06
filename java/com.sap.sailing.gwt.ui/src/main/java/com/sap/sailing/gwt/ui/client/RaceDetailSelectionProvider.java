package com.sap.sailing.gwt.ui.client;

import java.util.List;

/**
 * A user may select a set of detail columns to be displayed when a race column is expanded.
 * An instance of a class implementing this interface can tell which column types are requested
 * to be displayed.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface RaceDetailSelectionProvider {
    static enum RaceDetailColumnType {
        DISTANCE_TRAVELED, AVERAGE_SPEED_OVER_GROUND_IN_KNOTS, GAP_TO_LEADER_IN_SECONDS;

        public String toString(StringConstants stringConstants) {
            switch (this) {
            case DISTANCE_TRAVELED:
                return stringConstants.distanceInMeters();
            case AVERAGE_SPEED_OVER_GROUND_IN_KNOTS:
                return stringConstants.averageSpeedInKnots();
            case GAP_TO_LEADER_IN_SECONDS:
                return stringConstants.gapToLeaderInSeconds();
            }
            return null;
        }
    }

    List<RaceDetailColumnType> getRaceDetailsToShow();
}
