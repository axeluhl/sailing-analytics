package com.sap.sailing.windestimation.maneuvergraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.tracking.impl.WindWithConfidenceImpl;
import com.sap.sse.common.TimePoint;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSequenceGraph<T extends ManeuverNodesLevel<T>, R> {

    private T firstGraphLevel = null;
    private T lastGraphLevel = null;
    private final ManeuverNodesLevelFactory<T, R> maneuverNodesLevelFactory;

    public ManeuverSequenceGraph(Iterable<R> maneuverSequence,
            ManeuverNodesLevelFactory<T, R> maneuverNodesLevelFactory) {
        this.maneuverNodesLevelFactory = maneuverNodesLevelFactory;
        for (R maneuver : maneuverSequence) {
            appendManeuverAsGraphLevel(maneuver);
        }
    }

    public ManeuverSequenceGraph(ManeuverNodesLevelFactory<T, R> maneuverNodesLevelFactory) {
        this.maneuverNodesLevelFactory = maneuverNodesLevelFactory;
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
                if (currentNode.getCoarseGrainedPointOfSail().getLegType() == LegType.UPWIND) {
                    SpeedWithBearingWithConfidence<Void> speedWithTwaIfTack = getSpeedWithTwaIfTack(currentLevel);
                    SpeedWithBearing windSpeedWithBearing = new KnotSpeedWithBearingImpl(
                            speedWithTwaIfTack.getObject().getKnots(), currentLevel.getManeuver()
                                    .getCurveWithUnstableCourseAndSpeed().getMiddleCourse().reverse());
                    WindImpl wind = new WindImpl(currentLevel.getManeuver().getPosition(),
                            currentLevel.getManeuver().getTimePoint(), windSpeedWithBearing);
                    WindWithConfidenceImpl<TimePoint> windWithConfidence = new WindWithConfidenceImpl<TimePoint>(wind,
                            speedWithTwaIfTack.getConfidence() * baseConfidence,
                            currentLevel.getManeuver().getTimePoint(), true);
                    windTrack.add(windWithConfidence);
                } else if (currentNode.getCoarseGrainedPointOfSail().getLegType() == LegType.DOWNWIND) {
                    SpeedWithBearingWithConfidence<Void> speedWithTwaIfJibe = getSpeedWithTwaIfJibe(currentLevel);
                    SpeedWithBearing windSpeedWithBearing = new KnotSpeedWithBearingImpl(
                            speedWithTwaIfJibe.getObject().getKnots(),
                            currentLevel.getManeuver().getCurveWithUnstableCourseAndSpeed().getMiddleCourse());
                    WindImpl wind = new WindImpl(currentLevel.getManeuver().getPosition(),
                            currentLevel.getManeuver().getTimePoint(), windSpeedWithBearing);
                    WindWithConfidenceImpl<TimePoint> windWithConfidence = new WindWithConfidenceImpl<TimePoint>(wind,
                            speedWithTwaIfJibe.getConfidence() * baseConfidence,
                            currentLevel.getManeuver().getTimePoint(), true);
                    windTrack.add(windWithConfidence);
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
                    SpeedWithBearingWithConfidence<Void> windSpeedWithTwa = getWindSpeedWithTwa(
                            pointOfSailForWindSpeedDetermination, boatSpeedForWindSpeedDetermination);
                    double twa = Math.abs(windSpeedWithTwa.getObject().getBearing().getDegrees());
                    if (pointOfSailForWindSpeedDetermination.getTack() == Tack.PORT) {
                        twa = 360 - twa;
                    }
                    FineGrainedPointOfSail targetPointOfSail = pointOfSailForWindSpeedDetermination
                            .getNextPointOfSail(twa - pointOfSailForWindSpeedDetermination.getTwa());
                    double windCourseInDegrees;
                    if (targetPointOfSail == pointOfSailForWindSpeedDetermination) {
                        windCourseInDegrees = currentLevel.getWindCourseInDegrees(twa);
                    } else {
                        windCourseInDegrees = currentLevel.getWindCourseInDegrees(pointOfSailForWindSpeedDetermination);
                    }
                    SpeedWithBearing windSpeedWithCourse = new KnotSpeedWithBearingImpl(
                            windSpeedWithTwa.getObject().getKnots(), new DegreeBearingImpl(windCourseInDegrees));
                    WindImpl wind = new WindImpl(currentLevel.getManeuver().getPosition(),
                            currentLevel.getManeuver().getTimePoint(), windSpeedWithCourse);
                    WindWithConfidenceImpl<TimePoint> windWithConfidence = new WindWithConfidenceImpl<TimePoint>(wind,
                            baseConfidence, currentLevel.getManeuver().getTimePoint(), true);
                    windTrack.add(windWithConfidence);
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
                double windCourseInDegrees = currentLevel.getWindCourseInDegrees(pointOfSailBeforeManeuver);
                SpeedWithBearing windSpeedWithCourse = new KnotSpeedWithBearingImpl(0,
                        new DegreeBearingImpl(windCourseInDegrees));
                WindImpl wind = new WindImpl(currentLevel.getManeuver().getPosition(),
                        currentLevel.getManeuver().getTimePoint(), windSpeedWithCourse);
                WindWithConfidenceImpl<TimePoint> windWithConfidence = new WindWithConfidenceImpl<TimePoint>(wind,
                        baseConfidence, currentLevel.getManeuver().getTimePoint(), false);
                windTrack.add(windWithConfidence);
            }
            currentNode = currentLevel.getBestPreviousNode(currentNode);
            currentLevel = currentLevel.getPreviousLevel();
        }
        return windTrack;
    }

    private SpeedWithBearingWithConfidence<Void> getWindSpeedWithTwa(
            FineGrainedPointOfSail pointOfSailForWindSpeedDetermination, Speed boatSpeedForWindSpeedDetermination) {
        // TODO Auto-generated method stub
        return null;
    }

    private SpeedWithBearingWithConfidence<Void> getSpeedWithTwaIfJibe(T currentLevel) {
        // TODO Auto-generated method stub
        return null;
    }

    private SpeedWithBearingWithConfidence<Void> getSpeedWithTwaIfTack(T currentLevel) {
        // TODO Auto-generated method stub
        return null;
    }

    public T getFirstGraphLevel() {
        return firstGraphLevel;
    }

    public T getLastGraphLevel() {
        return lastGraphLevel;
    }

}
