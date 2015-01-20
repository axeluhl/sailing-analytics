package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.FleetJsonSerializer;

public class FleetDeserializer implements JsonDeserializer<Fleet> {
    private JsonDeserializer<Color> colorDeserializer;

    public FleetDeserializer(JsonDeserializer<Color> colorDeserializer) {
        this.colorDeserializer = colorDeserializer;
    }

    public Fleet deserialize(JSONObject object) throws JsonDeserializationException {
        Color color = null;
        if (object.containsKey(FleetJsonSerializer.FIELD_COLOR)) {
            color = colorDeserializer.deserialize(Helpers.getNestedObjectSafe(object, FleetJsonSerializer.FIELD_COLOR));
        }
        String name = (String) object.get(FleetJsonSerializer.FIELD_NAME);
        Number ordering = (Number) object.get(FleetJsonSerializer.FIELD_ORDERING);

        return new FleetImpl(name, ordering.intValue(), color);
    }

}
