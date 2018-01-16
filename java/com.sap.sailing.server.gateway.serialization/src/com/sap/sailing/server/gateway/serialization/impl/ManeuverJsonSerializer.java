package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class ManeuverJsonSerializer implements JsonSerializer<Maneuver> {
    public static final String MANEUVER_TYPE = "maneuverType";
    public static final String NEW_TACK = "newTack";
    public static final String SPEED_BEFORE_IN_KNOTS = "speedBeforeInKnots";
    public static final String COG_BEFORE_IN_TRUE_DEGREES = "cogBeforeInTrueDegrees";
    public static final String SPEED_AFTER_IN_KNOTS = "speedAfterInKnots";
    public static final String COG_AFTER_IN_TRUE_DEGREES = "cogAfterInTrueDegrees";
    public static final String DIRECTION_CHANGE_IN_DEGREES = "directionChangeInDegrees";
    public static final String MANEUVER_LOSS = "maneuverLoss";
    public static final String POSITION_AND_TIME = "positionAndTime";
    public static final String MAX_ANGULAR_VELOCITY_IN_DEGREES_PER_SECOND = "maxAngularVelocityInDegreesPerSecond";

    private final GPSFixJsonSerializer gpsFixSerializer;
    private final DistanceJsonSerializer distanceSerializer;

    public ManeuverJsonSerializer(GPSFixJsonSerializer gpsFixSerializer, DistanceJsonSerializer distanceSerializer) {
        super();
        this.gpsFixSerializer = gpsFixSerializer;
        this.distanceSerializer = distanceSerializer;
    }

    @Override
    public JSONObject serialize(Maneuver maneuver) {
        final JSONObject result = new JSONObject();
        result.put(MANEUVER_TYPE, maneuver.getType() == null ? null : maneuver.getType().name());
        result.put(NEW_TACK, maneuver.getNewTack() == null ? null : maneuver.getNewTack().name());
        result.put(SPEED_BEFORE_IN_KNOTS,
                maneuver.getSpeedWithBearingBefore() == null ? null : maneuver.getSpeedWithBearingBefore().getKnots());
        result.put(COG_BEFORE_IN_TRUE_DEGREES, maneuver.getSpeedWithBearingBefore() == null ? null
                : maneuver.getSpeedWithBearingBefore().getBearing().getDegrees());
        result.put(SPEED_AFTER_IN_KNOTS,
                maneuver.getSpeedWithBearingAfter() == null ? null : maneuver.getSpeedWithBearingAfter().getKnots());
        result.put(COG_AFTER_IN_TRUE_DEGREES, maneuver.getSpeedWithBearingAfter() == null ? null
                : maneuver.getSpeedWithBearingAfter().getBearing().getDegrees());
        result.put(DIRECTION_CHANGE_IN_DEGREES, maneuver.getDirectionChangeInDegrees());
        result.put(MANEUVER_LOSS,
                maneuver.getManeuverLoss() == null ? null : distanceSerializer.serialize(maneuver.getManeuverLoss()));
        result.put(POSITION_AND_TIME, gpsFixSerializer.serialize(maneuver));
        result.put(MAX_ANGULAR_VELOCITY_IN_DEGREES_PER_SECOND, maneuver.getMaxAngularVelocityInDegreesPerSecond());
        return result;
    }
}
