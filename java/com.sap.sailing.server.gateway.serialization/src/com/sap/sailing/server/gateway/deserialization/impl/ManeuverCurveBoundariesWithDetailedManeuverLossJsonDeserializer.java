package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.maneuverdetection.ManeuverCurveBoundariesWithDetailedManeuverLoss;
import com.sap.sailing.domain.maneuverdetection.impl.ManeuverCurveBoundariesWithDetailedManeuverLossImpl;
import com.sap.sailing.domain.tracking.ManeuverCurveBoundaries;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.serialization.impl.ManeuverCurveBoundariesWithDetailedManeuverLossJsonSerializer;
import com.sap.sse.common.Distance;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverCurveBoundariesWithDetailedManeuverLossJsonDeserializer
        extends ManeuverCurveBoundariesJsonDeserializer {

    @Override
    public ManeuverCurveBoundariesWithDetailedManeuverLoss deserialize(JSONObject object)
            throws JsonDeserializationException {
        ManeuverCurveBoundaries boundaries = super.deserialize(object);
        Double distanceSailedWithinManeuverInMeters = (Double) object.get(
                ManeuverCurveBoundariesWithDetailedManeuverLossJsonSerializer.DISTANCE_SAILED_WITHIN_MANEUVER_IN_METERS);
        Double distanceSailedWithinManeuverTowardMiddleAngleProjectionInMeters = (Double) object.get(
                ManeuverCurveBoundariesWithDetailedManeuverLossJsonSerializer.DISTANCE_SAILED_WITHIN_MANEUVER_TOWARD_MIDDLE_ANGLE_PROJECTION_IN_METERS);
        Double distanceSailedIfNotManeuveringInMeters = (Double) object.get(
                ManeuverCurveBoundariesWithDetailedManeuverLossJsonSerializer.DISTANCE_SAILED_IF_NOT_MANEUVERING_IN_METERS);
        Double distanceSailedTowardMiddleAngleProjectionIfNotManeuveringInMeters = (Double) object.get(
                ManeuverCurveBoundariesWithDetailedManeuverLossJsonSerializer.DISTANCE_SAILED_IF_NOT_MANEUVERING_TOWARD_MIDDLE_ANGLE_PROJECTION_IN_METERS);
        return new ManeuverCurveBoundariesWithDetailedManeuverLossImpl(boundaries.getTimePointBefore(),
                boundaries.getTimePointAfter(), boundaries.getSpeedWithBearingBefore(),
                boundaries.getSpeedWithBearingAfter(), boundaries.getDirectionChangeInDegrees(),
                boundaries.getLowestSpeed(), convertDistance(distanceSailedWithinManeuverInMeters),
                convertDistance(distanceSailedWithinManeuverTowardMiddleAngleProjectionInMeters),
                convertDistance(distanceSailedIfNotManeuveringInMeters),
                convertDistance(distanceSailedTowardMiddleAngleProjectionIfNotManeuveringInMeters));
    }

    private Distance convertDistance(Double meters) {
        return meters == null ? null : new MeterDistance(meters);
    }

}
