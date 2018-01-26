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
    UPWIND_STARBOARD(45), DOWNWIND_STARBOARD(135), DOWNWIND_PORT(225), UPWIND_PORT(315);
    
    private final int twa;

    private PresumedPointOfSail(int twa) {
        this.twa = twa;
    }
    
    public PresumedPointOfSail getNextPointOfSail(NauticalSide toSide) {
        int nextOrdinal = 0;
        switch (toSide) {
        case STARBOARD:
            nextOrdinal = (this.ordinal() + 1) % PresumedPointOfSail.values().length;
            break;
        case PORT:
            nextOrdinal = (this.ordinal() - 1 + PresumedPointOfSail.values().length) % PresumedPointOfSail.values().length;
            break;
        }
        return PresumedPointOfSail.values()[nextOrdinal];
    }
    
    public int getDifferenceInDegrees(PresumedPointOfSail otherPointOfSail) {
        int deviationDeg = this.getTwa() - otherPointOfSail.getTwa();
        if (deviationDeg < -180) {
            deviationDeg += 360;
        } else if (deviationDeg > 180) {
            deviationDeg -= 360;
        }
        return deviationDeg;
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
