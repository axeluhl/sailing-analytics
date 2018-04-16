package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.common.BoatHullType;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.BoatClassJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.DetailedBoatClassJsonSerializer;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class DetailedBoatClassJsonDeserializer implements JsonDeserializer<BoatClass> {

    @Override
    public BoatClass deserialize(JSONObject object) throws JsonDeserializationException {
        String name = (String) object.get(BoatClassJsonSerializer.FIELD_NAME);
        Boolean typicallyStartsUpwind = (Boolean) object.get(BoatClassJsonSerializer.FIELD_TYPICALLY_STARTS_UPWIND);
        String displayName = (String) object.get(DetailedBoatClassJsonSerializer.DISPLAY_NAME);
        Double hullLengthInMeters = (Double) object.get(DetailedBoatClassJsonSerializer.HULL_LENGTH_IN_METERS);
        Double hullBeamInMeters = (Double) object.get(DetailedBoatClassJsonSerializer.HULL_BEAM_IN_METERS);
        String hullType = (String) object.get(DetailedBoatClassJsonSerializer.HULL_TYPE);
        return new BoatClassImpl(name, typicallyStartsUpwind, displayName, new MeterDistance(hullLengthInMeters),
                new MeterDistance(hullBeamInMeters), BoatHullType.valueOf(hullType));
    }

}
