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

    private Map<T, FineGrainedPointOfSail[]> bestPreviousNodesPerLevel;
    private double[] probabilitiesOfBestPathToNodeFromStart;
    private WindRange[] windDeviationWithinBestPaths;

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
        bestPreviousNodesPerLevel = null;
        probabilitiesOfBestPathToNodeFromStart = null;
        windDeviationWithinBestPaths = null;
    }

    public void computeBestPathsToNextLevel(T nextLevel) {
        T previousLevel = nextLevel.getPreviousLevel();
        if (previousLevel != lastLevel) {
            throw new IllegalArgumentException(
                    "The previous level of next level does not match with the last level processed by this calculator");
        }
        T currentLevel = nextLevel;
        if (previousLevel == null) {
            probabilitiesOfBestPathToNodeFromStart = new double[FineGrainedPointOfSail.values().length];
            bestPreviousNodesPerLevel = new HashMap<>();
            for (FineGrainedPointOfSail currentNode : FineGrainedPointOfSail.values()) {
                windDeviationWithinBestPaths[currentNode.ordinal()] = new WindRange(
                        currentLevel.getWindCourseInDegrees(currentNode), 0, 0, 0);
                double maxProbability = 0;
                for (FineGrainedPointOfSail previousNode : FineGrainedPointOfSail.values()) {
                    double probability = currentLevel.getProbabilityFromPreviousLevelNodeToThisLevelNode(previousNode,
                            currentNode);
                    if (probability > maxProbability) {
                        maxProbability = probability;
                    }
                }
                probabilitiesOfBestPathToNodeFromStart[currentNode.ordinal()] = maxProbability;
            }
        } else {
            double secondsPassedSincePreviousManeuver = currentLevel.getPreviousLevel().getManeuver().getTimePoint()
                    .until(currentLevel.getManeuver().getTimePoint()).asSeconds();
            double[] newProbabilitiesOfBestPathToNodeFromStart = new double[probabilitiesOfBestPathToNodeFromStart.length];
            FineGrainedPointOfSail[] bestPreviousNodes = new FineGrainedPointOfSail[probabilitiesOfBestPathToNodeFromStart.length];
            WindRange[] newWindDeviationWithinBestPaths = new WindRange[probabilitiesOfBestPathToNodeFromStart.length];
            for (FineGrainedPointOfSail currentNode : FineGrainedPointOfSail.values()) {
                double currentWindCourse = currentLevel.getWindCourseInDegrees(currentNode);
                double maxProbability = 0;
                FineGrainedPointOfSail bestPreviousNode = null;
                WindRange windDeviationUntilNodeWithinBestPath = null;
                for (FineGrainedPointOfSail previousNode : FineGrainedPointOfSail.values()) {
                    WindRange newWindDeviationUntilNodeWithinBestPath = getWindDeviationRangeForNextNode(previousLevel,
                            previousNode, currentLevel, currentNode, currentWindCourse,
                            secondsPassedSincePreviousManeuver);
                    double probability = probabilitiesOfBestPathToNodeFromStart[previousNode.ordinal()]
                            * currentLevel.getProbabilityFromPreviousLevelNodeToThisLevelNode(previousNode, currentNode)
                            * getPenaltyFactorForTransitionConsideringWindRangeWithinPreviousBestPath(
                                    newWindDeviationUntilNodeWithinBestPath.getWindCourseDeviationRangeInDegrees());
                    if (probability > maxProbability) {
                        maxProbability = probability;
                        bestPreviousNode = previousNode;
                        windDeviationUntilNodeWithinBestPath = newWindDeviationUntilNodeWithinBestPath;
                    }
                }
                newProbabilitiesOfBestPathToNodeFromStart[currentNode.ordinal()] = maxProbability;
                bestPreviousNodes[currentNode.ordinal()] = bestPreviousNode;
                setNewWindDeviationWithinProvidedWindDeviationArray(currentLevel, currentNode,
                        windDeviationUntilNodeWithinBestPath, newWindDeviationWithinBestPaths);
            }
            this.probabilitiesOfBestPathToNodeFromStart = newProbabilitiesOfBestPathToNodeFromStart;
            this.bestPreviousNodesPerLevel.put(currentLevel, bestPreviousNodes);
            this.windDeviationWithinBestPaths = newWindDeviationWithinBestPaths;
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
        WindRange newWindDeviationUntilNodeWithinBestPath = windDeviationWithinBestPaths[previousNode.ordinal()]
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
                    bestPreviousNodeForLevelWithinTimePeriodLimitForWindDeviationAnalysis = bestPreviousNodesPerLevel
                            .get(previousLevelToCheck)[bestPreviousNodeForLevelWithinTimePeriodLimitForWindDeviationAnalysis
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

    private double getPenaltyFactorForTransitionConsideringWindRangeWithinPreviousBestPath(
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

    public double getProbabilityOfBestPathToNodeFromStart(FineGrainedPointOfSail lastNode) {
        return probabilitiesOfBestPathToNodeFromStart[lastNode.ordinal()];
    }

    public List<Pair<T, FineGrainedPointOfSail>> getBestPath(T lastLevel, FineGrainedPointOfSail lastNode) {
        List<Pair<T, FineGrainedPointOfSail>> result = new LinkedList<>();
        FineGrainedPointOfSail currentNode = lastNode;
        T currentLevel = lastLevel;
        while (currentLevel != null) {
            Pair<T, FineGrainedPointOfSail> entry = new Pair<>(currentLevel, currentNode);
            result.add(0, entry);
            currentNode = bestPreviousNodesPerLevel.get(currentLevel)[currentNode.ordinal()];
            currentLevel = currentLevel.getPreviousLevel();
        }
        return result;
    }

}
