package com.sap.sailing.polars.jaxrs.deserialization;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.common.BoatHullType;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.polars.jaxrs.serialization.BoatClassSerializer;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;

public class BoatClassDeserializer implements JsonDeserializer<BoatClass> {

    @Override
    public BoatClass deserialize(JSONObject object) throws JsonDeserializationException {
        String name = (String) object.get(BoatClassSerializer.FIELD_NAME);
        boolean typicallyStartsUpwind = (boolean) object.get(BoatClassSerializer.FIELD_TYPICALLY_STARTS_UPWIND);
        String displayName = null;
        Distance hullLength = null;
        Distance hullBeam = null;
        BoatHullType hullType = null;

        if (object.get(BoatClassSerializer.FIELD_DISPLAY_NAME) != null) {
            displayName = (String) object.get(BoatClassSerializer.FIELD_DISPLAY_NAME);
        }
        if (object.get(BoatClassSerializer.FIELD_HULL_LENGTH) != null) {
            hullLength = new MeterDistance((double) object.get(BoatClassSerializer.FIELD_HULL_LENGTH));
        }
        if (object.get(BoatClassSerializer.FIELD_HULL_BEAM) != null) {
            hullBeam = new MeterDistance((double) object.get(BoatClassSerializer.FIELD_HULL_BEAM));
        }
        if (object.get(BoatClassSerializer.FIELD_HULL_TYPE) != null) {
            hullType = BoatHullType.valueOf((String) object.get(BoatClassSerializer.FIELD_HULL_TYPE));
        }

        return new BoatClassImpl(name, typicallyStartsUpwind, displayName, hullLength, hullBeam, hullType);
    }

}
