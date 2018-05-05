package com.sap.sailing.windestimation.maneuvergraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.tracking.impl.WindWithConfidenceImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSequenceGraph<T extends ManeuverNodesLevel<T>, R> {

    private T firstGraphLevel = null;
    private T lastGraphLevel = null;
    private final ManeuverNodesLevelFactory<T, R> maneuverNodesLevelFactory;
    private final PolarDataService polarService;

    public ManeuverSequenceGraph(Iterable<R> maneuverSequence,
            ManeuverNodesLevelFactory<T, R> maneuverNodesLevelFactory, PolarDataService polarService) {
        this(maneuverNodesLevelFactory, polarService);
        for (R maneuver : maneuverSequence) {
            appendManeuverAsGraphLevel(maneuver);
        }
    }

    public ManeuverSequenceGraph(ManeuverNodesLevelFactory<T, R> maneuverNodesLevelFactory,
            PolarDataService polarService) {
        this.maneuverNodesLevelFactory = maneuverNodesLevelFactory;
        this.polarService = polarService;
    }

    protected void appendManeuverAsGraphLevel(R nodeLevelReference) {
        T newManeuverNodesLevel = maneuverNodesLevelFactory.createNewManeuverNodesLevel(nodeLevelReference);
        if (firstGraphLevel == null) {
            firstGraphLevel = newManeuverNodesLevel;
            lastGraphLevel = newManeuverNodesLevel;

        } else {
            lastGraphLevel.appendNextManeuverNodesLevel(newManeuverNodesLevel);
            lastGraphLevel = newManeuverNodesLevel;
        }
        newManeuverNodesLevel.computeProbabilitiesFromPreviousLevelToThisLevel();
        newManeuverNodesLevel.computeBestPathsToThisLevel();
    }

    public void recomputePossiblePathsWithDistances() {
        T currentLevel = firstGraphLevel;
        while (currentLevel != null) {
            currentLevel.computeProbabilitiesFromPreviousLevelToThisLevel();
            currentLevel.computeBestPathsToThisLevel();
            currentLevel = currentLevel.getNextLevel();
        }
    }

    public List<WindWithConfidence<TimePoint>> estimateWindTrack() {
        List<WindWithConfidence<TimePoint>> windTrack = Collections.emptyList();
        T lastGraphLevel = this.lastGraphLevel;
        if (lastGraphLevel != null) {
            double[] probabilitiesOfBestPathToCoarseGrainedPointOfSail = new double[CoarseGrainedPointOfSail
                    .values().length];
            double maxProbability = 0;
            FineGrainedPointOfSail pointOfSailWithMaxProbability = null;
            for (FineGrainedPointOfSail pointOfSail : FineGrainedPointOfSail.values()) {
                double probability = lastGraphLevel.getProbabilityOfBestPathToNodeFromStart(pointOfSail);
                if (probability > probabilitiesOfBestPathToCoarseGrainedPointOfSail[pointOfSail
                        .getCoarseGrainedPointOfSail().ordinal()]) {
                    probabilitiesOfBestPathToCoarseGrainedPointOfSail[pointOfSail.getCoarseGrainedPointOfSail()
                            .ordinal()] = probability;
                    if (maxProbability < probability) {
                        maxProbability = probability;
                        pointOfSailWithMaxProbability = pointOfSail;
                    }
                }
            }
            double sumOfprobabilitiesOfBestPathToCoarseGrainedPointOfSail = 0;
            for (CoarseGrainedPointOfSail coarseGrainedPointOfSail : CoarseGrainedPointOfSail.values()) {
                sumOfprobabilitiesOfBestPathToCoarseGrainedPointOfSail = probabilitiesOfBestPathToCoarseGrainedPointOfSail[coarseGrainedPointOfSail
                        .ordinal()];
            }
            double bestPathConfidence = maxProbability / sumOfprobabilitiesOfBestPathToCoarseGrainedPointOfSail;
            windTrack = getWindTrackWithLastNodeOfLastLevelConsideringMiddleCoursesOfCleanTacksAndJibes(
                    pointOfSailWithMaxProbability, lastGraphLevel, bestPathConfidence);
            if (windTrack.isEmpty()) {
                windTrack = getWindTrackWithLastNodeOfLastLevelConsideringStableCourses(pointOfSailWithMaxProbability,
                        lastGraphLevel, bestPathConfidence * 0.5);
            }
            if (windTrack.isEmpty()) {
                windTrack = getWindTrackWithLastNodeOfLastLevelConsideringPointOfSailsWithoutWindSpeed(
                        pointOfSailWithMaxProbability, lastGraphLevel, bestPathConfidence * 0.5);
            }
        }
        return windTrack;
    }

    private List<WindWithConfidence<TimePoint>> getWindTrackWithLastNodeOfLastLevelConsideringMiddleCoursesOfCleanTacksAndJibes(
            FineGrainedPointOfSail lastNode, T lastLevel, double baseConfidence) {
        FineGrainedPointOfSail currentNode = lastNode;
        T currentLevel = lastLevel;
        List<WindWithConfidence<TimePoint>> windTrack = new ArrayList<>();
        while (currentLevel.getPreviousLevel() != null) {
            FineGrainedPointOfSail previousNode = currentNode.getNextPointOfSail(
                    currentLevel.getManeuver().getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees() * -1);
            if (previousNode.getCoarseGrainedPointOfSail().getLegType() == currentNode.getCoarseGrainedPointOfSail()
                    .getLegType() && previousNode.getTack() != currentNode.getTack()
                    && Math.abs(currentLevel.getManeuver().getMainCurve().getDirectionChangeInDegrees()) < 110
                    && isCleanManeuver(currentLevel)) {
                ManeuverType tackOrJibe;
                if (currentNode.getCoarseGrainedPointOfSail().getLegType() == LegType.UPWIND) {
                    tackOrJibe = ManeuverType.TACK;
                } else if (currentNode.getCoarseGrainedPointOfSail().getLegType() == LegType.DOWNWIND) {
                    tackOrJibe = ManeuverType.JIBE;
                } else {
                    tackOrJibe = null;
                }
                if (tackOrJibe != null) {
                    WindWithConfidenceImpl<TimePoint> windWithConfidence = getWindWithConfidenceIfTackOrJibe(
                            currentLevel, baseConfidence, tackOrJibe);
                    if (windWithConfidence != null) {
                        windTrack.add(windWithConfidence);
                    }
                }
            }
            currentNode = currentLevel.getBestPreviousNode(currentNode);
            currentLevel = currentLevel.getPreviousLevel();
        }
        return windTrack;
    }

    private boolean isCleanManeuver(T maneuverNodesLevel) {
        boolean maneuverBeginningClean = isManeuverBeginningClean(maneuverNodesLevel);
        boolean maneuverEndClean = isManeuverEndClean(maneuverNodesLevel);
        if (maneuverNodesLevel.getManeuver().isMarkPassing() || !maneuverBeginningClean || !maneuverEndClean) {
            return false;
        }
        return true;
    }

    private boolean isManeuverEndClean(T maneuverNodesLevel) {
        // TODO implement duration to next fix
        // consider mark passing, duration to next fix from both maneuver boundary sides
        CompleteManeuverCurveWithEstimationData maneuver = maneuverNodesLevel.getManeuver();
        CompleteManeuverCurveWithEstimationData nextManeuver = maneuverNodesLevel.getNextLevel() == null ? null
                : maneuverNodesLevel.getNextLevel().getManeuver();
        double secondsToNextManeuver = maneuver.getCurveWithUnstableCourseAndSpeed()
                .getDurationFromManeuverEndToNextManeuverStart().asSeconds();
        int gpsFixesCountToNextManeuver = maneuver.getCurveWithUnstableCourseAndSpeed()
                .getGpsFixesCountFromManeuverEndToNextManeuverStart();
        if (maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getKnots() > 1
                && (secondsToNextManeuver >= 4 && gpsFixesCountToNextManeuver / secondsToNextManeuver >= 0.125
                        || nextManeuver != null
                                && Math.abs(nextManeuver.getMainCurve().getDirectionChangeInDegrees()) < Math
                                        .abs(maneuver.getMainCurve().getDirectionChangeInDegrees()) * 0.3)) {
            return true;
        }
        return false;
    }

    private boolean isManeuverBeginningClean(T maneuverNodesLevel) {
        // TODO implement duration to next fix
        // consider mark passing, duration to next fix from both maneuver boundary sides
        CompleteManeuverCurveWithEstimationData maneuver = maneuverNodesLevel.getManeuver();
        CompleteManeuverCurveWithEstimationData previousManeuver = maneuverNodesLevel.getPreviousLevel() == null ? null
                : maneuverNodesLevel.getPreviousLevel().getManeuver();
        double secondsToPreviousManeuver = maneuver.getCurveWithUnstableCourseAndSpeed()
                .getDurationFromPreviousManeuverEndToManeuverStart().asSeconds();
        int gpsFixesCountToPreviousManeuver = maneuver.getCurveWithUnstableCourseAndSpeed()
                .getGpsFixesCountFromPreviousManeuverEndToManeuverStart();
        if (maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore().getKnots() > 1
                && (secondsToPreviousManeuver >= 4
                        && gpsFixesCountToPreviousManeuver / secondsToPreviousManeuver >= 0.125
                        || previousManeuver != null
                                && Math.abs(previousManeuver.getMainCurve().getDirectionChangeInDegrees()) < Math
                                        .abs(maneuver.getMainCurve().getDirectionChangeInDegrees()) * 0.3)) {
            return true;
        }
        return false;
    }

    private List<WindWithConfidence<TimePoint>> getWindTrackWithLastNodeOfLastLevelConsideringStableCourses(
            FineGrainedPointOfSail lastNode, T lastLevel, double baseConfidence) {
        FineGrainedPointOfSail currentNode = lastNode;
        T currentLevel = lastLevel;
        List<WindWithConfidence<TimePoint>> windTrack = new ArrayList<>();
        while (currentLevel != null) {
            FineGrainedPointOfSail pointOfSailBeforeManeuver = currentNode.getNextPointOfSail(
                    currentLevel.getManeuver().getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees() * -1);
            if (pointOfSailBeforeManeuver.getCoarseGrainedPointOfSail() != currentNode.getCoarseGrainedPointOfSail()) {
                FineGrainedPointOfSail pointOfSailForWindSpeedDetermination = null;
                Speed boatSpeedForWindSpeedDetermination = null;
                if (isManeuverEndClean(currentLevel)) {
                    pointOfSailForWindSpeedDetermination = currentNode;
                    boatSpeedForWindSpeedDetermination = currentLevel.getManeuver().getCurveWithUnstableCourseAndSpeed()
                            .getSpeedWithBearingAfter();
                } else if (isManeuverBeginningClean(currentLevel)) {
                    pointOfSailForWindSpeedDetermination = pointOfSailBeforeManeuver;
                    boatSpeedForWindSpeedDetermination = currentLevel.getManeuver().getCurveWithUnstableCourseAndSpeed()
                            .getSpeedWithBearingBefore();
                }
                if (pointOfSailForWindSpeedDetermination != null) {
                    WindWithConfidenceImpl<TimePoint> windWithConfidence = getWindWithConfidence(
                            pointOfSailForWindSpeedDetermination, boatSpeedForWindSpeedDetermination, currentLevel,
                            baseConfidence);
                    if (windWithConfidence != null) {
                        windTrack.add(windWithConfidence);
                    }
                }
            }
            currentNode = currentLevel.getBestPreviousNode(currentNode);
            currentLevel = currentLevel.getPreviousLevel();
        }
        return windTrack;
    }

    private List<WindWithConfidence<TimePoint>> getWindTrackWithLastNodeOfLastLevelConsideringPointOfSailsWithoutWindSpeed(
            FineGrainedPointOfSail lastNode, T lastLevel, double baseConfidence) {
        FineGrainedPointOfSail currentNode = lastNode;
        T currentLevel = lastLevel;
        List<WindWithConfidence<TimePoint>> windTrack = new ArrayList<>();
        while (currentLevel != null) {
            FineGrainedPointOfSail pointOfSailBeforeManeuver = currentNode.getNextPointOfSail(
                    currentLevel.getManeuver().getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees() * -1);
            if (pointOfSailBeforeManeuver.getCoarseGrainedPointOfSail() != currentNode.getCoarseGrainedPointOfSail()) {
                WindWithConfidenceImpl<TimePoint> windWithConfidence = getWindWithConfidenceWithoutSpeed(
                        pointOfSailBeforeManeuver, currentLevel, baseConfidence);
                windTrack.add(windWithConfidence);
            }
            currentNode = currentLevel.getBestPreviousNode(currentNode);
            currentLevel = currentLevel.getPreviousLevel();
        }
        return windTrack;
    }

    private WindWithConfidenceImpl<TimePoint> getWindWithConfidenceWithoutSpeed(FineGrainedPointOfSail pointOfSail,
            T currentLevel, double baseConfidence) {
        double windCourseInDegrees = currentLevel.getWindCourseInDegrees(pointOfSail);
        WindWithConfidenceImpl<TimePoint> windWithConfidence = constructWindWithConfidence(windCourseInDegrees, 0,
                currentLevel, baseConfidence);
        return windWithConfidence;
    }

    private WindWithConfidenceImpl<TimePoint> getWindWithConfidence(
            FineGrainedPointOfSail pointOfSailForWindSpeedDetermination, Speed boatSpeedForWindSpeedDetermination,
            T currentLevel, double baseConfidence) {
        Set<SpeedWithBearingWithConfidence<Void>> trueWindSpeedAndTwaCandidates = polarService
                .getAverageTrueWindSpeedAndAngleCandidates(currentLevel.getBoatClass(),
                        boatSpeedForWindSpeedDetermination, pointOfSailForWindSpeedDetermination.getLegType(),
                        pointOfSailForWindSpeedDetermination.getTack());
        SpeedWithBearingWithConfidence<Void> bestWindSpeedAndTwa = null;
        double minTwaDeviation = Double.MAX_VALUE;
        double bestTwa = 0;
        for (SpeedWithBearingWithConfidence<Void> trueWindSpeedAndTwa : trueWindSpeedAndTwaCandidates) {
            double twa = Math.abs(trueWindSpeedAndTwa.getObject().getBearing().getDegrees());
            if (pointOfSailForWindSpeedDetermination.getTwa() > 180) {
                twa = 360 - twa;
            }
            double twaDeviation = Math.abs(pointOfSailForWindSpeedDetermination.getTwa() - twa);
            if (minTwaDeviation > twaDeviation) {
                minTwaDeviation = twaDeviation;
                bestWindSpeedAndTwa = trueWindSpeedAndTwa;
                bestTwa = twa;
            }
        }
        WindWithConfidenceImpl<TimePoint> windWithConfidence = null;
        if (bestWindSpeedAndTwa != null) {
            FineGrainedPointOfSail targetPointOfSail = pointOfSailForWindSpeedDetermination
                    .getNextPointOfSail(bestTwa - pointOfSailForWindSpeedDetermination.getTwa());
            double windCourseInDegrees;
            if (targetPointOfSail == pointOfSailForWindSpeedDetermination) {
                windCourseInDegrees = currentLevel.getWindCourseInDegrees(bestTwa);
            } else {
                windCourseInDegrees = currentLevel.getWindCourseInDegrees(pointOfSailForWindSpeedDetermination);
            }
            windWithConfidence = constructWindWithConfidence(windCourseInDegrees,
                    bestWindSpeedAndTwa.getObject().getKnots(), currentLevel, baseConfidence);
        }
        return windWithConfidence;
    }

    private WindWithConfidenceImpl<TimePoint> getWindWithConfidenceIfTackOrJibe(T currentLevel, double baseConfidence,
            ManeuverType tackOrJibe) {
        assert tackOrJibe == ManeuverType.TACK || tackOrJibe == ManeuverType.JIBE;
        Speed boatSpeed = new KnotSpeedImpl((currentLevel.getManeuver().getCurveWithUnstableCourseAndSpeed()
                .getSpeedWithBearingBefore().getKnots()
                + currentLevel.getManeuver().getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getKnots())
                / 2);
        double directionChangeInDegrees = currentLevel.getManeuver().getCurveWithUnstableCourseAndSpeed()
                .getDirectionChangeInDegrees();
        Pair<Double, SpeedWithBearingWithConfidence<Void>> tackOrJibeLikelihoodWithTwaTws = polarService
                .getManeuverLikelihoodAndTwsTwa(currentLevel.getBoatClass(), boatSpeed, directionChangeInDegrees,
                        tackOrJibe);
        WindWithConfidenceImpl<TimePoint> windWithConfidence = null;
        if (tackOrJibeLikelihoodWithTwaTws.getA() != null) {
            SpeedWithBearing windSpeedAndTwaIfTackOrJibe = tackOrJibeLikelihoodWithTwaTws.getB().getObject();
            SpeedWithBearing windSpeedWithCourse = new KnotSpeedWithBearingImpl(windSpeedAndTwaIfTackOrJibe.getKnots(),
                    currentLevel.getManeuver().getCurveWithUnstableCourseAndSpeed().getMiddleCourse().reverse());
            windWithConfidence = constructWindWithConfidence(windSpeedWithCourse, currentLevel,
                    tackOrJibeLikelihoodWithTwaTws.getA() * baseConfidence);

        }
        return windWithConfidence;
    }

    private WindWithConfidenceImpl<TimePoint> constructWindWithConfidence(SpeedWithBearing windSpeedWithCourse,
            T currentLevel, double confidence) {
        WindImpl wind = new WindImpl(currentLevel.getManeuver().getPosition(),
                currentLevel.getManeuver().getTimePoint(), windSpeedWithCourse);
        WindWithConfidenceImpl<TimePoint> windWithConfidence = new WindWithConfidenceImpl<TimePoint>(wind, confidence,
                currentLevel.getManeuver().getTimePoint(), windSpeedWithCourse.getKnots() > 0);
        return windWithConfidence;
    }

    private WindWithConfidenceImpl<TimePoint> constructWindWithConfidence(double windCourseDeg, double windSpeedKnots,
            T currentLevel, double confidence) {
        DegreeBearingImpl windCourse = new DegreeBearingImpl(windCourseDeg);
        SpeedWithBearing windSpeedWithCourse = new KnotSpeedWithBearingImpl(windSpeedKnots, windCourse);
        return constructWindWithConfidence(windSpeedWithCourse, currentLevel, confidence);
    }

    public T getFirstGraphLevel() {
        return firstGraphLevel;
    }

    public T getLastGraphLevel() {
        return lastGraphLevel;
    }

    public PolarDataService getPolarService() {
        return polarService;
    }

}
