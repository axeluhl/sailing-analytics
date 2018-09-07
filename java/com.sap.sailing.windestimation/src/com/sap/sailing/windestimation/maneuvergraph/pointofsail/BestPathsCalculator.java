package com.sap.sailing.windestimation.maneuvergraph.pointofsail;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.windestimation.data.CoarseGrainedPointOfSail;
import com.sap.sailing.windestimation.data.FineGrainedManeuverType;
import com.sap.sailing.windestimation.data.FineGrainedPointOfSail;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.polarsfitting.SailingStatistics;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class BestPathsCalculator {

    protected static final double INTERVAL_FOR_WIND_PATH_DEVIATION_ANALYSIS_IN_SECONDS = 30 * 60;
    protected static final double MAX_ABS_WIND_COURSE_DEVIATION_TOLERANCE_WITHIN_ANALYSIS_INTERVAL_IN_DEGREES = 45;

    private GraphLevel lastLevel;

    private Map<GraphLevel, BestPathsUntilLevel> bestPathsPerLevel;

    public BestPathsCalculator() {
    }

    public void computeBestPathsFromScratch() {
        GraphLevel previousLevel = lastLevel;
        if (previousLevel != null) {
            // find first level
            while (previousLevel.getPreviousLevel() != null) {
                previousLevel = previousLevel.getPreviousLevel();
            }
            computeBestPathsFromScratch(previousLevel);
        }
    }

    public void computeBestPathsFromScratch(GraphLevel firstLevel) {
        resetState();
        GraphLevel currentLevel = firstLevel;
        do {
            computeBestPathsToNextLevel(currentLevel);
        } while ((currentLevel = currentLevel.getNextLevel()) != null);
    }

    public void recomputeBestPathsFromLevel(GraphLevel fromLevel) {
        List<GraphLevel> levelsToKeep = new LinkedList<>();
        GraphLevel currentLevel = fromLevel.getPreviousLevel();
        while (currentLevel != null) {
            levelsToKeep.add(currentLevel);
            currentLevel = currentLevel.getPreviousLevel();
        }
        Iterator<Entry<GraphLevel, BestPathsUntilLevel>> iterator = bestPathsPerLevel.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<GraphLevel, BestPathsUntilLevel> entry = iterator.next();
            if (!levelsToKeep.contains(entry.getKey())) {
                iterator.remove();
            }
        }
        lastLevel = fromLevel.getPreviousLevel();
        currentLevel = fromLevel;
        do {
            computeBestPathsToNextLevel(currentLevel);
        } while (currentLevel != null);
    }

    public void resetState() {
        lastLevel = null;
        bestPathsPerLevel = null;
    }

    public void computeBestPathsToNextLevel(GraphLevel nextLevel) {
        GraphLevel previousLevel = nextLevel.getPreviousLevel();
        if (previousLevel != lastLevel) {
            throw new IllegalArgumentException(
                    "The previous level of next level does not match with the last level processed by this calculator");
        }
        GraphLevel currentLevel = nextLevel;
        if (previousLevel == null) {
            bestPathsPerLevel = new HashMap<>();
            BestPathsUntilLevel bestPathsUntilLevel = new BestPathsUntilLevel();
            for (FineGrainedPointOfSail currentNode : FineGrainedPointOfSail.values()) {
                bestPathsUntilLevel.setWindDeviation(currentNode,
                        new WindCourseRange(currentLevel.getWindCourseInDegrees(currentNode), 0, 0, 0));
                double maxProbability = 0;
                for (FineGrainedPointOfSail previousNode : FineGrainedPointOfSail.values()) {
                    double probability = currentLevel.getProbabilityFromPreviousLevelNodeToThisLevelNode(previousNode,
                            currentNode);
                    if (probability > maxProbability) {
                        maxProbability = probability;
                    }
                }
                bestPathsUntilLevel.setProbabilityOfBestPathToNodeFromStart(currentNode, maxProbability);
                SailingStatistics pathStats = new SailingStatistics();
                pathStats.addRecordToStatistics(currentLevel.getManeuver(),
                        currentLevel.getTypeOfCleanManeuver(currentNode), currentNode);
                bestPathsUntilLevel.setPathStatistics(currentLevel, currentNode, pathStats);
            }
            bestPathsPerLevel.put(currentLevel, bestPathsUntilLevel);
        } else {
            BestPathsUntilLevel bestPathsUntilPreviousLevel = bestPathsPerLevel.get(previousLevel);
            double secondsPassedSincePreviousManeuver = currentLevel.getPreviousLevel().getManeuver()
                    .getManeuverTimePoint().until(currentLevel.getManeuver().getManeuverTimePoint()).asSeconds();
            BestPathsUntilLevel bestPathsUntilLevel = new BestPathsUntilLevel();
            for (FineGrainedPointOfSail currentNode : FineGrainedPointOfSail.values()) {
                double currentWindCourse = currentLevel.getWindCourseInDegrees(currentNode);
                double maxProbability = 0;
                FineGrainedPointOfSail bestPreviousNode = null;
                WindCourseRange windDeviationUntilNodeWithinBestPath = null;
                SailingStatistics bestPreviousPathStats = null;
                for (FineGrainedPointOfSail previousNode : FineGrainedPointOfSail.values()) {
                    SailingStatistics previousPathStats = bestPathsUntilPreviousLevel.getPathStatistics(previousLevel,
                            previousNode);
                    WindCourseRange newWindDeviationUntilNodeWithinBestPath = getWindDeviationRangeForNextNode(
                            previousLevel, previousNode, currentLevel, currentNode, currentWindCourse,
                            secondsPassedSincePreviousManeuver);
                    double probability = bestPathsUntilPreviousLevel
                            .getProbabilityOfBestPathToNodeFromStart(previousNode)

                            * currentLevel.getProbabilityFromPreviousLevelNodeToThisLevelNode(previousNode, currentNode)
                            * getPenaltyFactorForTransitionConsideringWindRangeWithinBestPath(
                                    newWindDeviationUntilNodeWithinBestPath.getWindCourseDeviationRangeInDegrees())
                            * getPenaltyFactorForTransitionConsideringPreviousPathStats(currentLevel, currentNode,
                                    previousPathStats);
                    if (probability > maxProbability) {
                        maxProbability = probability;
                        bestPreviousNode = previousNode;
                        windDeviationUntilNodeWithinBestPath = newWindDeviationUntilNodeWithinBestPath;
                        bestPreviousPathStats = previousPathStats;
                    }
                }
                bestPathsUntilLevel.setProbabilityOfBestPathToNodeFromStart(currentNode, maxProbability);
                bestPathsUntilLevel.setBestPreviousNode(currentNode, bestPreviousNode);
                if (windDeviationUntilNodeWithinBestPath
                        .getWindCourseDeviationRangeInDegrees() > BestPathsCalculator.MAX_ABS_WIND_COURSE_DEVIATION_TOLERANCE_WITHIN_ANALYSIS_INTERVAL_IN_DEGREES) {
                    windDeviationUntilNodeWithinBestPath = new WindCourseRange(
                            currentLevel.getWindCourseInDegrees(currentNode), 0, 0, 0);
                }
                bestPathsUntilLevel.setWindDeviation(currentNode, windDeviationUntilNodeWithinBestPath);
                SailingStatistics currentPathStats = bestPreviousPathStats.cloneAndAddRecordToStatistics(
                        currentLevel.getManeuver(), currentLevel.getTypeOfCleanManeuver(currentNode), currentNode);
                bestPathsUntilLevel.setPathStatistics(currentLevel, currentNode, currentPathStats);
            }
            // avoid that probability product becomes zero due to precision of Double
            bestPathsUntilLevel.normalizeProbabilities();
            bestPathsPerLevel.put(currentLevel, bestPathsUntilLevel);
        }
        this.lastLevel = currentLevel;
    }

    private WindCourseRange getWindDeviationRangeForNextNode(GraphLevel previousLevel,
            FineGrainedPointOfSail previousNode, GraphLevel currentLevel, FineGrainedPointOfSail currentNode,
            double currentWindCourse, double secondsPassedSincePreviousManeuver) {
        WindCourseRange newWindDeviationUntilNodeWithinBestPath = bestPathsPerLevel.get(previousLevel)
                .getWindDeviation(previousNode)
                .calculateForNextGraphLevel(currentWindCourse, secondsPassedSincePreviousManeuver);
        if (newWindDeviationUntilNodeWithinBestPath
                .getWindCourseDeviationRangeInDegrees() > MAX_ABS_WIND_COURSE_DEVIATION_TOLERANCE_WITHIN_ANALYSIS_INTERVAL_IN_DEGREES
                && !newWindDeviationUntilNodeWithinBestPath
                        .isCalculatedWithinLastSeconds(INTERVAL_FOR_WIND_PATH_DEVIATION_ANALYSIS_IN_SECONDS)) {
            TimePoint currentTimePoint = currentLevel.getManeuver().getManeuverTimePoint();
            GraphLevel levelWithinTimePeriodLimitForWindDeviationAnalysis = currentLevel;
            LinkedList<FineGrainedPointOfSail> pathForWindDeviationAnalysis = new LinkedList<>();
            FineGrainedPointOfSail bestPreviousNodeForLevelWithinTimePeriodLimitForWindDeviationAnalysis = currentNode;
            while (true) {
                if (bestPreviousNodeForLevelWithinTimePeriodLimitForWindDeviationAnalysis != null) {
                    pathForWindDeviationAnalysis.add(0,
                            bestPreviousNodeForLevelWithinTimePeriodLimitForWindDeviationAnalysis);
                }
                GraphLevel previousLevelToCheck = levelWithinTimePeriodLimitForWindDeviationAnalysis.getPreviousLevel();
                if (previousLevelToCheck != null && previousLevel.getManeuver().getManeuverTimePoint()
                        .until(currentTimePoint).asSeconds() <= INTERVAL_FOR_WIND_PATH_DEVIATION_ANALYSIS_IN_SECONDS) {
                    levelWithinTimePeriodLimitForWindDeviationAnalysis = previousLevelToCheck;
                    bestPreviousNodeForLevelWithinTimePeriodLimitForWindDeviationAnalysis = bestPathsPerLevel
                            .get(previousLevelToCheck)
                            .getBestPreviousNode(bestPreviousNodeForLevelWithinTimePeriodLimitForWindDeviationAnalysis);
                } else {
                    break;
                }
            }
            newWindDeviationUntilNodeWithinBestPath = new WindCourseRange(
                    levelWithinTimePeriodLimitForWindDeviationAnalysis
                            .getWindCourseInDegrees(pathForWindDeviationAnalysis.pop()),
                    0, 0, 0);
            for (FineGrainedPointOfSail pointOfSail : pathForWindDeviationAnalysis) {
                double secondsPassed = levelWithinTimePeriodLimitForWindDeviationAnalysis.getManeuver()
                        .getManeuverTimePoint().until(levelWithinTimePeriodLimitForWindDeviationAnalysis.getNextLevel()
                                .getManeuver().getManeuverTimePoint())
                        .asSeconds();
                levelWithinTimePeriodLimitForWindDeviationAnalysis = levelWithinTimePeriodLimitForWindDeviationAnalysis
                        .getNextLevel();
                double windCourseInDegrees = levelWithinTimePeriodLimitForWindDeviationAnalysis
                        .getWindCourseInDegrees(pointOfSail.ordinal());
                newWindDeviationUntilNodeWithinBestPath = newWindDeviationUntilNodeWithinBestPath
                        .calculateForNextGraphLevel(windCourseInDegrees, secondsPassed);
            }
        }
        return newWindDeviationUntilNodeWithinBestPath;
    }

    protected double getPenaltyFactorForTransitionConsideringWindRangeWithinBestPath(
            double windCourseDeviationInDegrees) {
        if (MAX_ABS_WIND_COURSE_DEVIATION_TOLERANCE_WITHIN_ANALYSIS_INTERVAL_IN_DEGREES < windCourseDeviationInDegrees) {
            return 1 / (4
                    + Math.pow(
                            (windCourseDeviationInDegrees
                                    - MAX_ABS_WIND_COURSE_DEVIATION_TOLERANCE_WITHIN_ANALYSIS_INTERVAL_IN_DEGREES) / 5,
                            2));
        }
        return 1;
    }

    protected double getPenaltyFactorForTransitionConsideringPreviousPathStats(GraphLevel currentLevel,
            FineGrainedPointOfSail currentNode, SailingStatistics previousPathStats) {
        if (previousPathStats == null) {
            return 1.0;
        } else {
            ManeuverForEstimation maneuver = currentLevel.getManeuver();
            double speedPenaltyFactorBefore = 1;
            double speedPenaltyFactorAfter = 1;
            double lowestSpeedAndTurningRatePenaltyFactor = 1;
            if (maneuver.isCleanBefore()) {
                FineGrainedPointOfSail pointOfSailBeforeManeuver = currentNode
                        .getNextPointOfSail(maneuver.getCourseChangeInDegrees() * -1);
                speedPenaltyFactorBefore = getSpeedPenaltyFactorForPointOfSail(previousPathStats,
                        pointOfSailBeforeManeuver, maneuver.getAverageSpeedWithBearingBefore().getKnots(),
                        maneuver.getBoatClass());
            }
            if (maneuver.isCleanAfter()) {
                speedPenaltyFactorAfter = getSpeedPenaltyFactorForPointOfSail(previousPathStats, currentNode,
                        maneuver.getAverageSpeedWithBearingAfter().getKnots(), maneuver.getBoatClass());
            }
            if (maneuver.isClean()) {
                lowestSpeedAndTurningRatePenaltyFactor = getLowestSpeedAndTurningRatePenaltyFactor(previousPathStats,
                        currentLevel, currentNode);
            }
            return speedPenaltyFactorBefore * speedPenaltyFactorAfter * lowestSpeedAndTurningRatePenaltyFactor;
        }
    }

    protected double getLowestSpeedAndTurningRatePenaltyFactor(SailingStatistics averageStatistics,
            GraphLevel currentLevel, FineGrainedPointOfSail currentNode) {
        double tackProbabilityBonus = 0;
        ManeuverForEstimation maneuver = currentLevel.getManeuver();
        FineGrainedManeuverType maneuverType = currentLevel.getTypeOfCleanManeuver(currentNode);
        double absDirectionChangeInDegrees = Math.abs(maneuver.getCourseChangeWithinMainCurveInDegrees());
        double lowestSpeedRatio = (maneuver.getSpeedLossRatio() + maneuver.getLowestSpeedVsExitingSpeedRatio()) / 2.0;
        double turningRate = maneuver.getMaxTurningRateInDegreesPerSecond();
        switch (maneuverType) {
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
                        tackProbabilityBonus -= lowestSpeedRatioDifference * 2 + turningRateDifference / -100;
                    }
                }
            }
            double courseChangeDifference = absDirectionChangeInDegrees
                    - averageStatistics.getAverageAbsCourseChangeInDegreesForManeuverType(FineGrainedManeuverType.JIBE);
            if (courseChangeDifference < 0) {
                tackProbabilityBonus -= courseChangeDifference / -180;
            }
            break;
        case _180_TACK:
            // if (averageStatistics.getNumberOfCleanManeuvers(FineGrainedManeuverType._180_JIBE) > 0) {
            // double lowestSpeedRatioDifference = lowestSpeedRatio - averageStatistics
            // .getAverageSpeedLossForManeuverType(
            // FineGrainedManeuverType._180_JIBE);
            // if (lowestSpeedRatioDifference > 0) {
            // tackProbabilityBonus -= lowestSpeedRatioDifference;
            // }
            // }
            break;
        case _180_JIBE:
            // double lowestSpeedRatioDifference = lowestSpeedRatio
            // - averageStatistics.getAverageSpeedLossForManeuverType(
            // FineGrainedManeuverType._180_TACK);
            // if (lowestSpeedRatioDifference < 0) {
            // tackProbabilityBonus += lowestSpeedRatioDifference * -1;
            // }
            break;
        case _360:
            break;
        default:
            if (averageStatistics.getNumberOfCleanManeuvers(FineGrainedManeuverType.TACK) > 0) {
                double lowestSpeedRatioDifference = lowestSpeedRatio
                        - averageStatistics.getAverageSpeedLossForManeuverType(FineGrainedManeuverType.TACK);
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
        return 1 - Math.abs(tackProbabilityBonus);
    }

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

    public List<Pair<GraphLevel, FineGrainedPointOfSail>> getBestPath(GraphLevel lastLevel,
            FineGrainedPointOfSail lastNode) {
        List<Pair<GraphLevel, FineGrainedPointOfSail>> result = new LinkedList<>();
        FineGrainedPointOfSail currentNode = lastNode;
        GraphLevel currentLevel = lastLevel;
        while (currentLevel != null) {
            Pair<GraphLevel, FineGrainedPointOfSail> entry = new Pair<>(currentLevel, currentNode);
            result.add(0, entry);
            currentNode = bestPathsPerLevel.get(currentLevel).getBestPreviousNode(currentNode);
            currentLevel = currentLevel.getPreviousLevel();
        }
        return result;
    }

    public List<Pair<GraphLevel, FineGrainedPointOfSail>> getBestPath(GraphLevel lastLevel) {
        BestPathsUntilLevel bestPathsUntilLevel = bestPathsPerLevel.get(lastLevel);
        double maxProbability = 0;
        FineGrainedPointOfSail bestLastNode = null;
        for (FineGrainedPointOfSail pointOfSail : FineGrainedPointOfSail.values()) {
            double probability = bestPathsUntilLevel.getProbabilityOfBestPathToNodeFromStart(pointOfSail);
            if (maxProbability < probability) {
                maxProbability = probability;
                bestLastNode = pointOfSail;
            }
        }
        return getBestPath(lastLevel, bestLastNode);
    }

    public double getConfidenceOfBestPath(List<Pair<GraphLevel, FineGrainedPointOfSail>> bestPath) {
        Pair<GraphLevel, FineGrainedPointOfSail> lastLevelWithNode = bestPath.get(bestPath.size() - 1);
        FineGrainedPointOfSail lastNode = lastLevelWithNode.getB();
        BestPathsUntilLevel bestPathsUntilLastLevel = bestPathsPerLevel.get(lastLevelWithNode.getA());
        double[] probabilitiesOfBestPathToCoarseGrainedPointOfSail = new double[CoarseGrainedPointOfSail
                .values().length];
        for (FineGrainedPointOfSail pointOfSail : FineGrainedPointOfSail.values()) {
            double probability = bestPathsUntilLastLevel.getProbabilityOfBestPathToNodeFromStart(pointOfSail);
            if (lastNode == pointOfSail
                    || lastNode.getCoarseGrainedPointOfSail() != pointOfSail.getCoarseGrainedPointOfSail()
                            && probability > probabilitiesOfBestPathToCoarseGrainedPointOfSail[pointOfSail
                                    .getCoarseGrainedPointOfSail().ordinal()]) {
                probabilitiesOfBestPathToCoarseGrainedPointOfSail[pointOfSail.getCoarseGrainedPointOfSail()
                        .ordinal()] = probability;
            }
        }
        double sumOfprobabilitiesOfBestPathToCoarseGrainedPointOfSail = 0;
        for (CoarseGrainedPointOfSail coarseGrainedPointOfSail : CoarseGrainedPointOfSail.values()) {
            sumOfprobabilitiesOfBestPathToCoarseGrainedPointOfSail = probabilitiesOfBestPathToCoarseGrainedPointOfSail[coarseGrainedPointOfSail
                    .ordinal()];
        }
        double bestPathConfidence = probabilitiesOfBestPathToCoarseGrainedPointOfSail[lastNode
                .getCoarseGrainedPointOfSail().ordinal()] / sumOfprobabilitiesOfBestPathToCoarseGrainedPointOfSail;
        return bestPathConfidence;
    }

}
