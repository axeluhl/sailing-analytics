package com.sap.sailing.windestimation.maneuvergraph.maneuvernode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.tracking.impl.WindWithConfidenceImpl;
import com.sap.sailing.windestimation.data.FineGrainedManeuverType;
import com.sap.sailing.windestimation.data.FineGrainedPointOfSail;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.polarsfitting.SailingStatistics;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Speed;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.DegreeBearingImpl;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverNodeBestPathsCalculator {

    protected static final double INTERVAL_FOR_WIND_PATH_DEVIATION_ANALYSIS_IN_SECONDS = 30 * 60;
    protected static final double MAX_ABS_WIND_COURSE_DEVIATION_TOLERANCE_WITHIN_ANALYSIS_INTERVAL_IN_DEGREES = 45;

    private ManeuverNodeGraphLevel lastLevel;

    private Map<ManeuverNodeGraphLevel, ManeuverNodeBestPathsPerLevel> bestPathsPerLevel;

    public ManeuverNodeBestPathsCalculator() {
    }

    public void computeBestPathsFromScratch() {
        ManeuverNodeGraphLevel previousLevel = lastLevel;
        if (previousLevel != null) {
            // find first level
            while (previousLevel.getPreviousLevel() != null) {
                previousLevel = previousLevel.getPreviousLevel();
            }
            computeBestPathsFromScratch(previousLevel);
        }
    }

    public void computeBestPathsFromScratch(ManeuverNodeGraphLevel firstLevel) {
        resetState();
        ManeuverNodeGraphLevel currentLevel = firstLevel;
        do {
            computeBestPathsToNextLevel(currentLevel);
        } while ((currentLevel = currentLevel.getNextLevel()) != null);
    }

    public void recomputeBestPathsFromLevel(ManeuverNodeGraphLevel fromLevel) {
        List<ManeuverNodeGraphLevel> levelsToKeep = new LinkedList<>();
        ManeuverNodeGraphLevel currentLevel = fromLevel.getPreviousLevel();
        while (currentLevel != null) {
            levelsToKeep.add(currentLevel);
            currentLevel = currentLevel.getPreviousLevel();
        }
        Iterator<Entry<ManeuverNodeGraphLevel, ManeuverNodeBestPathsPerLevel>> iterator = bestPathsPerLevel.entrySet()
                .iterator();
        while (iterator.hasNext()) {
            Entry<ManeuverNodeGraphLevel, ManeuverNodeBestPathsPerLevel> entry = iterator.next();
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

    public void computeBestPathsToNextLevel(ManeuverNodeGraphLevel nextLevel) {
        ManeuverNodeGraphLevel previousLevel = nextLevel.getPreviousLevel();
        if (previousLevel != lastLevel) {
            throw new IllegalArgumentException(
                    "The previous level of next level does not match with the last level processed by this calculator");
        }
        ManeuverNodeGraphLevel currentLevel = nextLevel;
        if (previousLevel == null) {
            bestPathsPerLevel = new HashMap<>();
            ManeuverNodeBestPathsPerLevel bestPathsUntilLevel = new ManeuverNodeBestPathsPerLevel(currentLevel);
            for (ManeuverNode currentNode : currentLevel.getLevelNodes()) {
                double probability = currentNode.getConfidence();
                IntersectedWindRange intersectedWindRange = currentNode.getValidWindRange().toIntersected();
                bestPathsUntilLevel.addBestPreviousNodeInfo(currentNode, null, probability, intersectedWindRange, null);
            }
            bestPathsPerLevel.put(currentLevel, bestPathsUntilLevel);
        } else {
            ManeuverNodeBestPathsPerLevel bestPathsUntilPreviousLevel = bestPathsPerLevel.get(previousLevel);
            double secondsPassedSincePreviousManeuver = currentLevel.getPreviousLevel().getManeuver()
                    .getManeuverTimePoint().until(currentLevel.getManeuver().getManeuverTimePoint()).asSeconds();
            ManeuverNodeBestPathsPerLevel bestPathsUntilLevel = new ManeuverNodeBestPathsPerLevel(currentLevel);
            for (ManeuverNode currentNode : currentLevel.getLevelNodes()) {
                double maxProbability = 0;
                ManeuverNode bestPreviousNode = null;
                IntersectedWindRange bestIntersectedWindRangeUntilCurrentNode = null;
                SailingStatistics bestPreviousPathStats = null;
                for (ManeuverNode previousNode : previousLevel.getLevelNodes()) {
                    BestManeuverNodeInfo bestPreviousNodeInfo = bestPathsUntilPreviousLevel
                            .getBestPreviousNodeInfo(previousNode);
                    SailingStatistics previousPathStats = bestPreviousNodeInfo
                            .getPathSailingStatistics(previousLevel.getManeuver().getBoatClass());
                    IntersectedWindRange intersectedWindRangeUntilCurrentNode = bestPreviousNodeInfo.getWindRange()
                            .intersect(currentNode.getValidWindRange());
                    double probability = bestPathsUntilPreviousLevel
                            .getNormalizedProbabilityToNodeFromStart(previousNode)
                            * currentNode.getConfidence()
                            * getPenaltyFactorForTransitionConsideringWindRangeWithinBestPath(
                                    intersectedWindRangeUntilCurrentNode, secondsPassedSincePreviousManeuver)
                            * getPenaltyFactorForTransitionConsideringPreviousPathStats(currentLevel, currentNode,
                                    previousPathStats, intersectedWindRangeUntilCurrentNode);
                    if (probability > maxProbability) {
                        maxProbability = probability;
                        bestPreviousNode = previousNode;
                        bestIntersectedWindRangeUntilCurrentNode = intersectedWindRangeUntilCurrentNode;
                        bestPreviousPathStats = previousPathStats;
                    }
                }
                bestPathsUntilLevel.addBestPreviousNodeInfo(currentNode, bestPreviousNode, maxProbability,
                        bestIntersectedWindRangeUntilCurrentNode, bestPreviousPathStats);
            }
            bestPathsPerLevel.put(currentLevel, bestPathsUntilLevel);
        }
        this.lastLevel = currentLevel;
    }

    protected double getPenaltyFactorForTransitionConsideringWindRangeWithinBestPath(
            IntersectedWindRange intersectedWindRangeUntilCurrentNode, double secondsPassedSincePreviousWindRange) {
        double violationRange = intersectedWindRangeUntilCurrentNode.getViolationRange();
        double penaltyFactor;
        if (violationRange == 0) {
            penaltyFactor = 1.0;
        } else {
            violationRange -= secondsPassedSincePreviousWindRange / 3600
                    * MAX_ABS_WIND_COURSE_DEVIATION_TOLERANCE_WITHIN_ANALYSIS_INTERVAL_IN_DEGREES;
            if (violationRange <= 0) {
                penaltyFactor = 1 / (1
                        + (MAX_ABS_WIND_COURSE_DEVIATION_TOLERANCE_WITHIN_ANALYSIS_INTERVAL_IN_DEGREES + violationRange)
                                * 0.01);
            } else {
                penaltyFactor = 1 / (4 + Math.pow((violationRange) / 5, 2));
            }
        }
        return penaltyFactor;
    }

    protected double getPenaltyFactorForTransitionConsideringPreviousPathStats(ManeuverNodeGraphLevel currentLevel,
            ManeuverNode currentNode, SailingStatistics previousPathStats, IntersectedWindRange intersectedWindRange) {
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
            return speedPenaltyFactorBefore * speedPenaltyFactorAfter * lowestSpeedAndTurningRatePenaltyFactor;
        }
    }

    private double getSpeedPenaltyFactor(ManeuverNode currentNode, SailingStatistics previousPathStats,
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
            ManeuverNodeGraphLevel currentLevel, ManeuverNode currentNode) {
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
        case JIBE:
        case OTHER:
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

    public List<Pair<ManeuverNodeGraphLevel, ManeuverNode>> getBestPath(ManeuverNodeGraphLevel lastLevel,
            ManeuverNode lastNode) {
        List<Pair<ManeuverNodeGraphLevel, ManeuverNode>> result = new LinkedList<>();
        ManeuverNode currentNode = lastNode;
        ManeuverNodeGraphLevel currentLevel = lastLevel;
        while (currentLevel != null) {
            Pair<ManeuverNodeGraphLevel, ManeuverNode> entry = new Pair<>(currentLevel, currentNode);
            result.add(0, entry);
            currentNode = bestPathsPerLevel.get(currentLevel).getBestPreviousNodeInfo(currentNode)
                    .getBestPreviousNode();
            currentLevel = currentLevel.getPreviousLevel();
        }
        return result;
    }

    public List<Pair<ManeuverNodeGraphLevel, ManeuverNode>> getBestPath(ManeuverNodeGraphLevel lastLevel) {
        ManeuverNodeBestPathsPerLevel bestPathsUntilLevel = bestPathsPerLevel.get(lastLevel);
        double maxProbability = 0;
        ManeuverNode bestLastNode = null;
        for (ManeuverNode lastNode : lastLevel.getLevelNodes()) {
            double probability = bestPathsUntilLevel.getNormalizedProbabilityToNodeFromStart(lastNode);
            if (maxProbability < probability) {
                maxProbability = probability;
                bestLastNode = lastNode;
            }
        }
        return getBestPath(lastLevel, bestLastNode);
    }

    public double getConfidenceOfBestPath(List<Pair<ManeuverNodeGraphLevel, ManeuverNode>> bestPath) {
        Pair<ManeuverNodeGraphLevel, ManeuverNode> lastLevelWithNode = bestPath.get(bestPath.size() - 1);
        ManeuverNode lastNode = lastLevelWithNode.getB();
        ManeuverNodeBestPathsPerLevel bestPathsUntilLastLevel = bestPathsPerLevel.get(lastLevelWithNode.getA());
        double sumOfprobabilitiesOfBestPathToCoarseGrainedPointOfSail = 0;
        for (ManeuverNode node : lastLevelWithNode.getA().getLevelNodes()) {
            double probability = bestPathsUntilLastLevel.getNormalizedProbabilityToNodeFromStart(node);
            sumOfprobabilitiesOfBestPathToCoarseGrainedPointOfSail += probability;
        }
        double lastNodeProbability = bestPathsUntilLastLevel.getNormalizedProbabilityToNodeFromStart(lastNode);
        double bestPathConfidence = lastNodeProbability / sumOfprobabilitiesOfBestPathToCoarseGrainedPointOfSail;
        return bestPathConfidence;
    }

    public List<WindWithConfidence<ManeuverForEstimation>> getWindTrack(
            List<Pair<ManeuverNodeGraphLevel, ManeuverNode>> bestPath) {
        List<WindWithConfidence<ManeuverForEstimation>> windFixes = new ArrayList<>();
        double baseConfidence = getConfidenceOfBestPath(bestPath);
        if (!bestPath.isEmpty()) {
            IntersectedWindRange globalWindRange = null;
            for (ListIterator<Pair<ManeuverNodeGraphLevel, ManeuverNode>> iterator = bestPath
                    .listIterator(bestPath.size()); iterator.hasPrevious();) {
                Pair<ManeuverNodeGraphLevel, ManeuverNode> entry = iterator.previous();
                ManeuverNodeGraphLevel currentLevel = entry.getA();
                ManeuverNode currentNode = entry.getB();
                ManeuverNodeBestPathsPerLevel bestPathsUntilCurrentLevel = bestPathsPerLevel.get(currentLevel);
                BestManeuverNodeInfo bestPreviousNodeInfo = bestPathsUntilCurrentLevel
                        .getBestPreviousNodeInfo(currentNode);
                globalWindRange = globalWindRange == null ? bestPreviousNodeInfo.getWindRange()
                        : globalWindRange.intersect(bestPreviousNodeInfo.getWindRange());
                if (!globalWindRange.isViolation() && globalWindRange.getAngleTowardStarboard() <= 40) {
                    DegreeBearingImpl windCourse = new DegreeBearingImpl(globalWindRange.getAvgWindCourse());
                    Speed avgWindSpeed = getAvgWindSpeed(currentLevel.getManeuver(), windCourse);
                    Wind wind = new WindImpl(currentLevel.getManeuver().getManeuverPosition(),
                            currentLevel.getManeuver().getManeuverTimePoint(),
                            new KnotSpeedWithBearingImpl(avgWindSpeed.getKnots(), windCourse));
                    windFixes.add(new WindWithConfidenceImpl<ManeuverForEstimation>(wind, baseConfidence,
                            currentLevel.getManeuver(), avgWindSpeed.getKnots() > 0));
                }
            }
        }
        return windFixes;
    }

    public Speed getAvgWindSpeed(ManeuverForEstimation maneuver, Bearing windCourse) {
        return new KnotSpeedImpl(0.0);
    }

}
