package com.sap.sailing.windestimation.data.deserializer;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.BoatClassJsonDeserializer;
import com.sap.sailing.windestimation.data.LabelledManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverCategory;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class ManeuverForEstimationJsonDeserializer implements JsonDeserializer<ManeuverForEstimation> {

    private final BoatClassJsonDeserializer boatClassDeserializer = new BoatClassJsonDeserializer(
            DomainFactory.INSTANCE);

    @Override
    public ManeuverForEstimation deserialize(JSONObject object) throws JsonDeserializationException {
        long maneuverTimePoint = (long) object.get(ManeuverForEstimationJsonSerializer.TIMEPOINT);
        double positionLatitude = (double) object.get(ManeuverForEstimationJsonSerializer.POSITION_LATITUDE);
        double positionLongitude = (double) object.get(ManeuverForEstimationJsonSerializer.POSITION_LONGITUDE);
        double middleCourseInDegrees = (double) object.get(ManeuverForEstimationJsonSerializer.MIDDLE_COURSE);
        double speedBeforeInKnots = (double) object.get(ManeuverForEstimationJsonSerializer.SPEED_BEFORE);
        double speedAfterInKnots = (double) object.get(ManeuverForEstimationJsonSerializer.SPEED_AFTER);
        double cogBefore = (double) object.get(ManeuverForEstimationJsonSerializer.COURSE_BEFORE);
        double cogAfter = (double) object.get(ManeuverForEstimationJsonSerializer.COURSE_AFTER);
        double cogLowestSpeed = (double) object.get(ManeuverForEstimationJsonSerializer.COURSE_AT_LOWEST_SPEED);
        Double avgSpeedBeforeInKnots = (Double) object.get(ManeuverForEstimationJsonSerializer.AVG_SPEED_BEFORE);
        Double avgSpeedAfterInKnots = (Double) object.get(ManeuverForEstimationJsonSerializer.AVG_SPEED_AFTER);
        Double avgCogBefore = (Double) object.get(ManeuverForEstimationJsonSerializer.AVG_COURSE_BEFORE);
        Double avgCogAfter = (Double) object.get(ManeuverForEstimationJsonSerializer.AVG_COURSE_AFTER);
        double courseChangeInDegrees = (double) object.get(ManeuverForEstimationJsonSerializer.COURSE_CHANGE);
        double courseChangeMainCurveInDegrees = (double) object
                .get(ManeuverForEstimationJsonSerializer.COURSE_CHANGE_MAIN_CURVE);
        double maxTurningRateInDegrees = (double) object.get(ManeuverForEstimationJsonSerializer.MAX_TURNING_RATE);
        Double deviationFromOptimalTackAngleInDegrees = (Double) object
                .get(ManeuverForEstimationJsonSerializer.DEVIATION_FROM_OPTIMAL_TACK_ANGLE);
        Double deviationFromOptimalJibeAngleInDegrees = (Double) object
                .get(ManeuverForEstimationJsonSerializer.DEVIATION_FROM_OPTIMAL_JIBE_ANGLE);
        double speedLossRatio = (double) object.get(ManeuverForEstimationJsonSerializer.SPEED_LOSS_RATIO);
        double speedGainRatio = (double) object.get(ManeuverForEstimationJsonSerializer.SPEED_GAIN_RATIO);
        double lowestVsExitingSpeedRatio = (double) object
                .get(ManeuverForEstimationJsonSerializer.LOWEST_VS_EXITING_SPEED_RATIO);
        boolean clean = (boolean) object.get(ManeuverForEstimationJsonSerializer.CLEAN);
        boolean cleanBefore = (boolean) object.get(ManeuverForEstimationJsonSerializer.CLEAN_BEFORE);
        boolean cleanAfter = (boolean) object.get(ManeuverForEstimationJsonSerializer.CLEAN_AFTER);
        ManeuverCategory maneuverCategory = ManeuverCategory
                .valueOf((String) object.get(ManeuverForEstimationJsonSerializer.MANEUVER_CATEGORY));
        double scaledSpeedBefore = (double) object
                .get(ManeuverForEstimationJsonSerializer.SCALED_SPEED_BEFORE_IN_KNOTS);
        double scaledSpeedAfter = (double) object.get(ManeuverForEstimationJsonSerializer.SCALED_SPEED_AFTER_IN_KNOTS);
        BoatClass boatClass = boatClassDeserializer
                .deserialize((JSONObject) object.get(ManeuverForEstimationJsonSerializer.BOAT_CLASS));
        Double relativeBearingToNextMarkBeforeInDegrees = (Double) object
                .get(ManeuverForEstimationJsonSerializer.RELATIVE_BEARING_TO_NEXT_MARK_BEFORE_IN_DEGREES);
        Double relativeBearingToNextMarkAfterInDegrees = (Double) object
                .get(ManeuverForEstimationJsonSerializer.RELATIVE_BEARING_TO_NEXT_MARK_AFTER_IN_DEGREES);
        boolean markPassing = (boolean) object.get(ManeuverForEstimationJsonSerializer.MARK_PASSING);
        String regattaName = (String) object.get("regattaName");
        ManeuverForEstimation maneuver = new ManeuverForEstimation(new MillisecondsTimePoint(maneuverTimePoint),
                new DegreePosition(positionLatitude, positionLongitude), new DegreeBearingImpl(middleCourseInDegrees),
                new KnotSpeedWithBearingImpl(speedBeforeInKnots, new DegreeBearingImpl(cogBefore)),
                new KnotSpeedWithBearingImpl(speedAfterInKnots, new DegreeBearingImpl(cogAfter)),
                new DegreeBearingImpl(cogLowestSpeed),
                avgSpeedBeforeInKnots == null ? null
                        : new KnotSpeedWithBearingImpl(avgSpeedBeforeInKnots, new DegreeBearingImpl(avgCogBefore)),
                avgSpeedAfterInKnots == null ? null
                        : new KnotSpeedWithBearingImpl(avgSpeedAfterInKnots, new DegreeBearingImpl(avgCogAfter)),
                courseChangeInDegrees, courseChangeMainCurveInDegrees, maxTurningRateInDegrees,
                deviationFromOptimalTackAngleInDegrees, deviationFromOptimalJibeAngleInDegrees, speedLossRatio,
                speedGainRatio, lowestVsExitingSpeedRatio, clean, cleanBefore, cleanAfter, maneuverCategory,
                scaledSpeedBefore, scaledSpeedAfter, boatClass, markPassing, relativeBearingToNextMarkBeforeInDegrees,
                relativeBearingToNextMarkAfterInDegrees, regattaName);
        if (object.containsKey(ManeuverForEstimationJsonSerializer.WIND_SPEED)) {
            ManeuverTypeForClassification maneuverType = ManeuverTypeForClassification
                    .valueOf((String) object.get(ManeuverForEstimationJsonSerializer.MANEUVER_TYPE));
            Double windSpeedInKnots = (Double) object.get(ManeuverForEstimationJsonSerializer.WIND_SPEED);
            Double windCourse = (Double) object.get(ManeuverForEstimationJsonSerializer.WIND_COURSE);
            maneuver = new LabelledManeuverForEstimation(maneuver.getManeuverTimePoint(),
                    maneuver.getManeuverPosition(), maneuver.getMiddleCourse(), maneuver.getSpeedWithBearingBefore(),
                    maneuver.getSpeedWithBearingAfter(), maneuver.getCourseAtLowestSpeed(),
                    maneuver.getAverageSpeedWithBearingBefore(), maneuver.getAverageSpeedWithBearingAfter(),
                    maneuver.getCourseChangeInDegrees(), maneuver.getCourseChangeWithinMainCurveInDegrees(),
                    maneuver.getMaxTurningRateInDegreesPerSecond(),
                    maneuver.getDeviationFromOptimalTackAngleInDegrees(),
                    maneuver.getDeviationFromOptimalJibeAngleInDegrees(), maneuver.getSpeedLossRatio(),
                    maneuver.getSpeedGainRatio(), maneuver.getLowestSpeedVsExitingSpeedRatio(), maneuver.isClean(),
                    maneuver.isCleanBefore(), maneuver.isCleanAfter(), maneuver.getManeuverCategory(),
                    maneuver.getScaledSpeedBefore(), maneuver.getScaledSpeedAfter(), maneuver.getBoatClass(),
                    markPassing, relativeBearingToNextMarkBeforeInDegrees, relativeBearingToNextMarkAfterInDegrees,
                    regattaName, maneuverType,
                    new WindImpl(maneuver.getManeuverPosition(), maneuver.getManeuverTimePoint(),
                            new KnotSpeedWithBearingImpl(windSpeedInKnots, new DegreeBearingImpl(windCourse))));
        }
        return maneuver;
    }

}
