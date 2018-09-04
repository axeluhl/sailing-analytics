package com.sap.sailing.windestimation.maneuvergraph.bestpath;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.windestimation.maneuvergraph.CoarseGrainedPointOfSail;
import com.sap.sailing.windestimation.maneuvergraph.FineGrainedPointOfSail;
import com.sap.sailing.windestimation.maneuvergraph.GraphLevel;
import com.sap.sailing.windestimation.maneuvergraph.ProbabilityUtil;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class BestPathsCalculator {

    private static final double INTERVAL_FOR_WIND_PATH_DEVIATION_ANALYSIS_IN_SECONDS = 30 * 60;
    private static final double MAX_ABS_WIND_COURSE_DEVIATION_TOLERANCE_WITHIN_ANALYSIS_INTERVAL_IN_DEGREES = 45;

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
            double secondsPassedSincePreviousManeuver = currentLevel.getPreviousLevel().getManeuver()
                    .getManeuverTimePoint().until(currentLevel.getManeuver().getManeuverTimePoint()).asSeconds();
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
            // avoid that probability product becomes zero due to precision of Double
            ProbabilityUtil.normalizeLikelihoodArray(bestPathsUntilLevel.probabilitiesOfBestPathToNodeFromStart);
            bestPathsPerLevel.put(currentLevel, bestPathsUntilLevel);
        }
        this.lastLevel = currentLevel;
    }

    private void setNewWindDeviationWithinProvidedWindDeviationArray(GraphLevel currentLevel,
            FineGrainedPointOfSail currentNode, WindRange windDeviationUntilNodeWithinBestPath,
            WindRange[] windDeviationWithinBestPaths) {
        WindRange windDeviationToSet;
        if (windDeviationUntilNodeWithinBestPath
                .getWindCourseDeviationRangeInDegrees() > MAX_ABS_WIND_COURSE_DEVIATION_TOLERANCE_WITHIN_ANALYSIS_INTERVAL_IN_DEGREES) {
            windDeviationToSet = new WindRange(currentLevel.getWindCourseInDegrees(currentNode), 0, 0, 0);
        } else {
            windDeviationToSet = windDeviationUntilNodeWithinBestPath;
        }
        windDeviationWithinBestPaths[currentNode.ordinal()] = windDeviationToSet;
    }

    private WindRange getWindDeviationRangeForNextNode(GraphLevel previousLevel, FineGrainedPointOfSail previousNode,
            GraphLevel currentLevel, FineGrainedPointOfSail currentNode, double currentWindCourse,
            double secondsPassedSincePreviousManeuver) {
        WindRange newWindDeviationUntilNodeWithinBestPath = bestPathsPerLevel
                .get(previousLevel).windDeviationWithinBestPaths[previousNode.ordinal()]
                        .calculateForNextManeuverNodesLevel(currentWindCourse, secondsPassedSincePreviousManeuver);
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
                            .get(previousLevelToCheck).bestPreviousNodes[bestPreviousNodeForLevelWithinTimePeriodLimitForWindDeviationAnalysis
                                    .ordinal()];
                } else {
                    break;
                }
            }
            newWindDeviationUntilNodeWithinBestPath = new WindRange(levelWithinTimePeriodLimitForWindDeviationAnalysis
                    .getWindCourseInDegrees(pathForWindDeviationAnalysis.pop()), 0, 0, 0);
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

    public List<Pair<GraphLevel, FineGrainedPointOfSail>> getBestPath(GraphLevel lastLevel,
            FineGrainedPointOfSail lastNode) {
        List<Pair<GraphLevel, FineGrainedPointOfSail>> result = new LinkedList<>();
        FineGrainedPointOfSail currentNode = lastNode;
        GraphLevel currentLevel = lastLevel;
        while (currentLevel != null) {
            Pair<GraphLevel, FineGrainedPointOfSail> entry = new Pair<>(currentLevel, currentNode);
            result.add(0, entry);
            currentNode = bestPathsPerLevel.get(currentLevel).bestPreviousNodes[currentNode.ordinal()];
            currentLevel = currentLevel.getPreviousLevel();
        }
        return result;
    }

    public List<Pair<GraphLevel, FineGrainedPointOfSail>> getBestPath(GraphLevel lastLevel) {
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

    public double getConfidenceOfBestPath(List<Pair<GraphLevel, FineGrainedPointOfSail>> bestPath) {
        Pair<GraphLevel, FineGrainedPointOfSail> lastLevelWithNode = bestPath.get(bestPath.size() - 1);
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
        double bestPathConfidence = probabilitiesOfBestPathToCoarseGrainedPointOfSail[lastNode
                .getCoarseGrainedPointOfSail().ordinal()] / sumOfprobabilitiesOfBestPathToCoarseGrainedPointOfSail;
        return bestPathConfidence;
    }

    private static class BestPathsUntilLevel {
        private FineGrainedPointOfSail[] bestPreviousNodes = new FineGrainedPointOfSail[FineGrainedPointOfSail
                .values().length];
        private double[] probabilitiesOfBestPathToNodeFromStart = new double[bestPreviousNodes.length];
        private WindRange[] windDeviationWithinBestPaths = new WindRange[bestPreviousNodes.length];
    }

    private static class WindRange {

        private final double windCourseOnPortsideBoundary;
        private final double windCourseDeviationTowardStarboardInDegrees;
        private final double secondsPassedSincePortsideBoundaryRecord;
        private final double secondsPassedSinceStarboardBoundaryRecord;

        public WindRange(double windCourseOnPortsideBoundary, double windCourseDeviationTowardStarboardInDegrees,
                double secondsPassedSincePortsideBoundaryRecord, double secondsPassedSinceStarboardBoundaryRecord) {
            this.windCourseOnPortsideBoundary = windCourseOnPortsideBoundary;
            this.windCourseDeviationTowardStarboardInDegrees = windCourseDeviationTowardStarboardInDegrees;
            this.secondsPassedSincePortsideBoundaryRecord = secondsPassedSincePortsideBoundaryRecord;
            this.secondsPassedSinceStarboardBoundaryRecord = secondsPassedSinceStarboardBoundaryRecord;
        }

        public WindRange calculateForNextManeuverNodesLevel(double nextWindCourse,
                double secondsPassedSincePreviousManeuver) {
            double deviationFromPortsideBoundaryTowardStarboard = nextWindCourse - windCourseOnPortsideBoundary;
            if (deviationFromPortsideBoundaryTowardStarboard < 0) {
                deviationFromPortsideBoundaryTowardStarboard += 360;
            }
            double deviationFromRecordedWindCourseDeviationTowardStarboardInDegrees = deviationFromPortsideBoundaryTowardStarboard
                    - windCourseDeviationTowardStarboardInDegrees;
            if (deviationFromRecordedWindCourseDeviationTowardStarboardInDegrees <= 0) {
                // new wind course is within the previous wind deviation range
                return new WindRange(windCourseOnPortsideBoundary, windCourseDeviationTowardStarboardInDegrees,
                        secondsPassedSincePortsideBoundaryRecord + secondsPassedSincePreviousManeuver,
                        secondsPassedSinceStarboardBoundaryRecord + secondsPassedSincePreviousManeuver);
            } else {
                double deviationFromRecordedPortsideBoundaryTowardPortside = 360
                        - deviationFromPortsideBoundaryTowardStarboard;
                if (deviationFromRecordedWindCourseDeviationTowardStarboardInDegrees > deviationFromRecordedPortsideBoundaryTowardPortside) {
                    return new WindRange(nextWindCourse,
                            windCourseDeviationTowardStarboardInDegrees
                                    + deviationFromRecordedPortsideBoundaryTowardPortside,
                            0, secondsPassedSinceStarboardBoundaryRecord + secondsPassedSincePreviousManeuver);
                } else {
                    return new WindRange(windCourseOnPortsideBoundary, deviationFromPortsideBoundaryTowardStarboard,
                            secondsPassedSincePortsideBoundaryRecord + secondsPassedSincePreviousManeuver, 0);
                }
            }
        }

        public boolean isCalculatedWithinLastSeconds(double lastSeconds) {
            return secondsPassedSinceStarboardBoundaryRecord < lastSeconds
                    && secondsPassedSincePortsideBoundaryRecord < lastSeconds;
        }

        public double getWindCourseDeviationRangeInDegrees() {
            return windCourseDeviationTowardStarboardInDegrees;
        }

    }

}
