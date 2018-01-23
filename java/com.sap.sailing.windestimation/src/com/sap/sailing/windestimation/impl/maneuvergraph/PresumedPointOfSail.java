package com.sap.sailing.windestimation.impl.maneuvergraph;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public enum PresumedPointOfSail {
    UPWIND_STARBOARD(45), UPWIND_PORT(315), REACHING_STARBOARD(90), REACHING_PORT(270), DOWNWIND_STARBOARD(145), DOWNWIND_PORT(215);
    
    private final Bearing referenceTwa;
    
    private PresumedPointOfSail(int referenceTwa) {
        this.referenceTwa = new DegreeBearingImpl(referenceTwa);
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
    
    public Bearing getReferenceTwa() {
        return referenceTwa;
    }
    
}
