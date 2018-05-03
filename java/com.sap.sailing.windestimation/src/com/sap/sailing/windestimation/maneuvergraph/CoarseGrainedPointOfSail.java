package com.sap.sailing.windestimation.maneuvergraph;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Tack;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public enum CoarseGrainedPointOfSail {
    UPWIND_STARBOARD(FineGrainedPointOfSail.VERY_CLOSE_HAULED_STARBOARD, FineGrainedPointOfSail.CLOSE_HAULED_STARBOARD,
            FineGrainedPointOfSail.CLOSE_REACH_STARBOARD), REACHING_STARBOARD(
                    FineGrainedPointOfSail.BEAM_REACH_STARBOARD), DOWNWIND_STARBOARD(
                            FineGrainedPointOfSail.BROAD_REACH_STARBOARD,
                            FineGrainedPointOfSail.VERY_BROAD_REACH_STARBOARD,
                            FineGrainedPointOfSail.RUNNING_STARBOARD), DOWNWIND_PORT(
                                    FineGrainedPointOfSail.RUNNING_PORT, FineGrainedPointOfSail.VERY_BROAD_REACH_PORT,
                                    FineGrainedPointOfSail.BROAD_REACH_PORT), REACHING_PORT(
                                            FineGrainedPointOfSail.BEAM_REACH_PORT), UPWIND_PORT(
                                                    FineGrainedPointOfSail.CLOSE_REACH_PORT,
                                                    FineGrainedPointOfSail.CLOSE_HAULED_PORT,
                                                    FineGrainedPointOfSail.VERY_CLOSE_HAULED_PORT);

    private final FineGrainedPointOfSail[] fineGrainedPointOfSailCoverage;

    private CoarseGrainedPointOfSail(FineGrainedPointOfSail... fineGrainedPointOfSailCoverage) {
        this.fineGrainedPointOfSailCoverage = fineGrainedPointOfSailCoverage;
    }

    public static final int getFineGrainedPointOfSailCount() {
        return 14;
    }

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

    public FineGrainedPointOfSail[] getFineGrainedPointOfSailCoverage() {
        return fineGrainedPointOfSailCoverage;
    }

}
