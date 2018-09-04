package com.sap.sailing.windestimation.maneuvergraph.bestpath;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.maneuvergraph.FineGrainedManeuverType;
import com.sap.sailing.windestimation.maneuvergraph.FineGrainedPointOfSail;
import com.sap.sailing.windestimation.maneuvergraph.GraphLevel;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class SameBoatClassBestPathsEvaluator implements BestPathsEvaluator {

    @Override
    public BestPathEvaluationResult evaluateBestPath(List<Pair<GraphLevel, FineGrainedPointOfSail>> bestPath) {
        BestPathEvaluationResult result = new BestPathEvaluationResult();
        AverageStatistics averageStatistics = calculateAverageStatistics(bestPath);
        double lowestAverageSpeedUpwind = 0;
        for (FineGrainedPointOfSail pointOfSail : FineGrainedPointOfSail.values()) {
            if (pointOfSail.getLegType() == LegType.UPWIND
                    && averageStatistics.getNumberOfCleanTracks(pointOfSail) > 0) {
                double averageSpeed = averageStatistics.getAverageSpeedInKnotsForPointOfSail(pointOfSail);
                if (averageSpeed < lowestAverageSpeedUpwind) {
                    lowestAverageSpeedUpwind = averageSpeed;
                }
            }
        }
        for (Triple<GraphLevel, FineGrainedManeuverType, FineGrainedPointOfSail> triple : averageStatistics
                .getManeuvers()) {
            double tackProbabilityBonus = 0;
            GraphLevel currentLevel = triple.getA();
            ManeuverForEstimation maneuver = currentLevel.getManeuver();
            FineGrainedManeuverType maneuverType = triple.getB();
            double absDirectionChangeInDegrees = Math.abs(maneuver.getCourseChangeWithinMainCurveInDegrees());
            double lowestSpeedRatio = maneuver.getSpeedLossRatio();
            double turningRate = maneuver.getMaxTurningRateInDegreesPerSecond();
            switch (maneuverType) {
            case TACK:
                for (FineGrainedManeuverType otherManeuverType : FineGrainedManeuverType.values()) {
                    if (otherManeuverType != FineGrainedManeuverType.TACK
                            && otherManeuverType != FineGrainedManeuverType._180_JIBE
                            && otherManeuverType != FineGrainedManeuverType._180_TACK
                            && otherManeuverType != FineGrainedManeuverType._360
                            && averageStatistics.getNumberOfCleanManeuvers(otherManeuverType) > 0) {
                        double lowestSpeedRatioDifference = lowestSpeedRatio - averageStatistics
                                .getAverageRatioBetweenLowestSpeedAndManeuverEnteringSpeedForManeuverType(
                                        otherManeuverType);
                        double turningRateDifference = turningRate
                                - averageStatistics.getAverageTurningRateForManeuverType(otherManeuverType);
                        if (lowestSpeedRatioDifference > 0 && turningRateDifference < 0) {
                            tackProbabilityBonus -= lowestSpeedRatioDifference * 2 + turningRateDifference / -100;
                        }
                    }
                }
                double courseChangeDifference = absDirectionChangeInDegrees - averageStatistics
                        .getAverageAbsCourseChangeInDegreesForManeuverType(FineGrainedManeuverType.JIBE);
                if (courseChangeDifference < 0) {
                    tackProbabilityBonus -= courseChangeDifference / -180;
                }
                break;
            case _180_TACK:
                if (averageStatistics.getNumberOfCleanManeuvers(FineGrainedManeuverType._180_JIBE) > 0) {
                    double lowestSpeedRatioDifference = lowestSpeedRatio - averageStatistics
                            .getAverageRatioBetweenLowestSpeedAndManeuverEnteringSpeedForManeuverType(
                                    FineGrainedManeuverType._180_JIBE);
                    if (lowestSpeedRatioDifference > 0) {
                        tackProbabilityBonus -= lowestSpeedRatioDifference;
                    }
                }
                break;
            case _180_JIBE:
                double lowestSpeedRatioDifference = lowestSpeedRatio
                        - averageStatistics.getAverageRatioBetweenLowestSpeedAndManeuverEnteringSpeedForManeuverType(
                                FineGrainedManeuverType._180_TACK);
                if (lowestSpeedRatioDifference < 0) {
                    tackProbabilityBonus += lowestSpeedRatioDifference * -1;
                }
                break;
            default:
                if (averageStatistics.getNumberOfCleanManeuvers(FineGrainedManeuverType.TACK) > 0) {
                    lowestSpeedRatioDifference = lowestSpeedRatio - averageStatistics
                            .getAverageRatioBetweenLowestSpeedAndManeuverEnteringSpeedForManeuverType(
                                    FineGrainedManeuverType.TACK);
                    double turningRateDifference = turningRate
                            - averageStatistics.getAverageTurningRateForManeuverType(FineGrainedManeuverType.TACK);
                    if (lowestSpeedRatioDifference < 0 && turningRateDifference > 0) {
                        tackProbabilityBonus += lowestSpeedRatioDifference * -2 + turningRateDifference / 100;
                    }
                    // courseChangeDifference = absDirectionChangeInDegrees - averageStatistics
                    // .getAverageAbsCourseChangeInDegreesForManeuverType(FineGrainedManeuverType.TACK);
                    // if (courseChangeDifference > 0) {
                    // tackProbabilityBonus += courseChangeDifference / 180;
                    // }
                }
                break;
            }
            if (tackProbabilityBonus != 0) {
                result.addTackProbabilityBonusForManeuverOfLevel(currentLevel, tackProbabilityBonus);
            }

            FineGrainedPointOfSail pointOfSailAfterManeuver = triple.getC();
            if (maneuver.isCleanBefore()) {
                FineGrainedPointOfSail pointOfSailBeforeManeuver = pointOfSailAfterManeuver
                        .getNextPointOfSail(maneuver.getCourseChangeInDegrees() * -1);
                double upwindBeforeProbabilityBonus = getUpwindProbabilityBonusForPointOfSail(averageStatistics,
                        lowestAverageSpeedUpwind, pointOfSailBeforeManeuver,
                        maneuver.getAverageSpeedWithBearingBefore().getKnots());
                if (upwindBeforeProbabilityBonus != 0) {
                    result.addUpwindBeforeProbabilityBonusForManeuverOfLevel(currentLevel,
                            upwindBeforeProbabilityBonus);
                }
            }
            if (maneuver.isCleanAfter()) {
                double upwindAfterProbabilityBonus = getUpwindProbabilityBonusForPointOfSail(averageStatistics,
                        lowestAverageSpeedUpwind, pointOfSailAfterManeuver,
                        maneuver.getAverageSpeedWithBearingAfter().getKnots());
                if (upwindAfterProbabilityBonus != 0) {
                    result.addUpwindAfterProbabilityBonusForManeuverOfLevel(currentLevel, upwindAfterProbabilityBonus);
                }
            }
        }
        return result;
    }

    private double getUpwindProbabilityBonusForPointOfSail(AverageStatistics averageStatistics,
            double lowestAverageSpeedUpwind, FineGrainedPointOfSail pointOfSail, double speedAtPointOfSail) {
        double upwindProbabilityBonus = 0.0;
        if (pointOfSail.getLegType() == LegType.UPWIND) {
            for (FineGrainedPointOfSail otherPointOfSail : FineGrainedPointOfSail.values()) {
                if (otherPointOfSail.getLegType() == LegType.REACHING
                        || otherPointOfSail.getLegType() == LegType.DOWNWIND
                                && averageStatistics.getNumberOfCleanTracks(otherPointOfSail) > 0) {
                    double speedRatio = speedAtPointOfSail
                            / averageStatistics.getAverageSpeedInKnotsForPointOfSail(otherPointOfSail);
                    if (speedRatio < 0.95) {
                        upwindProbabilityBonus = Math.min(upwindProbabilityBonus, (speedRatio - 1) / 2);
                    }
                }
            }
        } else if (lowestAverageSpeedUpwind != 0) {
            double speedRatio = lowestAverageSpeedUpwind / speedAtPointOfSail;
            if (speedRatio > 1.05) {
                upwindProbabilityBonus = (speedRatio - 1) / 2;
            }
        }
        return upwindProbabilityBonus;
    }

    private AverageStatistics calculateAverageStatistics(List<Pair<GraphLevel, FineGrainedPointOfSail>> bestPath) {
        AverageStatistics averageStatistics = new AverageStatistics();
        for (ListIterator<Pair<GraphLevel, FineGrainedPointOfSail>> iterator = bestPath.listIterator(); iterator
                .hasNext();) {
            Pair<GraphLevel, FineGrainedPointOfSail> pair = iterator.next();
            GraphLevel currentLevel = pair.getA();
            FineGrainedPointOfSail pointOfSailAfterCurrentManeuver = pair.getB();
            averageStatistics.addRecordToStatistics(currentLevel, pointOfSailAfterCurrentManeuver);
        }
        return averageStatistics;
    }

    private static class AverageStatistics {
        private List<Triple<GraphLevel, FineGrainedManeuverType, FineGrainedPointOfSail>> cleanManeuvers = new ArrayList<>();
        // maneuver data
        private double[] sumOfAbsCourseChangesInDegreesPerManeuverType = new double[FineGrainedManeuverType
                .values().length];
        private double[] sumOfTurningRatesPerManeuverType = new double[sumOfAbsCourseChangesInDegreesPerManeuverType.length];
        private double[] sumOfRatiosBetweenLowestSpeedAndManeuverEnteringSpeedPerManeuverType = new double[sumOfAbsCourseChangesInDegreesPerManeuverType.length];
        private int[] maneuversCountPerManeuverType = new int[sumOfAbsCourseChangesInDegreesPerManeuverType.length];
        // speed data
        private double[] sumOfAverageSpeedsPerPointOfSail = new double[FineGrainedPointOfSail.values().length];
        private int[] trackCountPerPointOfSail = new int[sumOfAverageSpeedsPerPointOfSail.length];
        private List<Triple<GraphLevel, FineGrainedManeuverType, FineGrainedPointOfSail>> maneuverLevels = new ArrayList<>();

        public double getAverageAbsCourseChangeInDegreesForManeuverType(FineGrainedManeuverType maneuverType) {
            return maneuversCountPerManeuverType[maneuverType.ordinal()] == 0 ? 0
                    : sumOfAbsCourseChangesInDegreesPerManeuverType[maneuverType.ordinal()]
                            / maneuversCountPerManeuverType[maneuverType.ordinal()];
        }

        public double getAverageTurningRateForManeuverType(FineGrainedManeuverType maneuverType) {
            return maneuversCountPerManeuverType[maneuverType.ordinal()] == 0 ? 0
                    : sumOfTurningRatesPerManeuverType[maneuverType.ordinal()]
                            / maneuversCountPerManeuverType[maneuverType.ordinal()];
        }

        public double getAverageRatioBetweenLowestSpeedAndManeuverEnteringSpeedForManeuverType(
                FineGrainedManeuverType maneuverType) {
            return maneuversCountPerManeuverType[maneuverType.ordinal()] == 0 ? 0
                    : sumOfRatiosBetweenLowestSpeedAndManeuverEnteringSpeedPerManeuverType[maneuverType.ordinal()]
                            / maneuversCountPerManeuverType[maneuverType.ordinal()];
        }

        public double getAverageSpeedInKnotsForPointOfSail(FineGrainedPointOfSail pointOfSail) {
            return trackCountPerPointOfSail[pointOfSail.ordinal()] == 0 ? 0
                    : sumOfAverageSpeedsPerPointOfSail[pointOfSail.ordinal()]
                            / trackCountPerPointOfSail[pointOfSail.ordinal()];
        }

        public void addRecordToStatistics(GraphLevel maneuverLevel, FineGrainedPointOfSail pointOfSailAfterManeuver) {
            ManeuverForEstimation maneuver = maneuverLevel.getManeuver();
            FineGrainedManeuverType maneuverType = maneuverLevel.getTypeOfCleanManeuver(pointOfSailAfterManeuver);
            if (maneuver.isClean()) {
                // maneuver data
                sumOfAbsCourseChangesInDegreesPerManeuverType[maneuverType.ordinal()] += Math
                        .abs(maneuver.getCourseChangeWithinMainCurveInDegrees());
                sumOfTurningRatesPerManeuverType[maneuverType.ordinal()] += maneuver
                        .getMaxTurningRateInDegreesPerSecond();
                sumOfRatiosBetweenLowestSpeedAndManeuverEnteringSpeedPerManeuverType[maneuverType.ordinal()] += maneuver
                        .getSpeedLossRatio();
                maneuversCountPerManeuverType[maneuverType.ordinal()]++;
                cleanManeuvers.add(new Triple<>(maneuverLevel, maneuverType, pointOfSailAfterManeuver));
            }
            if (maneuver.isCleanBefore()) {
                // speed data
                FineGrainedPointOfSail pointOfSailBeforeManeuver = pointOfSailAfterManeuver
                        .getNextPointOfSail(maneuver.getCourseChangeInDegrees() * -1);
                sumOfAverageSpeedsPerPointOfSail[pointOfSailBeforeManeuver.ordinal()] += maneuver
                        .getAverageSpeedWithBearingBefore().getKnots();
                trackCountPerPointOfSail[pointOfSailBeforeManeuver.ordinal()]++;
            }
            if (maneuver.isCleanAfter()) {
                // speed data
                sumOfAverageSpeedsPerPointOfSail[pointOfSailAfterManeuver.ordinal()] += maneuver
                        .getAverageSpeedWithBearingAfter().getKnots();
                trackCountPerPointOfSail[pointOfSailAfterManeuver.ordinal()]++;
            }
            maneuverLevels.add(new Triple<>(maneuverLevel, maneuverType, pointOfSailAfterManeuver));
        }

        public int getNumberOfCleanManeuvers(FineGrainedManeuverType maneuverType) {
            return maneuversCountPerManeuverType[maneuverType.ordinal()];
        }

        public int getNumberOfCleanTracks(FineGrainedPointOfSail pointOfSail) {
            return trackCountPerPointOfSail[pointOfSail.ordinal()];
        }

        public List<Triple<GraphLevel, FineGrainedManeuverType, FineGrainedPointOfSail>> getManeuvers() {
            return maneuverLevels;
        }

    }

}
