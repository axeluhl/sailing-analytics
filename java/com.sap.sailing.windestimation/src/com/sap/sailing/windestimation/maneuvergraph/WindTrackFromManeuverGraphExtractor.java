package com.sap.sailing.windestimation.maneuvergraph;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.tracking.impl.WindWithConfidenceImpl;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.DegreeBearingImpl;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <T>
 */
public class WindTrackFromManeuverGraphExtractor {

    private final PolarDataService polarService;

    public WindTrackFromManeuverGraphExtractor(PolarDataService polarService) {
        this.polarService = polarService;
    }

    public List<WindWithConfidence<TimePoint>> getWindTrack(List<Pair<GraphLevel, FineGrainedPointOfSail>> bestPath,
            double bestPathConfidence) {

        List<WindWithConfidence<TimePoint>> windTrack = getWindTrackWithLastNodeOfLastLevelConsideringMiddleCoursesOfCleanTacksAndJibes(
                bestPath, bestPathConfidence);
        // TODO use the unused methods for estimation based on curve fitting with actual and target polars
        // if (windTrack.isEmpty()) {
        // windTrack = getWindTrackWithLastNodeOfLastLevelConsideringStableCourses(bestPath, bestPathConfidence * 0.5);
        // }
        // if (windTrack.isEmpty()) {
        // windTrack = getWindTrackWithLastNodeOfLastLevelConsideringPointOfSailsWithoutWindSpeed(bestPath,
        // bestPathConfidence * 0.5);
        // }
        return windTrack;
    }

    private List<WindWithConfidence<TimePoint>> getWindTrackWithLastNodeOfLastLevelConsideringMiddleCoursesOfCleanTacksAndJibes(
            List<Pair<GraphLevel, FineGrainedPointOfSail>> bestPath, double baseConfidence) {
        List<WindWithConfidence<TimePoint>> windTrack = new ArrayList<>();
        for (ListIterator<Pair<GraphLevel, FineGrainedPointOfSail>> iterator = bestPath.listIterator(); iterator
                .hasNext();) {
            Pair<GraphLevel, FineGrainedPointOfSail> pathEntry = iterator.next();
            final GraphLevel currentLevel = pathEntry.getA();
            final FineGrainedPointOfSail currentNode = pathEntry.getB();
            final FineGrainedPointOfSail pointOfSailBeforeManeuver = currentLevel
                    .getPointOfSailBeforeManeuver(currentNode);
            if (pointOfSailBeforeManeuver.getCoarseGrainedPointOfSail().getLegType() == currentNode
                    .getCoarseGrainedPointOfSail().getLegType()
                    && pointOfSailBeforeManeuver.getTack() != currentNode.getTack()
                    && Math.abs(currentLevel.getManeuver().getCourseChangeWithinMainCurveInDegrees()) < 120
                    && currentLevel.getManeuver().isClean()) {
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
        }
        return windTrack;
    }

    private List<WindWithConfidence<TimePoint>> getWindTrackWithLastNodeOfLastLevelConsideringStableCourses(
            List<Pair<GraphLevel, FineGrainedPointOfSail>> bestPath, double baseConfidence) {
        List<WindWithConfidence<TimePoint>> windTrack = new ArrayList<>();
        for (Pair<GraphLevel, FineGrainedPointOfSail> pathEntry : bestPath) {
            final GraphLevel currentLevel = pathEntry.getA();
            final FineGrainedPointOfSail currentNode = pathEntry.getB();
            final FineGrainedPointOfSail pointOfSailBeforeManeuver = currentLevel
                    .getPointOfSailBeforeManeuver(currentNode);
            if (pointOfSailBeforeManeuver.getCoarseGrainedPointOfSail() != currentNode.getCoarseGrainedPointOfSail()) {
                FineGrainedPointOfSail pointOfSailForWindSpeedDetermination = null;
                Speed boatSpeedForWindSpeedDetermination = null;
                if (currentLevel.getManeuver().isCleanAfter()) {
                    pointOfSailForWindSpeedDetermination = currentNode;
                    boatSpeedForWindSpeedDetermination = currentLevel.getManeuver().getSpeedWithBearingAfter();
                } else if (currentLevel.getManeuver().isCleanBefore()) {
                    pointOfSailForWindSpeedDetermination = pointOfSailBeforeManeuver;
                    boatSpeedForWindSpeedDetermination = currentLevel.getManeuver().getSpeedWithBearingBefore();
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
        }
        return windTrack;
    }

    private List<WindWithConfidence<TimePoint>> getWindTrackWithLastNodeOfLastLevelConsideringPointOfSailsWithoutWindSpeed(
            List<Pair<GraphLevel, FineGrainedPointOfSail>> bestPath, double baseConfidence) {
        List<WindWithConfidence<TimePoint>> windTrack = new ArrayList<>();
        for (Pair<GraphLevel, FineGrainedPointOfSail> pathEntry : bestPath) {
            final GraphLevel currentLevel = pathEntry.getA();
            final FineGrainedPointOfSail currentNode = pathEntry.getB();
            final FineGrainedPointOfSail pointOfSailBeforeManeuver = currentLevel
                    .getPointOfSailBeforeManeuver(currentNode);
            if (pointOfSailBeforeManeuver.getCoarseGrainedPointOfSail() != currentNode.getCoarseGrainedPointOfSail()) {
                WindWithConfidenceImpl<TimePoint> windWithConfidence = getWindWithConfidenceWithoutSpeed(
                        pointOfSailBeforeManeuver, currentLevel, baseConfidence);
                windTrack.add(windWithConfidence);
            }
        }
        return windTrack;
    }

    private WindWithConfidenceImpl<TimePoint> getWindWithConfidenceWithoutSpeed(FineGrainedPointOfSail pointOfSail,
            GraphLevel currentLevel, double baseConfidence) {
        double windCourseInDegrees = currentLevel.getWindCourseInDegrees(pointOfSail);
        WindWithConfidenceImpl<TimePoint> windWithConfidence = constructWindWithConfidence(windCourseInDegrees, 0,
                currentLevel, baseConfidence);
        return windWithConfidence;
    }

    private WindWithConfidenceImpl<TimePoint> getWindWithConfidence(
            FineGrainedPointOfSail pointOfSailForWindSpeedDetermination, Speed boatSpeedForWindSpeedDetermination,
            GraphLevel currentLevel, double baseConfidence) {
        Set<SpeedWithBearingWithConfidence<Void>> trueWindSpeedAndTwaCandidates = polarService
                .getAverageTrueWindSpeedAndAngleCandidates(currentLevel.getManeuver().getBoatClass(),
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

    private WindWithConfidenceImpl<TimePoint> getWindWithConfidenceIfTackOrJibe(GraphLevel currentLevel,
            double baseConfidence, ManeuverType tackOrJibe) {
        assert tackOrJibe == ManeuverType.TACK || tackOrJibe == ManeuverType.JIBE;
        Speed boatSpeed = new KnotSpeedImpl((currentLevel.getManeuver().getSpeedWithBearingBefore().getKnots()
                + currentLevel.getManeuver().getSpeedWithBearingAfter().getKnots()) / 2);
        double directionChangeInDegrees = currentLevel.getManeuver().getCourseChangeInDegrees();
        Pair<Double, SpeedWithBearingWithConfidence<Void>> tackOrJibeLikelihoodWithTwaTws = polarService
                .getManeuverLikelihoodAndTwsTwa(currentLevel.getManeuver().getBoatClass(), boatSpeed,
                        directionChangeInDegrees, tackOrJibe);
        WindWithConfidenceImpl<TimePoint> windWithConfidence = null;
        if (tackOrJibeLikelihoodWithTwaTws.getA() > 0.0001) {
            SpeedWithBearing windSpeedAndTwaIfTackOrJibe = tackOrJibeLikelihoodWithTwaTws.getB().getObject();
            Bearing windCourse = currentLevel.getManeuver().getMiddleCourse();
            if (tackOrJibe == ManeuverType.TACK) {
                windCourse = windCourse.reverse();
            }
            SpeedWithBearing windSpeedWithCourse = new KnotSpeedWithBearingImpl(windSpeedAndTwaIfTackOrJibe.getKnots(),
                    windCourse);
            windWithConfidence = constructWindWithConfidence(windSpeedWithCourse, currentLevel,
                    tackOrJibeLikelihoodWithTwaTws.getA() * baseConfidence);

        }
        return windWithConfidence;
    }

    private WindWithConfidenceImpl<TimePoint> constructWindWithConfidence(SpeedWithBearing windSpeedWithCourse,
            GraphLevel currentLevel, double confidence) {
        WindImpl wind = new WindImpl(currentLevel.getManeuver().getManeuverPosition(),
                currentLevel.getManeuver().getManeuverTimePoint(), windSpeedWithCourse);
        WindWithConfidenceImpl<TimePoint> windWithConfidence = new WindWithConfidenceImpl<TimePoint>(wind, confidence,
                currentLevel.getManeuver().getManeuverTimePoint(), windSpeedWithCourse.getKnots() > 0);
        return windWithConfidence;
    }

    private WindWithConfidenceImpl<TimePoint> constructWindWithConfidence(double windCourseDeg, double windSpeedKnots,
            GraphLevel currentLevel, double confidence) {
        DegreeBearingImpl windCourse = new DegreeBearingImpl(windCourseDeg);
        SpeedWithBearing windSpeedWithCourse = new KnotSpeedWithBearingImpl(windSpeedKnots, windCourse);
        return constructWindWithConfidence(windSpeedWithCourse, currentLevel, confidence);
    }

}
