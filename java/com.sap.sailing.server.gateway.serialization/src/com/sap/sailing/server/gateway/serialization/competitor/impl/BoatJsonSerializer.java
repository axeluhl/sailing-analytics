package com.sap.sailing.server.gateway.serialization.competitor.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class BoatJsonSerializer implements JsonSerializer<Boat> {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_SAIL_ID = "sailID";
    public static final String FIELD_BOAT_CLASS = "boatClass";
    
    private JsonSerializer<BoatClass> boatClassSerializer;
    
    public BoatJsonSerializer(JsonSerializer<BoatClass> boatClassSerializer) {
        this.boatClassSerializer = boatClassSerializer;
    }

    @Override
    public JSONObject serialize(Boat object) {
        JSONObject result = new JSONObject();
        result.put(FIELD_NAME, object.getName());
        result.put(FIELD_SAIL_ID, object.getSailID());
        result.put(FIELD_BOAT_CLASS, boatClassSerializer.serialize(object.getBoatClass()));
        return result;
    }

}
