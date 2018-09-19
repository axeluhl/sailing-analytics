package com.sap.sailing.windestimation.maneuvergraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.tracking.impl.WindWithConfidenceImpl;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.polarsfitting.SailingStatistics;
import com.sap.sailing.windestimation.util.WindUtil;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Speed;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.impl.DegreeBearingImpl;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class BestPathsCalculator {

    protected static final double INTERVAL_FOR_WIND_PATH_DEVIATION_ANALYSIS_IN_SECONDS = 30 * 60;
    protected static final double MAX_ABS_WIND_COURSE_DEVIATION_TOLERANCE_WITHIN_ANALYSIS_INTERVAL_IN_DEGREES = 30;

    private GraphLevel lastLevel;

    private Map<GraphLevel, BestPathsPerLevel> bestPathsPerLevel;
    private final boolean preciseConfidence;
    private final GraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator;

    public BestPathsCalculator(GraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
        this(true, transitionProbabilitiesCalculator);
    }

    public BestPathsCalculator(boolean preciseConfidence,
            GraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
        this.preciseConfidence = preciseConfidence;
        this.transitionProbabilitiesCalculator = transitionProbabilitiesCalculator;
    }

    public GraphNodeTransitionProbabilitiesCalculator getTransitionProbabilitiesCalculator() {
        return transitionProbabilitiesCalculator;
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
        Iterator<Entry<GraphLevel, BestPathsPerLevel>> iterator = bestPathsPerLevel.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<GraphLevel, BestPathsPerLevel> entry = iterator.next();
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
            BestPathsPerLevel bestPathsUntilLevel = new BestPathsPerLevel(currentLevel);
            for (GraphNode currentNode : currentLevel.getLevelNodes()) {
                IntersectedWindRange initialWindRange = transitionProbabilitiesCalculator
                        .getInitialWindRange(currentNode, currentLevel);
                double probability = currentNode.getConfidence() / currentLevel.getLevelNodes().size();
                BestManeuverNodeInfo currentNodeInfo = bestPathsUntilLevel.addBestPreviousNodeInfo(currentNode, null,
                        probability, initialWindRange, null);
                currentNodeInfo.setForwardProbability(probability);
            }
            bestPathsPerLevel.put(currentLevel, bestPathsUntilLevel);
        } else {
            BestPathsPerLevel bestPathsUntilPreviousLevel = bestPathsPerLevel.get(previousLevel);
            BestPathsPerLevel bestPathsUntilLevel = new BestPathsPerLevel(currentLevel);
            for (GraphNode currentNode : currentLevel.getLevelNodes()) {
                double bestProbabilityFromStart = 0;
                double forwardProbability = 0;
                GraphNode bestPreviousNode = null;
                IntersectedWindRange bestIntersectedWindRangeUntilCurrentNode = null;
                SailingStatistics bestPreviousPathStats = null;
                for (GraphNode previousNode : previousLevel.getLevelNodes()) {
                    BestManeuverNodeInfo previousNodeInfo = bestPathsUntilPreviousLevel
                            .getBestPreviousNodeInfo(previousNode);
                    Pair<IntersectedWindRange, Double> newWindRangeAndProbability = transitionProbabilitiesCalculator
                            .mergeWindRangeAndGetTransitionProbability(previousNode, previousLevel, previousNodeInfo,
                                    currentNode, currentLevel);
                    SailingStatistics previousPathStats = previousNodeInfo
                            .getPathSailingStatistics(previousLevel.getManeuver().getBoatClass());
                    double transitionObservationMultipliedProbability = newWindRangeAndProbability.getB()
                            * currentNode.getConfidence();
                    double probabilityFromStart = bestPathsUntilPreviousLevel.getNormalizedProbabilityToNodeFromStart(
                            previousNode) * transitionObservationMultipliedProbability;
                    forwardProbability += transitionObservationMultipliedProbability
                            * bestPathsUntilPreviousLevel.getNormalizedForwardProbability(previousNode);
                    if (probabilityFromStart > bestProbabilityFromStart) {
                        bestProbabilityFromStart = probabilityFromStart;
                        bestPreviousNode = previousNode;
                        bestIntersectedWindRangeUntilCurrentNode = newWindRangeAndProbability.getA();
                        bestPreviousPathStats = previousPathStats;
                    }
                }
                BestManeuverNodeInfo currentNodeInfo = bestPathsUntilLevel.addBestPreviousNodeInfo(currentNode,
                        bestPreviousNode, bestProbabilityFromStart, bestIntersectedWindRangeUntilCurrentNode,
                        bestPreviousPathStats);
                currentNodeInfo.setForwardProbability(forwardProbability);
            }
            bestPathsPerLevel.put(currentLevel, bestPathsUntilLevel);
        }
        this.lastLevel = currentLevel;
    }

    public List<Triple<GraphLevel, GraphNode, Double>> getBestPath(GraphLevel lastLevel, GraphNode lastNode) {
        double probabilitiesSum = 0;
        BestPathsPerLevel bestPathsUntilLastLevel = bestPathsPerLevel.get(lastLevel);
        double lastNodeProbability = bestPathsUntilLastLevel.getNormalizedProbabilityToNodeFromStart(lastNode);
        if (preciseConfidence) {
            if (!bestPathsUntilLastLevel.isBackwardProbabilitiesComputed()) {
                computeBackwardProbabilities();
            }
            GraphLevel firstLevel = lastLevel;
            while (firstLevel.getPreviousLevel() != null) {
                firstLevel = firstLevel.getPreviousLevel();
            }
            probabilitiesSum = bestPathsUntilLastLevel.getForwardProbabilitiesSum();
            // BestPathsPerLevel firstLevelInfo = bestPathsPerLevel.get(firstLevel);
            // double backwardProbabilitiesSum = 0.0;
            // for (GraphNode currentNode : firstLevel.getLevelNodes()) {
            // backwardProbabilitiesSum += currentNode.getConfidence() / firstLevel.getLevelNodes().size()
            // * firstLevelInfo.getBestPreviousNodeInfo(currentNode).getBackwardProbability();
            // }
            // double forwardProbabilitiesSum = bestPathsUntilLastLevel.getForwardProbabilitiesSum();
            // probabilitiesSum = (forwardProbabilitiesSum + backwardProbabilitiesSum) / 2.0;
        } else {
            for (GraphNode node : lastLevel.getLevelNodes()) {
                double probability = bestPathsUntilLastLevel.getNormalizedProbabilityToNodeFromStart(node);
                probabilitiesSum += probability;
            }
        }
        List<Triple<GraphLevel, GraphNode, Double>> result = new LinkedList<>();
        GraphNode currentNode = lastNode;
        GraphLevel currentLevel = lastLevel;
        while (currentLevel != null) {
            BestPathsPerLevel currentLevelInfo = bestPathsPerLevel.get(currentLevel);
            BestManeuverNodeInfo currentNodeInfo = currentLevelInfo.getBestPreviousNodeInfo(currentNode);
            double nodeConfidence;
            if (preciseConfidence) {
                nodeConfidence = currentLevelInfo.getNormalizedForwardBackwardProbability(currentNode);
            } else {
                nodeConfidence = lastNodeProbability / probabilitiesSum;
            }
            Triple<GraphLevel, GraphNode, Double> entry = new Triple<>(currentLevel, currentNode, nodeConfidence);
            result.add(0, entry);
            currentNode = currentNodeInfo.getBestPreviousNode();
            currentLevel = currentLevel.getPreviousLevel();
        }
        return result;
    }

    public List<Triple<GraphLevel, GraphNode, Double>> getBestPath(GraphLevel lastLevel) {
        BestPathsPerLevel bestPathsUntilLevel = bestPathsPerLevel.get(lastLevel);
        double maxProbability = 0;
        GraphNode bestLastNode = null;
        for (GraphNode lastNode : lastLevel.getLevelNodes()) {
            double probability = bestPathsUntilLevel.getNormalizedProbabilityToNodeFromStart(lastNode);
            if (maxProbability < probability) {
                maxProbability = probability;
                bestLastNode = lastNode;
            }
        }
        return getBestPath(lastLevel, bestLastNode);
    }

    public List<WindWithConfidence<Void>> getWindTrack(List<Triple<GraphLevel, GraphNode, Double>> bestPath) {
        List<WindWithConfidence<Void>> windFixes = new ArrayList<>();
        if (!bestPath.isEmpty()) {
            IntersectedWindRange globalWindRange = null;
            for (ListIterator<Triple<GraphLevel, GraphNode, Double>> iterator = bestPath
                    .listIterator(bestPath.size()); iterator.hasPrevious();) {
                Triple<GraphLevel, GraphNode, Double> entry = iterator.previous();
                GraphLevel currentLevel = entry.getA();
                GraphNode currentNode = entry.getB();
                BestPathsPerLevel bestPathsUntilCurrentLevel = bestPathsPerLevel.get(currentLevel);
                BestManeuverNodeInfo currentNodeInfo = bestPathsUntilCurrentLevel.getBestPreviousNodeInfo(currentNode);
                globalWindRange = globalWindRange == null ? currentNodeInfo.getWindRange()
                        : globalWindRange.intersect(currentNodeInfo.getWindRange());
                if (!globalWindRange.isViolation() && globalWindRange.getAngleTowardStarboard() <= 20
                        && currentNode.getManeuverType() != ManeuverTypeForClassification.OTHER) {
                    DegreeBearingImpl windCourse = new DegreeBearingImpl(globalWindRange.getAvgWindCourse());
                    Speed avgWindSpeed = getWindSpeed(currentLevel.getManeuver(), windCourse);
                    Wind wind = new WindImpl(currentLevel.getManeuver().getManeuverPosition(),
                            currentLevel.getManeuver().getManeuverTimePoint(),
                            new KnotSpeedWithBearingImpl(avgWindSpeed.getKnots(), windCourse));
                    windFixes.add(
                            new WindWithConfidenceImpl<Void>(wind, entry.getC(), null, avgWindSpeed.getKnots() > 0));
                }
            }
        }
        Collections.reverse(windFixes);
        windFixes = WindUtil.getWindFixesWithAveragedWindSpeed(windFixes);
        return windFixes;
    }

    // public List<WindWithConfidence<Void>> getWindTrack(List<Triple<GraphLevel, GraphNode, Double>> bestPath) {
    // List<WindWithConfidence<Void>> windFixes = new ArrayList<>();
    // if (!bestPath.isEmpty()) {
    // Triple<GraphLevel, GraphNode, Double> lastEntry = bestPath.get(bestPath.size() - 1);
    // double confidence = bestPathsPerLevel.get(lastEntry.getA())
    // .getNormalizedProbabilityToNodeFromStart(lastEntry.getB());
    // IntersectedWindRange globalWindRange = null;
    // for (ListIterator<Triple<GraphLevel, GraphNode, Double>> iterator = bestPath
    // .listIterator(bestPath.size()); iterator.hasPrevious();) {
    // Triple<GraphLevel, GraphNode, Double> entry = iterator.previous();
    // GraphLevel currentLevel = entry.getA();
    // GraphNode currentNode = entry.getB();
    // BestPathsPerLevel bestPathsUntilCurrentLevel = bestPathsPerLevel.get(currentLevel);
    // BestManeuverNodeInfo currentNodeInfo = bestPathsUntilCurrentLevel.getBestPreviousNodeInfo(currentNode);
    // globalWindRange = globalWindRange == null ? currentNodeInfo.getWindRange()
    // : globalWindRange.intersect(currentNodeInfo.getWindRange());
    // if (!globalWindRange.isViolation() && globalWindRange.getAngleTowardStarboard() <= 20
    // && currentNode.getManeuverType() != ManeuverTypeForClassification.OTHER) {
    // DegreeBearingImpl windCourse = new DegreeBearingImpl(globalWindRange.getAvgWindCourse());
    // Speed avgWindSpeed = getWindSpeed(currentLevel.getManeuver(), windCourse);
    // Wind wind = new WindImpl(currentLevel.getManeuver().getManeuverPosition(),
    // currentLevel.getManeuver().getManeuverTimePoint(),
    // new KnotSpeedWithBearingImpl(avgWindSpeed.getKnots(), windCourse));
    // windFixes
    // .add(new WindWithConfidenceImpl<Void>(wind, confidence, null, avgWindSpeed.getKnots() > 0));
    // }
    // }
    // }
    // Collections.reverse(windFixes);
    // windFixes = getWindFixesWithAveragedWindSpeed(windFixes);
    // return windFixes;
    // }

    // public List<WindWithConfidence<Void>> getWindTrack(List<Triple<GraphLevel, GraphNode, Double>> bestPath) {
    // List<WindWithConfidence<Void>> windFixes = new ArrayList<>();
    // for (Triple<GraphLevel, GraphNode, Double> entry : bestPath) {
    // GraphLevel currentLevel = entry.getA();
    // GraphNode currentNode = entry.getB();
    // if (currentNode.getManeuverType() != ManeuverTypeForClassification.OTHER) {
    // DegreeBearingImpl windCourse = new DegreeBearingImpl(
    // currentNode.getValidWindRange().getAvgWindCourse());
    // Speed avgWindSpeed = getWindSpeed(currentLevel.getManeuver(), windCourse);
    // Wind wind = new WindImpl(currentLevel.getManeuver().getManeuverPosition(),
    // currentLevel.getManeuver().getManeuverTimePoint(),
    // new KnotSpeedWithBearingImpl(avgWindSpeed.getKnots(), windCourse));
    // windFixes.add(new WindWithConfidenceImpl<Void>(wind, entry.getC(), null, avgWindSpeed.getKnots() > 0));
    // }
    // }
    // windFixes = getWindFixesWithAveragedWindSpeed(windFixes);
    // return windFixes;
    // }

    public Speed getWindSpeed(ManeuverForEstimation maneuver, Bearing windCourse) {
        return new KnotSpeedImpl(0.0);
    }

    public boolean isPreciseConfidence() {
        return preciseConfidence;
    }

    public void computeBackwardProbabilities() {
        GraphLevel currentLevel = lastLevel;
        BestPathsPerLevel bestPathsUntilLastLevel = bestPathsPerLevel.get(currentLevel);
        for (GraphNode currentNode : currentLevel.getLevelNodes()) {
            // double probability = currentNode.getConfidence();
            BestManeuverNodeInfo currentNodeInfo = bestPathsUntilLastLevel.getBestPreviousNodeInfo(currentNode);
            // Pair<IntersectedWindRange, Double> newWindRangeAndProbability = transitionProbabilitiesCalculator
            // .mergeWindRangeAndGetTransitionProbability(currentNode, currentLevel, currentNodeInfo, nextNode,
            // nextLevel);
            currentNodeInfo.setBackwardProbability(1.0);
        }
        GraphLevel nextLevel = currentLevel;
        while ((currentLevel = currentLevel.getPreviousLevel()) != null) {
            BestPathsPerLevel bestPathsUntilLevel = bestPathsPerLevel.get(currentLevel);
            BestPathsPerLevel bestPathsUntilNextLevel = bestPathsPerLevel.get(nextLevel);
            for (GraphNode currentNode : currentLevel.getLevelNodes()) {
                BestManeuverNodeInfo currentNodeInfo = bestPathsUntilLevel.getBestPreviousNodeInfo(currentNode);
                double backwardProbability = 0;
                for (GraphNode nextNode : nextLevel.getLevelNodes()) {
                    Pair<IntersectedWindRange, Double> newWindRangeAndProbability = transitionProbabilitiesCalculator
                            .mergeWindRangeAndGetTransitionProbability(currentNode, currentLevel, currentNodeInfo,
                                    nextNode, nextLevel);
                    backwardProbability += nextNode.getConfidence() * newWindRangeAndProbability.getB()
                            * bestPathsUntilNextLevel.getNormalizedBackwardProbability(nextNode);
                }
                currentNodeInfo.setBackwardProbability(backwardProbability);
            }
            nextLevel = currentLevel;
        }
    }

}
