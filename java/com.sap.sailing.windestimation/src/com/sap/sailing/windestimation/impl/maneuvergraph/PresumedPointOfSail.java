package com.sap.sailing.windestimation.impl.maneuvergraph;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.Tack;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public enum PresumedPointOfSail {
    UPWIND_STARBOARD(45), UPWIND_PORT(315), DOWNWIND_STARBOARD(135), DOWNWIND_PORT(225);
    
    private final int twa;

    private PresumedPointOfSail(int twa) {
        this.twa = twa;
        
    }
    
    public LegType getLegType() {
        switch (this) {
        case DOWNWIND_PORT:
        case DOWNWIND_STARBOARD:
            return LegType.DOWNWIND;
        case UPWIND_PORT:
        case UPWIND_STARBOARD:
            return LegType.UPWIND;
        }
        return null;
    }

    public Tack getTack() {
        switch (this) {
        case DOWNWIND_PORT:
        case UPWIND_STARBOARD:
            return Tack.STARBOARD;
        case DOWNWIND_STARBOARD:
        case UPWIND_PORT:
            return Tack.STARBOARD;
        }
        return null;
    }
    
    public NauticalSide getSide() {
        switch (this) {
        case DOWNWIND_PORT:
        case UPWIND_PORT:
            return NauticalSide.PORT;
        case DOWNWIND_STARBOARD:
        case UPWIND_STARBOARD:
            return NauticalSide.STARBOARD;
        }
        return null;
    }
    
    public int getTwa() {
        return twa;
    }
    
}
