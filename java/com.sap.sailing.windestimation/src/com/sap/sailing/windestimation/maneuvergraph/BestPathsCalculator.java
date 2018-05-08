package com.sap.sailing.windestimation.maneuvergraph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class BestPathsCalculator<T extends ManeuverNodesLevel<T>> {

    private static final double INTERVAL_FOR_WIND_PATH_DEVIATION_ANALYSIS_IN_SECONDS = 30 * 60;
    private static final double MAX_ABS_WIND_COURSE_DEVIATION_TOLERANCE_WITHIN_ANALYSIS_INTERVAL_IN_DEGREES = 45;

    private T lastLevel;

    private Map<T, BestPathsUntilLevel> bestPathsPerLevel;

    public BestPathsCalculator() {
    }

    public void computeBestPathsFromScratch() {
        T previousLevel = lastLevel;
        if (previousLevel != null) {
            // find first level
            while (previousLevel.getPreviousLevel() != null) {
                previousLevel = previousLevel.getPreviousLevel();
            }
            computeBestPathsFromScratch(previousLevel);
        }
    }

    public void computeBestPathsFromScratch(T firstLevel) {
        resetState();
        T currentLevel = firstLevel;
        do {
            computeBestPathsToNextLevel(currentLevel);
        } while ((currentLevel = currentLevel.getNextLevel()) != null);
    }

    public void resetState() {
        lastLevel = null;
        bestPathsPerLevel = null;
    }

    public void computeBestPathsToNextLevel(T nextLevel) {
        T previousLevel = nextLevel.getPreviousLevel();
        if (previousLevel != lastLevel) {
            throw new IllegalArgumentException(
                    "The previous level of next level does not match with the last level processed by this calculator");
        }
        T currentLevel = nextLevel;
        if (previousLevel == null) {
            bestPathsPerLevel = new HashMap<>();
            BestPathsUntilLevel bestPathsUntilLevel = new BestPathsUntilLevel();
            for (FineGrainedPointOfSail currentNode : FineGrainedPointOfSail.values()) {
                bestPathsUntilLevel.windDeviationWithinBestPaths[currentNode.ordinal()] = new WindRange(
                        currentLevel.getWindCourseInDegrees(currentNode), 0, 0, 0);
                double maxProbability = 0;
                for (FineGrainedPointOfSail previousNode : FineGrainedPointOfSail.values()) {
                    double probability = currentLevel.getProbabilityFromPreviousLevelNodeToThisLevelNode(previousNode,
                            currentNode);
                    if (probability > maxProbability) {
                        maxProbability = probability;
                    }
                }
                bestPathsUntilLevel.probabilitiesOfBestPathToNodeFromStart[currentNode.ordinal()] = maxProbability;
            }
            bestPathsPerLevel.put(currentLevel, bestPathsUntilLevel);
        } else {
            BestPathsUntilLevel bestPathsUntilPreviousLevel = bestPathsPerLevel.get(previousLevel);
            double secondsPassedSincePreviousManeuver = currentLevel.getPreviousLevel().getManeuver().getTimePoint()
                    .until(currentLevel.getManeuver().getTimePoint()).asSeconds();
            BestPathsUntilLevel bestPathsUntilLevel = new BestPathsUntilLevel();
            for (FineGrainedPointOfSail currentNode : FineGrainedPointOfSail.values()) {
                double currentWindCourse = currentLevel.getWindCourseInDegrees(currentNode);
                double maxProbability = 0;
                FineGrainedPointOfSail bestPreviousNode = null;
                WindRange windDeviationUntilNodeWithinBestPath = null;
                for (FineGrainedPointOfSail previousNode : FineGrainedPointOfSail.values()) {
                    WindRange newWindDeviationUntilNodeWithinBestPath = getWindDeviationRangeForNextNode(previousLevel,
                            previousNode, currentLevel, currentNode, currentWindCourse,
                            secondsPassedSincePreviousManeuver);
                    double probability = bestPathsUntilPreviousLevel.probabilitiesOfBestPathToNodeFromStart[previousNode
                            .ordinal()]
                            * currentLevel.getProbabilityFromPreviousLevelNodeToThisLevelNode(previousNode, currentNode)
                            * getPenaltyFactorForTransitionConsideringWindRangeWithinBestPath(
                                    newWindDeviationUntilNodeWithinBestPath.getWindCourseDeviationRangeInDegrees());
                    if (probability > maxProbability) {
                        maxProbability = probability;
                        bestPreviousNode = previousNode;
                        windDeviationUntilNodeWithinBestPath = newWindDeviationUntilNodeWithinBestPath;
                    }
                }
                bestPathsUntilLevel.probabilitiesOfBestPathToNodeFromStart[currentNode.ordinal()] = maxProbability;
                bestPathsUntilLevel.bestPreviousNodes[currentNode.ordinal()] = bestPreviousNode;
                setNewWindDeviationWithinProvidedWindDeviationArray(currentLevel, currentNode,
                        windDeviationUntilNodeWithinBestPath, bestPathsUntilLevel.windDeviationWithinBestPaths);
            }
            bestPathsPerLevel.put(currentLevel, bestPathsUntilLevel);
        }
        this.lastLevel = currentLevel;
    }

    private void setNewWindDeviationWithinProvidedWindDeviationArray(T currentLevel, FineGrainedPointOfSail currentNode,
            WindRange windDeviationUntilNodeWithinBestPath, WindRange[] windDeviationWithinBestPaths) {
        WindRange windDeviationToSet;
        if (windDeviationUntilNodeWithinBestPath
                .getWindCourseDeviationRangeInDegrees() > MAX_ABS_WIND_COURSE_DEVIATION_TOLERANCE_WITHIN_ANALYSIS_INTERVAL_IN_DEGREES) {
            windDeviationToSet = new WindRange(currentLevel.getWindCourseInDegrees(currentNode), 0, 0, 0);
        } else {
            windDeviationToSet = windDeviationUntilNodeWithinBestPath;
        }
        windDeviationWithinBestPaths[currentNode.ordinal()] = windDeviationToSet;
    }

    private WindRange getWindDeviationRangeForNextNode(T previousLevel, FineGrainedPointOfSail previousNode,
            T currentLevel, FineGrainedPointOfSail currentNode, double currentWindCourse,
            double secondsPassedSincePreviousManeuver) {
        WindRange newWindDeviationUntilNodeWithinBestPath = bestPathsPerLevel
                .get(previousLevel).windDeviationWithinBestPaths[previousNode.ordinal()]
                        .calculateForNextManeuverNodesLevel(currentWindCourse, secondsPassedSincePreviousManeuver);
        if (newWindDeviationUntilNodeWithinBestPath
                .getWindCourseDeviationRangeInDegrees() > MAX_ABS_WIND_COURSE_DEVIATION_TOLERANCE_WITHIN_ANALYSIS_INTERVAL_IN_DEGREES
                && !newWindDeviationUntilNodeWithinBestPath
                        .isCalculatedWithinLastSeconds(INTERVAL_FOR_WIND_PATH_DEVIATION_ANALYSIS_IN_SECONDS)) {
            TimePoint currentTimePoint = currentLevel.getManeuver().getTimePoint();
            T levelWithinTimePeriodLimitForWindDeviationAnalysis = currentLevel;
            Stack<FineGrainedPointOfSail> pathForWindDeviationAnalysis = new Stack<>();
            pathForWindDeviationAnalysis.add(currentNode);
            FineGrainedPointOfSail bestPreviousNodeForLevelWithinTimePeriodLimitForWindDeviationAnalysis = currentNode;
            while (true) {
                T previousLevelToCheck = levelWithinTimePeriodLimitForWindDeviationAnalysis.getPreviousLevel();
                if (previousLevelToCheck != null && previousLevel.getManeuver().getTimePoint().until(currentTimePoint)
                        .asSeconds() <= INTERVAL_FOR_WIND_PATH_DEVIATION_ANALYSIS_IN_SECONDS) {
                    levelWithinTimePeriodLimitForWindDeviationAnalysis = previousLevelToCheck;
                    bestPreviousNodeForLevelWithinTimePeriodLimitForWindDeviationAnalysis = bestPathsPerLevel
                            .get(previousLevelToCheck).bestPreviousNodes[bestPreviousNodeForLevelWithinTimePeriodLimitForWindDeviationAnalysis
                                    .ordinal()];
                    pathForWindDeviationAnalysis
                            .push(bestPreviousNodeForLevelWithinTimePeriodLimitForWindDeviationAnalysis);
                } else {
                    break;
                }
            }
            newWindDeviationUntilNodeWithinBestPath = new WindRange(levelWithinTimePeriodLimitForWindDeviationAnalysis
                    .getWindCourseInDegrees(bestPreviousNodeForLevelWithinTimePeriodLimitForWindDeviationAnalysis), 0,
                    0, 0);
            pathForWindDeviationAnalysis.pop();
            FineGrainedPointOfSail pointOfSail;
            while ((pointOfSail = pathForWindDeviationAnalysis.pop()) != null) {
                double secondsPassed = levelWithinTimePeriodLimitForWindDeviationAnalysis.getManeuver().getTimePoint()
                        .until(levelWithinTimePeriodLimitForWindDeviationAnalysis.getNextLevel().getManeuver()
                                .getTimePoint())
                        .asSeconds();
                levelWithinTimePeriodLimitForWindDeviationAnalysis = levelWithinTimePeriodLimitForWindDeviationAnalysis
                        .getNextLevel();
                double windCourseInDegrees = levelWithinTimePeriodLimitForWindDeviationAnalysis
                        .getWindCourseInDegrees(pointOfSail.ordinal());
                newWindDeviationUntilNodeWithinBestPath = newWindDeviationUntilNodeWithinBestPath
                        .calculateForNextManeuverNodesLevel(windCourseInDegrees, secondsPassed);
            }
        }
        return newWindDeviationUntilNodeWithinBestPath;
    }

    private double getPenaltyFactorForTransitionConsideringWindRangeWithinBestPath(
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

    public List<Pair<T, FineGrainedPointOfSail>> getBestPath(T lastLevel, FineGrainedPointOfSail lastNode) {
        List<Pair<T, FineGrainedPointOfSail>> result = new LinkedList<>();
        FineGrainedPointOfSail currentNode = lastNode;
        T currentLevel = lastLevel;
        while (currentLevel != null) {
            Pair<T, FineGrainedPointOfSail> entry = new Pair<>(currentLevel, currentNode);
            result.add(0, entry);
            currentNode = bestPathsPerLevel.get(currentLevel).bestPreviousNodes[currentNode.ordinal()];
            currentLevel = currentLevel.getPreviousLevel();
        }
        return result;
    }

    public List<Pair<T, FineGrainedPointOfSail>> getBestPath(T lastLevel) {
        BestPathsUntilLevel bestPathsUntilLevel = bestPathsPerLevel.get(lastLevel);
        double maxProbability = 0;
        FineGrainedPointOfSail bestLastNode = null;
        for (FineGrainedPointOfSail pointOfSail : FineGrainedPointOfSail.values()) {
            double probability = bestPathsUntilLevel.probabilitiesOfBestPathToNodeFromStart[pointOfSail.ordinal()];
            if (maxProbability < probability) {
                maxProbability = probability;
                bestLastNode = pointOfSail;
            }
        }
        return getBestPath(lastLevel, bestLastNode);
    }

    public double getConfidenceOfBestPath(List<Pair<T, FineGrainedPointOfSail>> bestPath) {
        Pair<T, FineGrainedPointOfSail> lastLevelWithNode = bestPath.get(bestPath.size() - 1);
        FineGrainedPointOfSail lastNode = lastLevelWithNode.getB();
        BestPathsUntilLevel bestPathsUntilLastLevel = bestPathsPerLevel.get(lastLevelWithNode.getA());
        double[] probabilitiesOfBestPathToCoarseGrainedPointOfSail = new double[CoarseGrainedPointOfSail
                .values().length];
        for (FineGrainedPointOfSail pointOfSail : FineGrainedPointOfSail.values()) {
            double probability = bestPathsUntilLastLevel.probabilitiesOfBestPathToNodeFromStart[pointOfSail.ordinal()];
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
        double bestPathConfidence = probabilitiesOfBestPathToCoarseGrainedPointOfSail[lastNode.ordinal()]
                / sumOfprobabilitiesOfBestPathToCoarseGrainedPointOfSail;
        return bestPathConfidence;
    }

    private static class BestPathsUntilLevel {
        private FineGrainedPointOfSail[] bestPreviousNodes = new FineGrainedPointOfSail[FineGrainedPointOfSail
                .values().length];
        private double[] probabilitiesOfBestPathToNodeFromStart = new double[bestPreviousNodes.length];
        private WindRange[] windDeviationWithinBestPaths = new WindRange[bestPreviousNodes.length];
    }

}
