package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.maneuverdetection.HasDetailedManeuverLoss;
import com.sap.sailing.domain.tracking.ManeuverCurveBoundaries;
import com.sap.sse.common.Distance;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverCurveBoundariesWithDetailedManeuverLossJsonSerializer
        extends ManeuverCurveBoundariesJsonSerializer {

    public final static String DISTANCE_SAILED_WITHIN_MANEUVER_IN_METERS = "metersSailed";
    public final static String DISTANCE_SAILED_WITHIN_MANEUVER_TOWARD_MIDDLE_ANGLE_PROJECTION_IN_METERS = "metersSailedProjected";
    public final static String DISTANCE_SAILED_IF_NOT_MANEUVERING_IN_METERS = "metersWithoutManeuver";
    public final static String DISTANCE_SAILED_IF_NOT_MANEUVERING_TOWARD_MIDDLE_ANGLE_PROJECTION_IN_METERS = "metersWithoutManeuverProjected";

    @Override
    public JSONObject serialize(ManeuverCurveBoundaries curveBoundaries) {
        JSONObject result = super.serialize(curveBoundaries);
        if (curveBoundaries instanceof HasDetailedManeuverLoss) {
            HasDetailedManeuverLoss details = (HasDetailedManeuverLoss) curveBoundaries;
            result.put(DISTANCE_SAILED_WITHIN_MANEUVER_IN_METERS,
                    convertMeters(details.getDistanceSailedWithinManeuver()));
            result.put(DISTANCE_SAILED_WITHIN_MANEUVER_TOWARD_MIDDLE_ANGLE_PROJECTION_IN_METERS,
                    convertMeters(details.getDistanceSailedWithinManeuverTowardMiddleAngleProjection()));
            result.put(DISTANCE_SAILED_IF_NOT_MANEUVERING_IN_METERS,
                    convertMeters(details.getDistanceSailedIfNotManeuvering()));
            result.put(DISTANCE_SAILED_IF_NOT_MANEUVERING_TOWARD_MIDDLE_ANGLE_PROJECTION_IN_METERS,
                    convertMeters(details.getDistanceSailedTowardMiddleAngleProjectionIfNotManeuvering()));
        }
        return result;
    }

    private Double convertMeters(Distance distance) {
        return distance == null ? null : distance.getMeters();
    }

}
