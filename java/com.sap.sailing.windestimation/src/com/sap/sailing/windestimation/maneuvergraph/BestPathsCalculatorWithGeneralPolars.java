package com.sap.sailing.windestimation.maneuvergraph;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.windestimation.data.FineGrainedManeuverType;
import com.sap.sailing.windestimation.data.FineGrainedPointOfSail;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.polarsfitting.SailingStatistics;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class BestPathsCalculatorWithGeneralPolars extends BestPathsCalculator {

    @Override
    protected double getLowestSpeedAndTurningRatePenaltyFactor(SailingStatistics averageStatistics,
            GraphLevel currentLevel, GraphNode currentNode) {
        double tackProbabilityBonus = 0;
        ManeuverForEstimation maneuver = currentLevel.getManeuver();
        double absDirectionChangeInDegrees = Math.abs(maneuver.getCourseChangeWithinMainCurveInDegrees());
        double lowestSpeedRatio = (maneuver.getSpeedLossRatio() + maneuver.getLowestSpeedVsExitingSpeedRatio()) / 2.0;
        double turningRate = maneuver.getMaxTurningRateInDegreesPerSecond();
        switch (currentNode.getManeuverType()) {
        case TACK:
            for (FineGrainedManeuverType otherManeuverType : FineGrainedManeuverType.values()) {
                if (otherManeuverType != FineGrainedManeuverType.TACK
                        && otherManeuverType != FineGrainedManeuverType._180_JIBE
                        && otherManeuverType != FineGrainedManeuverType._180_TACK
                        && otherManeuverType != FineGrainedManeuverType._360
                        && averageStatistics.getNumberOfCleanManeuvers(otherManeuverType) > 0) {
                    double lowestSpeedRatioDifference = lowestSpeedRatio
                            - averageStatistics.getAverageSpeedLossForManeuverType(otherManeuverType);
                    double turningRateDifference = turningRate
                            - averageStatistics.getAverageTurningRateForManeuverType(otherManeuverType);
                    if (lowestSpeedRatioDifference > 0 && turningRateDifference < 0) {
                        tackProbabilityBonus = Math.min(tackProbabilityBonus,
                                -1 / (1 + lowestSpeedRatioDifference / 2 + turningRateDifference / -100));
                    }
                }
            }
            double courseChangeDifference = absDirectionChangeInDegrees
                    - averageStatistics.getAverageAbsCourseChangeInDegreesForManeuverType(FineGrainedManeuverType.JIBE);
            if (courseChangeDifference < 0) {
                tackProbabilityBonus = (1 + tackProbabilityBonus) * (1 - Math.min(0.1, courseChangeDifference / -360));
            }
            break;
        case JIBE:
        case OTHER:
            if (averageStatistics.getNumberOfCleanManeuvers(FineGrainedManeuverType.TACK) > 0) {
                double lowestSpeedRatioDifference = lowestSpeedRatio
                        - averageStatistics.getAverageSpeedLossForManeuverType(FineGrainedManeuverType.TACK);
                double turningRateDifference = turningRate
                        - averageStatistics.getAverageTurningRateForManeuverType(FineGrainedManeuverType.TACK);
                if (lowestSpeedRatioDifference < 0 && turningRateDifference > 0) {
                    tackProbabilityBonus += Math.max(tackProbabilityBonus,
                            1 / (1 + lowestSpeedRatioDifference / -2 + turningRateDifference / 100));
                }
                // courseChangeDifference = absDirectionChangeInDegrees - averageStatistics
                // .getAverageAbsCourseChangeInDegreesForManeuverType(FineGrainedManeuverType.TACK);
                // if (courseChangeDifference > 0) {
                // tackProbabilityBonus += courseChangeDifference / 180;
                // }
            }
            break;
        }
        double penaltyFactor = 1 - Math.abs(tackProbabilityBonus);
        return penaltyFactor;
    }

    @Override
    protected double getSpeedPenaltyFactorForPointOfSail(SailingStatistics averageStatistics,
            FineGrainedPointOfSail pointOfSail, double speedAtPointOfSail, BoatClass boatClass) {
        double lowestAverageSpeedUpwind = averageStatistics.getLowestUpwindAvgSpeed();
        double upwindProbabilityBonus = 0.0;
        if (pointOfSail.getLegType() == LegType.UPWIND) {
            for (FineGrainedPointOfSail otherPointOfSail : FineGrainedPointOfSail.values()) {
                if (otherPointOfSail.getLegType() == LegType.REACHING
                        || otherPointOfSail.getLegType() == LegType.DOWNWIND
                                && averageStatistics.getNumberOfCleanTracks(otherPointOfSail) > 0) {
                    double speedRatio = speedAtPointOfSail
                            / averageStatistics.getAverageSpeedInKnotsForPointOfSail(otherPointOfSail);
                    if (speedRatio > 1.05) {
                        upwindProbabilityBonus = Math.min(upwindProbabilityBonus, Math.max((1 - speedRatio), -0.8));
                    }
                }
            }
        } else if (lowestAverageSpeedUpwind != 0) {
            double speedRatio = lowestAverageSpeedUpwind / speedAtPointOfSail;
            if (speedRatio > 1.05) {
                upwindProbabilityBonus = Math.max((1 - speedRatio), -0.8);
            }
        }
        return 1.0 - Math.abs(upwindProbabilityBonus);
    }

}
