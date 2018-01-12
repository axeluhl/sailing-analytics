package com.sap.sailing.domain.maneuverdetection.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.maneuverdetection.ManeuverWithEstimationData;
import com.sap.sailing.domain.maneuverdetection.ManeuverWithEstimationDataCalculator;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.SpeedWithBearingStep;
import com.sap.sailing.domain.tracking.SpeedWithBearingStepsIterable;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverWithEstimationDataCalculatorImpl implements ManeuverWithEstimationDataCalculator {

    @Override
    public Iterable<ManeuverWithEstimationData> complementManeuversWithEstimationData(TrackedRace trackedRace,
            Competitor competitor, Iterable<Maneuver> maneuvers) {
        int maneuversCount = Util.size(maneuvers);
        List<ManeuverWithEstimationData> result = new ArrayList<>(maneuversCount);
        if (maneuversCount > 0) {
            Iterator<Maneuver> maneuversIterator = maneuvers.iterator();
            Maneuver previousManeuver = null;
            Maneuver maneuver = maneuversIterator.next();
            while (maneuversIterator.hasNext()) {
                Maneuver nextManeuver = maneuversIterator.next();
                ManeuverWithEstimationData maneuverWithEstimationData = complementManeuverWithEstimationData(
                        trackedRace, competitor, previousManeuver, maneuver, nextManeuver);
                result.add(maneuverWithEstimationData);
                previousManeuver = maneuver;
                maneuver = nextManeuver;
            }
            ManeuverWithEstimationData maneuverWithEstimationData = complementManeuverWithEstimationData(trackedRace,
                    competitor, previousManeuver, maneuver, null);
            result.add(maneuverWithEstimationData);
        }

        return result;
    }

    private ManeuverWithEstimationData complementManeuverWithEstimationData(TrackedRace trackedRace,
            Competitor competitor, Maneuver previousManeuver, Maneuver maneuver, Maneuver nextManeuver) {
        Wind wind = trackedRace.getWind(maneuver.getPosition(), maneuver.getTimePoint());

        Pair<SpeedWithBearing, SpeedWithBearing> speedPair = determineHighestAndLowestSpeedWithinMainCurve(trackedRace,
                competitor, maneuver);
        SpeedWithBearing highestSpeedWithinMainCurve = speedPair.getA();
        SpeedWithBearing lowestSpeedWithinMainCurve = speedPair.getB();

        Pair<Duration, SpeedWithBearing> durationAndAverageSpeedWithBearingBetweenManeuvers = getDurationAndAverageSpeedWithBearingBetweenManeuvers(
                trackedRace, competitor, previousManeuver, maneuver);
        Duration durationFromPreviousManeuverEndToManeuverStart = durationAndAverageSpeedWithBearingBetweenManeuvers
                .getA();
        SpeedWithBearing averageSpeedWithBearingBefore = durationAndAverageSpeedWithBearingBetweenManeuvers.getB();

        durationAndAverageSpeedWithBearingBetweenManeuvers = getDurationAndAverageSpeedWithBearingBetweenManeuvers(
                trackedRace, competitor, maneuver, nextManeuver);
        Duration durationFromManeuverEndToNextManeuverStart = durationAndAverageSpeedWithBearingBetweenManeuvers.getA();
        SpeedWithBearing averageSpeedWithBearingAfter = durationAndAverageSpeedWithBearingBetweenManeuvers.getB();
        return new ManeuverWithEstimationDataImpl(maneuver, wind, highestSpeedWithinMainCurve,
                lowestSpeedWithinMainCurve, averageSpeedWithBearingBefore,
                durationFromPreviousManeuverEndToManeuverStart, averageSpeedWithBearingAfter,
                durationFromManeuverEndToNextManeuverStart);
    }

    private Pair<Duration, SpeedWithBearing> getDurationAndAverageSpeedWithBearingBetweenManeuvers(
            TrackedRace trackedRace, Competitor competitor, Maneuver previousManeuver, Maneuver maneuver) {
        Duration durationBetweenManeuvers = null;
        SpeedWithBearing averageSpeedWithBearing = null;
        if (previousManeuver != null) {
            TimePoint previousManeuverEnd = previousManeuver.getManeuverCurveWithStableSpeedAndCourseBoundaries()
                    .getTimePointBefore();
            TimePoint maneuverStart = maneuver.getManeuverCurveWithStableSpeedAndCourseBoundaries()
                    .getTimePointBefore();
            Duration duration = previousManeuverEnd.until(maneuverStart);
            if (duration.asSeconds() >= 1) {
                durationBetweenManeuvers = duration;
                SpeedWithBearingStepsIterable speedWithBearingSteps = trackedRace.getTrack(competitor)
                        .getSpeedWithBearingSteps(previousManeuverEnd, maneuverStart);
                double speedSumInKnots = 0;
                double sumBearingDifferenceToManeuverStartInDegrees = 0;
                int count = 0;
                Bearing maneuverStartBearing = maneuver.getManeuverCurveWithStableSpeedAndCourseBoundaries()
                        .getSpeedWithBearingBefore().getBearing();
                for (SpeedWithBearingStep step : speedWithBearingSteps) {
                    speedSumInKnots += step.getSpeedWithBearing().getKnots();
                    sumBearingDifferenceToManeuverStartInDegrees += maneuverStartBearing
                            .getDifferenceTo(step.getSpeedWithBearing().getBearing()).getDegrees();
                    ++count;
                }
                if (count > 0) {
                    averageSpeedWithBearing = new KnotSpeedWithBearingImpl(speedSumInKnots / count,
                            new DegreeBearingImpl((maneuverStartBearing.getDegrees()
                                    + sumBearingDifferenceToManeuverStartInDegrees / count + 360) % 360));
                }
            }
        }
        return new Pair<>(durationBetweenManeuvers, averageSpeedWithBearing);
    }

    private Pair<SpeedWithBearing, SpeedWithBearing> determineHighestAndLowestSpeedWithinMainCurve(
            TrackedRace trackedRace, Competitor competitor, Maneuver maneuver) {
        SpeedWithBearing highestSpeedWithinMainCurve = null;
        SpeedWithBearing lowestSpeedWithinMainCurve = null;
        Iterator<SpeedWithBearingStep> speedWithBearingStepsIterator = trackedRace.getTrack(competitor)
                .getSpeedWithBearingSteps(maneuver.getMainCurveBoundaries().getTimePointBefore(),
                        maneuver.getMainCurveBoundaries().getTimePointAfter())
                .iterator();
        if (speedWithBearingStepsIterator.hasNext()) {
            SpeedWithBearingStep firstStep = speedWithBearingStepsIterator.next();
            highestSpeedWithinMainCurve = lowestSpeedWithinMainCurve = firstStep.getSpeedWithBearing();
            while (speedWithBearingStepsIterator.hasNext()) {
                SpeedWithBearing speedWithBearing = speedWithBearingStepsIterator.next().getSpeedWithBearing();
                if (highestSpeedWithinMainCurve.getKnots() < speedWithBearing.getKnots()) {
                    highestSpeedWithinMainCurve = speedWithBearing;
                }
                if (lowestSpeedWithinMainCurve.getKnots() > speedWithBearing.getKnots()) {
                    lowestSpeedWithinMainCurve = speedWithBearing;
                }
            }
        }
        return new Pair<SpeedWithBearing, SpeedWithBearing>(highestSpeedWithinMainCurve, lowestSpeedWithinMainCurve);
    }

}
