package com.sap.sailing.windestimation.aggregator.polarsfitting;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Tack;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public enum CoarseGrainedPointOfSail {
    UPWIND_STARBOARD, REACHING_STARBOARD, DOWNWIND_STARBOARD, DOWNWIND_PORT, REACHING_PORT, UPWIND_PORT;

    public LegType getLegType() {
        switch (this) {
        case DOWNWIND_PORT:
        case DOWNWIND_STARBOARD:
            return LegType.DOWNWIND;
        case UPWIND_PORT:
        case UPWIND_STARBOARD:
            return LegType.UPWIND;
        case REACHING_PORT:
        case REACHING_STARBOARD:
            return LegType.REACHING;
        }
        return null;
    }

    public Tack getTack() {
        switch (this) {
        case DOWNWIND_STARBOARD:
        case UPWIND_STARBOARD:
        case REACHING_STARBOARD:
            return Tack.STARBOARD;
        case DOWNWIND_PORT:
        case UPWIND_PORT:
        case REACHING_PORT:
            return Tack.PORT;
        }
        return null;
    }

    public Collection<CoarseGrainedPointOfSail> getNextPossiblePointOfSails(double degreesToAdd) {
        Set<CoarseGrainedPointOfSail> result = new HashSet<>();
        for (FineGrainedPointOfSail fineGrainedPointOfSail : FineGrainedPointOfSail.values()) {
            if (fineGrainedPointOfSail.getCoarseGrainedPointOfSail() == this) {
                CoarseGrainedPointOfSail coarseGrainedPointOfSail = fineGrainedPointOfSail
                        .getNextPointOfSail(degreesToAdd).getCoarseGrainedPointOfSail();
                result.add(coarseGrainedPointOfSail);
            }
        }
        return result;
    }

}
