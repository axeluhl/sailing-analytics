package com.sap.sailing.windestimation.aggregator.polarsfitting;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.Tack;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public enum FineGrainedPointOfSail {
    VERY_CLOSE_HAULED_STARBOARD(30, CoarseGrainedPointOfSail.UPWIND_STARBOARD), CLOSE_HAULED_STARBOARD(45,
            CoarseGrainedPointOfSail.UPWIND_STARBOARD), CLOSE_REACH_STARBOARD(60,
                    CoarseGrainedPointOfSail.UPWIND_STARBOARD), BEAM_REACH_STARBOARD(90,
                            CoarseGrainedPointOfSail.REACHING_STARBOARD), BROAD_REACH_STARBOARD(120,
                                    CoarseGrainedPointOfSail.DOWNWIND_STARBOARD), VERY_BROAD_REACH_STARBOARD(150,
                                            CoarseGrainedPointOfSail.DOWNWIND_STARBOARD), RUNNING_STARBOARD(175,
                                                    CoarseGrainedPointOfSail.DOWNWIND_STARBOARD), RUNNING_PORT(185,
                                                            CoarseGrainedPointOfSail.DOWNWIND_PORT), VERY_BROAD_REACH_PORT(
                                                                    210,
                                                                    CoarseGrainedPointOfSail.DOWNWIND_PORT), BROAD_REACH_PORT(
                                                                            240,
                                                                            CoarseGrainedPointOfSail.DOWNWIND_PORT), BEAM_REACH_PORT(
                                                                                    270,
                                                                                    CoarseGrainedPointOfSail.REACHING_PORT), CLOSE_REACH_PORT(
                                                                                            300,
                                                                                            CoarseGrainedPointOfSail.UPWIND_PORT), CLOSE_HAULED_PORT(
                                                                                                    315,
                                                                                                    CoarseGrainedPointOfSail.UPWIND_PORT), VERY_CLOSE_HAULED_PORT(
                                                                                                            330,
                                                                                                            CoarseGrainedPointOfSail.UPWIND_PORT);

    private final int twa;
    private final CoarseGrainedPointOfSail coarseGrainedPointOfSail;

    private FineGrainedPointOfSail(int twa, CoarseGrainedPointOfSail coarseGrainedPointOfSail) {
        this.twa = twa;
        this.coarseGrainedPointOfSail = coarseGrainedPointOfSail;
    }

    public FineGrainedPointOfSail getNextPointOfSail(NauticalSide toSide) {
        int nextOrdinal = 0;
        switch (toSide) {
        case STARBOARD:
            nextOrdinal = (this.ordinal() + 1) % FineGrainedPointOfSail.values().length;
            break;
        case PORT:
            nextOrdinal = (this.ordinal() - 1 + FineGrainedPointOfSail.values().length)
                    % FineGrainedPointOfSail.values().length;
            break;
        }
        return FineGrainedPointOfSail.values()[nextOrdinal];
    }

    public FineGrainedPointOfSail getNextPointOfSail(double degreesToAdd) {
        double newTwa = (this.getTwa() + degreesToAdd) % 360;
        if (newTwa < 0) {
            newTwa += 360;
        }
        return valueOf(newTwa);
    }

    public static FineGrainedPointOfSail valueOf(double twa) {
        double twa360 = twa < 0 ? twa + 360 : twa;
        double smallestAbsTwaDeviation = Double.POSITIVE_INFINITY;
        FineGrainedPointOfSail bestPointOfSail = null;
        for (FineGrainedPointOfSail pointOfSail : FineGrainedPointOfSail.values()) {
            double absTwaDeviation = Math.abs(pointOfSail.getTwa() - twa360);
            if (absTwaDeviation < smallestAbsTwaDeviation) {
                smallestAbsTwaDeviation = absTwaDeviation;
                bestPointOfSail = pointOfSail;
            }
        }
        return bestPointOfSail;
    }

    public int getDifferenceInDegrees(FineGrainedPointOfSail otherPointOfSail) {
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
        case VERY_BROAD_REACH_PORT:
        case VERY_BROAD_REACH_STARBOARD:
        case RUNNING_PORT:
        case RUNNING_STARBOARD:
            return LegType.DOWNWIND;
        case VERY_CLOSE_HAULED_PORT:
        case VERY_CLOSE_HAULED_STARBOARD:
        case CLOSE_HAULED_PORT:
        case CLOSE_HAULED_STARBOARD:
            return LegType.UPWIND;
        case BROAD_REACH_PORT:
        case BROAD_REACH_STARBOARD:
        case CLOSE_REACH_PORT:
        case CLOSE_REACH_STARBOARD:
        case BEAM_REACH_PORT:
        case BEAM_REACH_STARBOARD:
            return LegType.REACHING;
        }
        return null;
    }

    public Tack getTack() {
        switch (this) {
        case VERY_BROAD_REACH_STARBOARD:
        case RUNNING_STARBOARD:
        case VERY_CLOSE_HAULED_STARBOARD:
        case CLOSE_HAULED_STARBOARD:
        case BROAD_REACH_STARBOARD:
        case CLOSE_REACH_STARBOARD:
        case BEAM_REACH_STARBOARD:
            return Tack.STARBOARD;
        case VERY_BROAD_REACH_PORT:
        case RUNNING_PORT:
        case VERY_CLOSE_HAULED_PORT:
        case CLOSE_HAULED_PORT:
        case BROAD_REACH_PORT:
        case CLOSE_REACH_PORT:
        case BEAM_REACH_PORT:
            return Tack.PORT;
        }
        return null;
    }

    public int getTwa() {
        return twa;
    }

    public CoarseGrainedPointOfSail getCoarseGrainedPointOfSail() {
        return coarseGrainedPointOfSail;
    }

    public double getWindCourse(double boatCourseInDegrees) {
        double windCourse = (boatCourseInDegrees - twa + 180) % 360;
        if (windCourse < 0) {
            windCourse += 360;
        }
        return windCourse;
    }

}
