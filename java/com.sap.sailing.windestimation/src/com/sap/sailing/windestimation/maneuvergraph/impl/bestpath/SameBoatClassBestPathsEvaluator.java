package com.sap.sailing.windestimation.maneuvergraph.impl.bestpath;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.windestimation.maneuvergraph.FineGrainedManeuverType;
import com.sap.sailing.windestimation.maneuvergraph.FineGrainedPointOfSail;
import com.sap.sailing.windestimation.maneuvergraph.ManeuverNodesLevel;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class SameBoatClassBestPathsEvaluator<T extends ManeuverNodesLevel<T>> implements BestPathsEvaluator<T> {

    @Override
    public BestPathEvaluationResult<T> evaluateBestPath(List<Pair<T, FineGrainedPointOfSail>> bestPath) {
        BestPathEvaluationResult<T> result = new BestPathEvaluationResult<>();
        AverageStatistics<T> averageStatistics = calculateAverageStatistics(bestPath);
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
        for (Triple<T, FineGrainedManeuverType, FineGrainedPointOfSail> pair : averageStatistics.getCleanManeuvers()) {
            double tackProbabilityBonus = 0;
            T currentLevel = pair.getA();
            CompleteManeuverCurveWithEstimationData maneuver = currentLevel.getManeuver();
            FineGrainedManeuverType maneuverType = pair.getB();
            double absDirectionChangeInDegrees = Math
                    .abs(maneuver.getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees());
            double lowestSpeedRatio = maneuver.getMainCurve().getLowestSpeed().getKnots()
                    / maneuver.getMainCurve().getSpeedWithBearingBefore().getKnots();
            double turningRate = maneuver.getMainCurve().getMaxTurningRateInDegreesPerSecond();
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

            FineGrainedPointOfSail pointOfSailAfterManeuver = pair.getC();
            if (pointOfSailAfterManeuver != null) {
                double speed = maneuver.getCurveWithUnstableCourseAndSpeed().getAverageSpeedWithBearingAfter()
                        .getKnots();
                if (pointOfSailAfterManeuver.getLegType() == LegType.UPWIND) {
                    for (FineGrainedPointOfSail otherPointOfSail : FineGrainedPointOfSail.values()) {
                        if (otherPointOfSail.getLegType() == LegType.REACHING
                                || otherPointOfSail.getLegType() == LegType.DOWNWIND
                                        && averageStatistics.getNumberOfCleanTracks(otherPointOfSail) > 0) {
                            double speedRatio = speed
                                    / averageStatistics.getAverageSpeedInKnotsForPointOfSail(otherPointOfSail);
                            if (speedRatio < 0.95) {
                                tackProbabilityBonus -= (1 - speedRatio) / 2;
                            }
                        }
                    }
                } else if (lowestAverageSpeedUpwind != 0) {
                    double speedRatio = lowestAverageSpeedUpwind / speed;
                    if (speedRatio > 1.05) {
                        tackProbabilityBonus += (speedRatio - 1) / 2;
                    }
                }
            }
            if (tackProbabilityBonus != 0) {
                result.addTackProbabilityBonusForManeuverOfLevel(currentLevel, tackProbabilityBonus);
            }
        }
        return result;
    }

    private AverageStatistics<T> calculateAverageStatistics(List<Pair<T, FineGrainedPointOfSail>> bestPath) {
        AverageStatistics<T> averageStatistics = new AverageStatistics<>();
        for (ListIterator<Pair<T, FineGrainedPointOfSail>> iterator = bestPath.listIterator(); iterator.hasNext();) {
            Pair<T, FineGrainedPointOfSail> pair = iterator.next();
            T currentLevel = pair.getA();
            FineGrainedPointOfSail pointOfSailAfterCurrentManeuver = pair.getB();
            if (currentLevel.isManeuverClean() && !currentLevel.getManeuver().isMarkPassing()) {
                averageStatistics.addRecordToStatistics(currentLevel, pointOfSailAfterCurrentManeuver);

            }
        }
        return averageStatistics;
    }

    private static class AverageStatistics<T extends ManeuverNodesLevel<T>> {
        private List<Triple<T, FineGrainedManeuverType, FineGrainedPointOfSail>> cleanManeuvers = new ArrayList<>();
        // maneuver data
        private double[] sumOfAbsCourseChangesInDegreesPerManeuverType = new double[FineGrainedManeuverType
                .values().length];
        private double[] sumOfTurningRatesPerManeuverType = new double[sumOfAbsCourseChangesInDegreesPerManeuverType.length];
        private double[] sumOfRatiosBetweenLowestSpeedAndManeuverEnteringSpeedPerManeuverType = new double[sumOfAbsCourseChangesInDegreesPerManeuverType.length];
        private int[] maneuversCountPerManeuverType = new int[sumOfAbsCourseChangesInDegreesPerManeuverType.length];
        // speed data
        private double[] sumOfAverageSpeedsPerPointOfSail = new double[FineGrainedPointOfSail.values().length];
        private int[] trackCountPerPointOfSail = new int[sumOfAverageSpeedsPerPointOfSail.length];

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

        public void addRecordToStatistics(T maneuverLevel, FineGrainedPointOfSail pointOfSailAfterManeuver) {
            CompleteManeuverCurveWithEstimationData maneuver = maneuverLevel.getManeuver();
            FineGrainedManeuverType maneuverType = maneuverLevel.getTypeOfCleanManeuver(pointOfSailAfterManeuver);
            // maneuver data
            sumOfAbsCourseChangesInDegreesPerManeuverType[maneuverType.ordinal()] += Math
                    .abs(maneuver.getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees());
            sumOfTurningRatesPerManeuverType[maneuverType.ordinal()] += maneuver.getMainCurve()
                    .getMaxTurningRateInDegreesPerSecond();
            sumOfRatiosBetweenLowestSpeedAndManeuverEnteringSpeedPerManeuverType[maneuverType.ordinal()] += maneuver
                    .getMainCurve().getLowestSpeed().getKnots()
                    / maneuver.getMainCurve().getSpeedWithBearingBefore().getKnots();
            maneuversCountPerManeuverType[maneuverType.ordinal()]++;
            // speed data
            sumOfAverageSpeedsPerPointOfSail[pointOfSailAfterManeuver.ordinal()] += maneuver
                    .getCurveWithUnstableCourseAndSpeed().getAverageSpeedWithBearingAfter().getKnots();
            trackCountPerPointOfSail[pointOfSailAfterManeuver.ordinal()]++;
            cleanManeuvers.add(new Triple<>(maneuverLevel, maneuverType, pointOfSailAfterManeuver));
        }

        public List<Triple<T, FineGrainedManeuverType, FineGrainedPointOfSail>> getCleanManeuvers() {
            return cleanManeuvers;
        }

        public int getNumberOfCleanManeuvers(FineGrainedManeuverType maneuverType) {
            return maneuversCountPerManeuverType[maneuverType.ordinal()];
        }

        public int getNumberOfCleanTracks(FineGrainedPointOfSail pointOfSail) {
            return trackCountPerPointOfSail[pointOfSail.ordinal()];
        }

    }

}
