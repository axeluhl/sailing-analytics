package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.ManeuverWindJsonSerializer;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.impl.DegreeBearingImpl;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverWindJsonDeserializer implements JsonDeserializer<SpeedWithBearing> {

    public SpeedWithBearing deserialize(JSONObject object) throws JsonDeserializationException {

        Double directionInTrueDegrees = (Double) object.get(ManeuverWindJsonSerializer.DIRECTION_IN_TRUE_DEGREES);
        Double speedInKnots = (Double) object.get(ManeuverWindJsonSerializer.SPEED_IN_KNOTS);
        Bearing degreeBearing = new DegreeBearingImpl(directionInTrueDegrees);
        SpeedWithBearing speedBearing = new KnotSpeedWithBearingImpl(speedInKnots, degreeBearing);
        return speedBearing;
    }

}
