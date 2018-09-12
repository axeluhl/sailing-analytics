package com.sap.sailing.windestimation.maneuvergraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.tracking.impl.WindWithConfidenceImpl;
import com.sap.sailing.windestimation.data.FineGrainedPointOfSail;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.polarsfitting.SailingStatistics;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Speed;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.impl.DegreeBearingImpl;

import smile.sort.QuickSelect;

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

    public BestPathsCalculator() {
        this(true);
    }

    public BestPathsCalculator(boolean preciseConfidence) {
        this.preciseConfidence = preciseConfidence;
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
                double probability = currentNode.getConfidence();
                IntersectedWindRange intersectedWindRange = currentNode.getValidWindRange().toIntersected();
                BestManeuverNodeInfo currentNodeInfo = bestPathsUntilLevel.addBestPreviousNodeInfo(currentNode, null,
                        probability, intersectedWindRange, null);
                currentNodeInfo.setForwardProbability(probability);
            }
            bestPathsPerLevel.put(currentLevel, bestPathsUntilLevel);
        } else {
            BestPathsPerLevel bestPathsUntilPreviousLevel = bestPathsPerLevel.get(previousLevel);
            double secondsPassedSincePreviousManeuver = currentLevel.getPreviousLevel().getManeuver()
                    .getManeuverTimePoint().until(currentLevel.getManeuver().getManeuverTimePoint()).asSeconds();
            BestPathsPerLevel bestPathsUntilLevel = new BestPathsPerLevel(currentLevel);
            for (GraphNode currentNode : currentLevel.getLevelNodes()) {
                double bestProbabilityFromStart = 0;
                double forwardProbability = 0;
                GraphNode bestPreviousNode = null;
                IntersectedWindRange bestIntersectedWindRangeUntilCurrentNode = null;
                SailingStatistics bestPreviousPathStats = null;
                for (GraphNode previousNode : previousLevel.getLevelNodes()) {
                    BestManeuverNodeInfo bestPreviousNodeInfo = bestPathsUntilPreviousLevel
                            .getBestPreviousNodeInfo(previousNode);
                    SailingStatistics previousPathStats = bestPreviousNodeInfo
                            .getPathSailingStatistics(previousLevel.getManeuver().getBoatClass());
                    IntersectedWindRange intersectedWindRangeUntilCurrentNode = bestPreviousNodeInfo.getWindRange()
                            .intersect(currentNode.getValidWindRange());
                    double transitionObservationMultipliedProbability = getTransitionObservationMultipliedProbability(
                            currentLevel, secondsPassedSincePreviousManeuver, currentNode, previousNode,
                            previousPathStats, intersectedWindRangeUntilCurrentNode);
                    double probabilityFromStart = bestPathsUntilPreviousLevel.getNormalizedProbabilityToNodeFromStart(
                            previousNode) * transitionObservationMultipliedProbability;
                    forwardProbability += transitionObservationMultipliedProbability;
                    assert (probabilityFromStart > 0.0001);
                    if (probabilityFromStart > bestProbabilityFromStart) {
                        bestProbabilityFromStart = probabilityFromStart;
                        bestPreviousNode = previousNode;
                        bestIntersectedWindRangeUntilCurrentNode = intersectedWindRangeUntilCurrentNode;
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

    private double getTransitionObservationMultipliedProbability(GraphLevel currentLevel,
            double secondsPassedSincePreviousManeuver, GraphNode currentNode, GraphNode previousNode,
            SailingStatistics previousPathStats, IntersectedWindRange intersectedWindRangeUntilCurrentNode) {
        return getPenaltyFactorForTackTransition(currentLevel, previousNode, currentNode) * currentNode.getConfidence()
                * getPenaltyFactorForTransitionConsideringWindRangeWithinBestPath(intersectedWindRangeUntilCurrentNode,
                        secondsPassedSincePreviousManeuver)
                * getPenaltyFactorForTransitionConsideringPreviousPathStats(currentLevel, currentNode,
                        previousPathStats, intersectedWindRangeUntilCurrentNode);
    }

    protected double getPenaltyFactorForTackTransition(GraphLevel currentLevel, GraphNode previousNode,
            GraphNode currentNode) {
        // if (previousNode.getManeuverType() != ManeuverTypeForClassification.OTHER
        // && currentNode.getManeuverType() != ManeuverTypeForClassification.OTHER) {
        // if (previousNode.getTackAfter() == currentNode.getTackAfter()) {
        // return 0.05;
        // }
        // } else if (previousNode.getManeuverType() == currentNode.getManeuverType()) {
        // // maneuver type is OTHER
        // if (previousNode.getTackAfter() != currentNode.getTackAfter()) {
        // return 0.05;
        // }
        // } else {
        // // one maneuver is OTHER, other maneuver is tack or jibe
        // if (previousNode.getManeuverType() == ManeuverTypeForClassification.OTHER) {
        // if (previousNode.getTackAfter() == currentNode.getTackAfter()) {
        // return 0.05;
        // }
        // } else {
        // if (previousNode.getTackAfter() != currentNode.getTackAfter()) {
        // return 0.05;
        // }
        // }
        // }
        return 1;
    }

    protected double getPenaltyFactorForTransitionConsideringWindRangeWithinBestPath(
            IntersectedWindRange intersectedWindRangeUntilCurrentNode, double secondsPassedSincePreviousWindRange) {
        double violationRange = intersectedWindRangeUntilCurrentNode.getViolationRange();
        double penaltyFactor;
        if (violationRange == 0) {
            penaltyFactor = 1.0;
        } else {
            // violationRange -= Math.max(1, secondsPassedSincePreviousWindRange / 3600)
            // * MAX_ABS_WIND_COURSE_DEVIATION_TOLERANCE_WITHIN_ANALYSIS_INTERVAL_IN_DEGREES;
            // if (violationRange <= 0) {
            // penaltyFactor = 1 / (1
            // + (MAX_ABS_WIND_COURSE_DEVIATION_TOLERANCE_WITHIN_ANALYSIS_INTERVAL_IN_DEGREES + violationRange)
            // * 0.1);
            // if (violationRange <= MAX_ABS_WIND_COURSE_DEVIATION_TOLERANCE_WITHIN_ANALYSIS_INTERVAL_IN_DEGREES) {
            // penaltyFactor = 1 / (1 + Math.pow((violationRange) / 15, 2));
            // } else {
            penaltyFactor = 1 / (1 + (Math.pow(violationRange, 2)));
            // }
        }
        assert (penaltyFactor > 0.0001);
        return penaltyFactor;
    }

    protected double getPenaltyFactorForTransitionConsideringPreviousPathStats(GraphLevel currentLevel,
            GraphNode currentNode, SailingStatistics previousPathStats, IntersectedWindRange intersectedWindRange) {
        if (previousPathStats == null) {
            return 1.0;
        } else {
            ManeuverForEstimation maneuver = currentLevel.getManeuver();
            double speedPenaltyFactorBefore = 1;
            double speedPenaltyFactorAfter = 1;
            double lowestSpeedAndTurningRatePenaltyFactor = 1;
            if (maneuver.isCleanBefore()) {
                speedPenaltyFactorBefore = getSpeedPenaltyFactor(currentNode, previousPathStats, intersectedWindRange,
                        maneuver, maneuver.getAverageSpeedWithBearingBefore());
            }
            if (maneuver.isCleanAfter()) {
                speedPenaltyFactorAfter = getSpeedPenaltyFactor(currentNode, previousPathStats, intersectedWindRange,
                        maneuver, maneuver.getAverageSpeedWithBearingAfter());
            }
            if (maneuver.isClean()) {
                lowestSpeedAndTurningRatePenaltyFactor = getLowestSpeedAndTurningRatePenaltyFactor(previousPathStats,
                        currentLevel, currentNode);
            }
            double finalPenaltyFactor = speedPenaltyFactorBefore * speedPenaltyFactorAfter
                    * lowestSpeedAndTurningRatePenaltyFactor;
            return finalPenaltyFactor;
        }
    }

    private double getSpeedPenaltyFactor(GraphNode currentNode, SailingStatistics previousPathStats,
            IntersectedWindRange intersectedWindRange, ManeuverForEstimation maneuver,
            SpeedWithBearing avgSpeedWithCourse) {
        double finalSpeedPenaltyFactor = 1;
        List<FineGrainedPointOfSail> bestSuitablePointOfSails = intersectedWindRange
                .getBestSuitablePointOfSails(currentNode, maneuver, avgSpeedWithCourse.getBearing());
        if (!bestSuitablePointOfSails.isEmpty()) {
            double maxSpeedPenaltyFactor = 0;
            for (FineGrainedPointOfSail pointOfSail : bestSuitablePointOfSails) {
                double speedPenaltyFactor = getSpeedPenaltyFactorForPointOfSail(previousPathStats, pointOfSail,
                        avgSpeedWithCourse.getKnots(), maneuver.getBoatClass());
                maxSpeedPenaltyFactor = Math.max(maxSpeedPenaltyFactor, speedPenaltyFactor);
            }
            finalSpeedPenaltyFactor = maxSpeedPenaltyFactor;
        }
        return finalSpeedPenaltyFactor;
    }

    protected double getLowestSpeedAndTurningRatePenaltyFactor(SailingStatistics averageStatistics,
            GraphLevel currentLevel, GraphNode currentNode) {
        return 1;
    }

    protected double getSpeedPenaltyFactorForPointOfSail(SailingStatistics averageStatistics,
            FineGrainedPointOfSail pointOfSail, double speedAtPointOfSail, BoatClass boatClass) {
        return 1;
    }

    public List<Triple<GraphLevel, GraphNode, Double>> getBestPath(GraphLevel lastLevel, GraphNode lastNode) {
        double probabilitiesSum = 0;
        BestPathsPerLevel bestPathsUntilLastLevel = bestPathsPerLevel.get(lastLevel);
        double lastNodeProbability = bestPathsUntilLastLevel.getNormalizedProbabilityToNodeFromStart(lastNode);
        if (preciseConfidence) {
            GraphLevel firstLevel = lastLevel;
            while (firstLevel.getPreviousLevel() != null) {
                firstLevel = firstLevel.getPreviousLevel();
            }
            BestPathsPerLevel bestPathsUntilFirstLevel = bestPathsPerLevel.get(firstLevel);
            if (!bestPathsUntilFirstLevel.isBackwardProbabilitiesComputed()) {
                computeBackwardProbabilities();
            }
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
                nodeConfidence = 2 * currentNodeInfo.getForwardProbability() * currentNodeInfo.getBackwardProbability()
                        / (currentLevelInfo.getForwardProbabilitiesSum()
                                + currentLevelInfo.getBackwardProbabilitiesSum());
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
                BestManeuverNodeInfo currentNodeInfo = bestPathsUntilCurrentLevel
                        .getBestPreviousNodeInfo(currentNode);
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
        windFixes = getWindFixesWithAveragedWindSpeed(windFixes);
        return windFixes;
    }

    private List<WindWithConfidence<Void>> getWindFixesWithAveragedWindSpeed(List<WindWithConfidence<Void>> windFixes) {
        if (windFixes.size() <= 1) {
            return windFixes;
        }
        double[] windSpeedsInKnots = new double[windFixes.size()];
        int i = 0;
        int zerosCount = 0;
        for (WindWithConfidence<Void> windFix : windFixes) {
            double windSpeedInKnots = windFix.getObject().getKnots();
            if (windSpeedInKnots > 0) {
                windSpeedsInKnots[i++] = windFix.getObject().getKnots();
            } else {
                zerosCount++;
            }
        }
        if (zerosCount == windSpeedsInKnots.length) {
            return windFixes;
        }
        if (zerosCount > 0) {
            windSpeedsInKnots = Arrays.copyOfRange(windSpeedsInKnots, 0, windSpeedsInKnots.length - zerosCount);
        }
        double avgWindSpeedInKnots = windSpeedsInKnots.length == 1 ? windSpeedsInKnots[0]
                : QuickSelect.median(windSpeedsInKnots);
        List<WindWithConfidence<Void>> result = new ArrayList<>();
        for (WindWithConfidence<Void> windFix : windFixes) {
            Wind wind = windFix.getObject();
            WindWithConfidence<Void> newWindFix = new WindWithConfidenceImpl<Void>(
                    new WindImpl(wind.getPosition(), wind.getTimePoint(),
                            new KnotSpeedWithBearingImpl(avgWindSpeedInKnots, wind.getBearing())),
                    windFix.getConfidence(), windFix.getRelativeTo(), avgWindSpeedInKnots > 0);
            result.add(newWindFix);
        }
        return result;
    }

    public Speed getWindSpeed(ManeuverForEstimation maneuver, Bearing windCourse) {
        return new KnotSpeedImpl(0.0);
    }

    public boolean isPreciseConfidence() {
        return preciseConfidence;
    }

    public void computeBackwardProbabilities() {
        GraphLevel currentLevel = lastLevel;
        BestPathsPerLevel bestPathsUntilLevel = bestPathsPerLevel.get(currentLevel);
        for (GraphNode currentNode : currentLevel.getLevelNodes()) {
            double probability = currentNode.getConfidence();
            BestManeuverNodeInfo currentNodeNodeInfo = bestPathsUntilLevel.getBestPreviousNodeInfo(currentNode);
            currentNodeNodeInfo.setBackwardProbability(probability);
        }
        GraphLevel nextLevel = currentLevel;
        while ((currentLevel = currentLevel.getPreviousLevel()) != null) {
            bestPathsUntilLevel = bestPathsPerLevel.get(currentLevel);
            BestPathsPerLevel bestPathsUntilNextLevel = bestPathsPerLevel.get(nextLevel);
            double secondsPassedSincePreviousManeuver = currentLevel.getManeuver().getManeuverTimePoint()
                    .until(nextLevel.getManeuver().getManeuverTimePoint()).asSeconds();
            for (GraphNode currentNode : currentLevel.getLevelNodes()) {
                double backwardProbability = 0;
                for (GraphNode nextNode : nextLevel.getLevelNodes()) {
                    BestManeuverNodeInfo nextNodeInfo = bestPathsUntilNextLevel.getBestPreviousNodeInfo(nextNode);
                    // TODO generate sailing statistics from backward? or throw sailings statistics away?
                    SailingStatistics nextPathStats = nextNodeInfo
                            .getPathSailingStatistics(nextLevel.getManeuver().getBoatClass());
                    IntersectedWindRange intersectedWindRangeUntilCurrentNode = nextNodeInfo.getWindRange()
                            .intersect(currentNode.getValidWindRange());
                    double transitionObservationMultipliedProbability = getTransitionObservationMultipliedProbability(
                            currentLevel, secondsPassedSincePreviousManeuver, currentNode, nextNode, nextPathStats,
                            intersectedWindRangeUntilCurrentNode);
                    backwardProbability += transitionObservationMultipliedProbability;
                }
                BestManeuverNodeInfo currentNodeInfo = bestPathsUntilLevel.getBestPreviousNodeInfo(currentNode);
                currentNodeInfo.setBackwardProbability(backwardProbability);
            }
            nextLevel = currentLevel;
        }
    }

}
