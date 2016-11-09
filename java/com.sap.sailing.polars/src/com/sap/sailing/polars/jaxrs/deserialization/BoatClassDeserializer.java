package com.sap.sailing.polars.jaxrs.deserialization;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatClassImpl.InstanceBuilder;
import com.sap.sailing.domain.common.BoatHullType;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.polars.jaxrs.serialization.BoatClassSerializer;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;

public class BoatClassDeserializer implements JsonDeserializer<BoatClass> {

    @Override
    public BoatClass deserialize(JSONObject object) throws JsonDeserializationException {
        String name = (String) object.get(BoatClassSerializer.FIELD_NAME);
        boolean typicallyStartsUpwind = (boolean) object.get(BoatClassSerializer.FIELD_TYPICALLY_STARTS_UPWIND);
        
        BoatClassImpl.InstanceBuilder boatClassBuilder = new InstanceBuilder(name, typicallyStartsUpwind);

        if (object.containsKey(BoatClassSerializer.FIELD_DISPLAY_NAME)) {
            boatClassBuilder.setDisplayName((String) object.get(BoatClassSerializer.FIELD_DISPLAY_NAME));
        }
        if (object.containsKey(BoatClassSerializer.FIELD_HULL_LENGTH)) {
            boatClassBuilder.setHullLength(new MeterDistance((double) object.get(BoatClassSerializer.FIELD_HULL_LENGTH)));
        }
        if (object.containsKey(BoatClassSerializer.FIELD_HULL_BEAM)) {
            boatClassBuilder.setHullBeam(new MeterDistance((double) object.get(BoatClassSerializer.FIELD_HULL_BEAM)));
        }
        if (object.containsKey(BoatClassSerializer.FIELD_HULL_TYPE)) {
            boatClassBuilder.setHullType(BoatHullType.valueOf((String) object.get(BoatClassSerializer.FIELD_HULL_TYPE)));
        }

        return boatClassBuilder.build();
    }

}
