package com.sap.sailing.windestimation.impl.graph;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Tack;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public enum PointOfSail {
    UPWIND_STARBOARD(45), UPWIND_PORT(315), REACHING_STARBOARD(90), REACHING_PORT(270), DOWNWIND_STARBOARD(145), DOWNWIND_PORT(215);
    
    private final int encodedTwaReference;
    
    private PointOfSail(int twaReference) {
        this.encodedTwaReference = twaReference;
    }

    public LegType getLegType() {
        switch (this) {
        case DOWNWIND_PORT:
        case DOWNWIND_STARBOARD:
            return LegType.DOWNWIND;
        case REACHING_PORT:
        case REACHING_STARBOARD:
            return LegType.REACHING;
        case UPWIND_PORT:
        case UPWIND_STARBOARD:
            return LegType.UPWIND;
        }
        return null;
    }

    public Tack getTack() {
        switch (this) {
        case DOWNWIND_PORT:
        case REACHING_PORT:
        case UPWIND_PORT:
            return Tack.PORT;
        case DOWNWIND_STARBOARD:
        case REACHING_STARBOARD:
        case UPWIND_STARBOARD:
            return Tack.STARBOARD;
        }
        return null;
    }
    
    public int getEncodedTwaReference() {
        return encodedTwaReference;
    }
}
