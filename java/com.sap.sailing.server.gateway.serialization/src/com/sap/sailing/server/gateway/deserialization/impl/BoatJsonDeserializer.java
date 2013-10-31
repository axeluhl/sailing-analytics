package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.BoatJsonSerializer;

public class BoatJsonDeserializer implements JsonDeserializer<Boat> {
    private final BoatClassJsonDeserializer boatClassDeserializer;
    
    public BoatJsonDeserializer(BoatClassJsonDeserializer boatClassDeserializer) {
        super();
        this.boatClassDeserializer = boatClassDeserializer;
    }

    public Boat deserialize(JSONObject object) throws JsonDeserializationException {
        String name = object.get(BoatJsonSerializer.FIELD_NAME).toString();
        String sailId = object.get(BoatJsonSerializer.FIELD_SAIL_ID).toString();
        BoatClass boatClass = null;
        if (boatClassDeserializer != null) {
            boatClass = boatClassDeserializer.deserialize(Helpers.getNestedObjectSafe(object, BoatJsonSerializer.FIELD_BOAT_CLASS));
        }
        return new BoatImpl(name, boatClass, sailId);
    }

}
