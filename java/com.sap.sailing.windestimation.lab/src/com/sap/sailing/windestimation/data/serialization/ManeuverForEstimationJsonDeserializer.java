package com.sap.sailing.windestimation.data.serialization;

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
        ManeuverCategory maneuverCategory = ManeuverCategory
                .valueOf((String) object.get(ManeuverForEstimationJsonSerializer.MANEUVER_CATEGORY));
        double scaledSpeedBefore = (double) object
                .get(ManeuverForEstimationJsonSerializer.SCALED_SPEED_BEFORE_IN_KNOTS);
        double scaledSpeedAfter = (double) object.get(ManeuverForEstimationJsonSerializer.SCALED_SPEED_AFTER_IN_KNOTS);
        BoatClass boatClass = boatClassDeserializer
                .deserialize((JSONObject) object.get(ManeuverForEstimationJsonSerializer.BOAT_CLASS));
        boolean markPassing = (boolean) object.get(ManeuverForEstimationJsonSerializer.MARK_PASSING);
        boolean markPassingDataAvailable = (boolean) object
                .get(ManeuverForEstimationJsonSerializer.MARK_PASSING_DATA_AVAILABLE);
        ManeuverForEstimation maneuver = new ManeuverForEstimation(new MillisecondsTimePoint(maneuverTimePoint),
                new DegreePosition(positionLatitude, positionLongitude), new DegreeBearingImpl(middleCourseInDegrees),
                new KnotSpeedWithBearingImpl(speedBeforeInKnots, new DegreeBearingImpl(cogBefore)),
                new KnotSpeedWithBearingImpl(speedAfterInKnots, new DegreeBearingImpl(cogAfter)), courseChangeInDegrees,
                courseChangeMainCurveInDegrees, maxTurningRateInDegrees, deviationFromOptimalTackAngleInDegrees,
                deviationFromOptimalJibeAngleInDegrees, speedLossRatio, speedGainRatio, lowestVsExitingSpeedRatio,
                clean, maneuverCategory, scaledSpeedBefore, scaledSpeedAfter, markPassing, boatClass,
                markPassingDataAvailable);
        if (object.containsKey(ManeuverForEstimationJsonSerializer.WIND_SPEED)) {
            String maneuverTypeStr = (String) object.get(ManeuverForEstimationJsonSerializer.MANEUVER_TYPE);
            ManeuverTypeForClassification maneuverType = maneuverTypeStr == null ? null
                    : ManeuverTypeForClassification.valueOf(maneuverTypeStr);
            Double windSpeedInKnots = (Double) object.get(ManeuverForEstimationJsonSerializer.WIND_SPEED);
            Double windCourse = (Double) object.get(ManeuverForEstimationJsonSerializer.WIND_COURSE);
            String regattaName = (String) object.get(ManeuverForEstimationJsonSerializer.REGATTA_NAME);
            maneuver = new LabelledManeuverForEstimation(maneuver.getManeuverTimePoint(),
                    maneuver.getManeuverPosition(), maneuver.getMiddleCourse(), maneuver.getSpeedWithBearingBefore(),
                    maneuver.getSpeedWithBearingAfter(), maneuver.getCourseChangeInDegrees(),
                    maneuver.getCourseChangeWithinMainCurveInDegrees(), maneuver.getMaxTurningRateInDegreesPerSecond(),
                    maneuver.getDeviationFromOptimalTackAngleInDegrees(),
                    maneuver.getDeviationFromOptimalJibeAngleInDegrees(), maneuver.getSpeedLossRatio(),
                    maneuver.getSpeedGainRatio(), maneuver.getLowestSpeedVsExitingSpeedRatio(), maneuver.isClean(),
                    maneuver.getManeuverCategory(), maneuver.getScaledSpeedBefore(), maneuver.getScaledSpeedAfter(),
                    markPassing, maneuver.getBoatClass(), maneuver.isMarkPassingDataAvailable(), maneuverType,
                    new WindImpl(maneuver.getManeuverPosition(), maneuver.getManeuverTimePoint(),
                            new KnotSpeedWithBearingImpl(windSpeedInKnots, new DegreeBearingImpl(windCourse))),
                    regattaName);
        }
        return maneuver;
    }

}
