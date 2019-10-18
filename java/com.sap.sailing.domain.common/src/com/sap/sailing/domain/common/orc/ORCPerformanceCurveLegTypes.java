package com.sap.sailing.domain.common.orc;

import com.sap.sailing.domain.common.LegType;

public enum ORCPerformanceCurveLegTypes {
    TWA, WINDWARD_LEEWARD, LONG_DISTANCE, NON_SPINNAKER, CIRCULAR_RANDOM;

    /**
     * Determines a {@link LegType} to use for, e.g., determining a leg's distance. If no ORC leg type is provided
     * ({@code null}, or the type is {@link #TWA} or {@link #WINDWARD_LEEWARD}, the result will be {@code null}, telling
     * the system to compute the real leg type by itself, leading to a windward projection for upwind and downwind legs,
     * and rhumb-line projection for reaching legs.
     * <p>
     * 
     * For all other ORC leg types, rhumb-line projection is forced by returning {@link LegType#REACHING}.
     */
    static public LegType getLegType(ORCPerformanceCurveLegTypes orcLegType) {
        final LegType result;
        if (orcLegType == null || orcLegType == TWA || orcLegType == WINDWARD_LEEWARD) {
            result = null;
        } else {
            result = LegType.REACHING;
        }
        return result;
    }
}
