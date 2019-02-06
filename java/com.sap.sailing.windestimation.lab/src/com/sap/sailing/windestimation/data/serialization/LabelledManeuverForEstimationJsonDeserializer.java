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
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class LabelledManeuverForEstimationJsonDeserializer implements JsonDeserializer<LabelledManeuverForEstimation> {

    private final BoatClassJsonDeserializer boatClassDeserializer = new BoatClassJsonDeserializer(
            DomainFactory.INSTANCE);

    @Override
    public LabelledManeuverForEstimation deserialize(JSONObject object) throws JsonDeserializationException {
        long maneuverTimePointMillis = (long) object.get(LabelledManeuverForEstimationJsonSerializer.TIMEPOINT);
        double positionLatitude = (double) object.get(LabelledManeuverForEstimationJsonSerializer.POSITION_LATITUDE);
        double positionLongitude = (double) object.get(LabelledManeuverForEstimationJsonSerializer.POSITION_LONGITUDE);
        double middleCourseInDegrees = (double) object.get(LabelledManeuverForEstimationJsonSerializer.MIDDLE_COURSE);
        double speedBeforeInKnots = (double) object.get(LabelledManeuverForEstimationJsonSerializer.SPEED_BEFORE);
        double speedAfterInKnots = (double) object.get(LabelledManeuverForEstimationJsonSerializer.SPEED_AFTER);
        double cogBefore = (double) object.get(LabelledManeuverForEstimationJsonSerializer.COURSE_BEFORE);
        double cogAfter = (double) object.get(LabelledManeuverForEstimationJsonSerializer.COURSE_AFTER);
        double courseChangeInDegrees = (double) object.get(LabelledManeuverForEstimationJsonSerializer.COURSE_CHANGE);
        double courseChangeMainCurveInDegrees = (double) object
                .get(LabelledManeuverForEstimationJsonSerializer.COURSE_CHANGE_MAIN_CURVE);
        double maxTurningRateInDegrees = (double) object
                .get(LabelledManeuverForEstimationJsonSerializer.MAX_TURNING_RATE);
        Double deviationFromOptimalTackAngleInDegrees = (Double) object
                .get(LabelledManeuverForEstimationJsonSerializer.DEVIATION_FROM_OPTIMAL_TACK_ANGLE);
        Double deviationFromOptimalJibeAngleInDegrees = (Double) object
                .get(LabelledManeuverForEstimationJsonSerializer.DEVIATION_FROM_OPTIMAL_JIBE_ANGLE);
        double speedLossRatio = (double) object.get(LabelledManeuverForEstimationJsonSerializer.SPEED_LOSS_RATIO);
        double speedGainRatio = (double) object.get(LabelledManeuverForEstimationJsonSerializer.SPEED_GAIN_RATIO);
        double lowestVsExitingSpeedRatio = (double) object
                .get(LabelledManeuverForEstimationJsonSerializer.LOWEST_VS_EXITING_SPEED_RATIO);
        boolean clean = (boolean) object.get(LabelledManeuverForEstimationJsonSerializer.CLEAN);
        ManeuverCategory maneuverCategory = ManeuverCategory
                .valueOf((String) object.get(LabelledManeuverForEstimationJsonSerializer.MANEUVER_CATEGORY));
        double scaledSpeedBefore = (double) object
                .get(LabelledManeuverForEstimationJsonSerializer.SCALED_SPEED_BEFORE_IN_KNOTS);
        double scaledSpeedAfter = (double) object
                .get(LabelledManeuverForEstimationJsonSerializer.SCALED_SPEED_AFTER_IN_KNOTS);
        BoatClass boatClass = boatClassDeserializer
                .deserialize((JSONObject) object.get(LabelledManeuverForEstimationJsonSerializer.BOAT_CLASS));
        boolean markPassing = (boolean) object.get(LabelledManeuverForEstimationJsonSerializer.MARK_PASSING);
        boolean markPassingDataAvailable = (boolean) object
                .get(LabelledManeuverForEstimationJsonSerializer.MARK_PASSING_DATA_AVAILABLE);
        String maneuverTypeStr = (String) object.get(LabelledManeuverForEstimationJsonSerializer.MANEUVER_TYPE);
        ManeuverTypeForClassification maneuverType = maneuverTypeStr == null ? null
                : ManeuverTypeForClassification.valueOf(maneuverTypeStr);
        Double windSpeedInKnots = (Double) object.get(LabelledManeuverForEstimationJsonSerializer.WIND_SPEED);
        Double windCourse = (Double) object.get(LabelledManeuverForEstimationJsonSerializer.WIND_COURSE);
        String regattaName = (String) object.get(LabelledManeuverForEstimationJsonSerializer.REGATTA_NAME);
        MillisecondsTimePoint maneuverTimePoint = new MillisecondsTimePoint(maneuverTimePointMillis);
        DegreePosition maneuverPosition = new DegreePosition(positionLatitude, positionLongitude);
        LabelledManeuverForEstimation maneuver = new LabelledManeuverForEstimation(maneuverTimePoint, maneuverPosition,
                new DegreeBearingImpl(middleCourseInDegrees),
                new KnotSpeedWithBearingImpl(speedBeforeInKnots, new DegreeBearingImpl(cogBefore)),
                new KnotSpeedWithBearingImpl(speedAfterInKnots, new DegreeBearingImpl(cogAfter)), courseChangeInDegrees,
                courseChangeMainCurveInDegrees, maxTurningRateInDegrees, deviationFromOptimalTackAngleInDegrees,
                deviationFromOptimalJibeAngleInDegrees, speedLossRatio, speedGainRatio, lowestVsExitingSpeedRatio,
                clean, maneuverCategory, scaledSpeedBefore, scaledSpeedAfter, markPassing, boatClass,
                markPassingDataAvailable, maneuverType,
                new WindImpl(maneuverPosition, maneuverTimePoint,
                        new KnotSpeedWithBearingImpl(windSpeedInKnots, new DegreeBearingImpl(windCourse))),
                regattaName);
        return maneuver;
    }

}
