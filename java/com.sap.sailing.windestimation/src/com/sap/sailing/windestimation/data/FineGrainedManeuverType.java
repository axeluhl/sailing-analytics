package com.sap.sailing.windestimation.data;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public enum FineGrainedManeuverType {

    TACK(CoarseGrainedManeuverType.TACK), JIBE(CoarseGrainedManeuverType.JIBE), _360(
            CoarseGrainedManeuverType._360), _180_JIBE(CoarseGrainedManeuverType._180), _180_TACK(
                    CoarseGrainedManeuverType._180), HEAD_UP_FROM_DOWNWIND_UNTIL_UPWIND(
                            CoarseGrainedManeuverType.HEAD_UP), HEAD_UP_FROM_REACHING_UNTIL_UPWIND(
                                    CoarseGrainedManeuverType.HEAD_UP), HEAD_UP_FROM_DOWNWIND_UNTIL_REACHING(
                                            CoarseGrainedManeuverType.HEAD_UP), HEAD_UP_AT_DOWNWIND(
                                                    CoarseGrainedManeuverType.HEAD_UP), HEAD_UP_AT_REACHING(
                                                            CoarseGrainedManeuverType.HEAD_UP), HEAD_UP_AT_UPWIND(
                                                                    CoarseGrainedManeuverType.HEAD_UP), BEAR_AWAY_FROM_UPWIND_UNTIL_DOWNWIND(
                                                                            CoarseGrainedManeuverType.BEAR_AWAY), BEAR_AWAY_FROM_UPWIND_UNTIL_REACHING(
                                                                                    CoarseGrainedManeuverType.BEAR_AWAY), BEAR_AWAY_FROM_REACHING_UNTIL_DOWNWIND(
                                                                                            CoarseGrainedManeuverType.BEAR_AWAY), BEAR_AWAY_AT_UPWIND(
                                                                                                    CoarseGrainedManeuverType.BEAR_AWAY), BEAR_AWAY_AT_REACHING(
                                                                                                            CoarseGrainedManeuverType.BEAR_AWAY), BEAR_AWAY_AT_DOWNWIND(
                                                                                                                    CoarseGrainedManeuverType.BEAR_AWAY);

    private final CoarseGrainedManeuverType coarseGrainedManeuverType;

    private FineGrainedManeuverType(CoarseGrainedManeuverType coarseGrainedManeuverType) {
        this.coarseGrainedManeuverType = coarseGrainedManeuverType;
    }

    public CoarseGrainedManeuverType getCoarseGrainedManeuverType() {
        return coarseGrainedManeuverType;
    }

}
