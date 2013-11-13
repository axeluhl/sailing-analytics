package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class BoatJsonSerializer implements JsonSerializer<Boat> {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_SAIL_ID = "sailId";
    public static final String FIELD_BOAT_CLASS = "boatClass";
    
    private final JsonSerializer<BoatClass> boatClassJsonSerializer;
    
    public static BoatJsonSerializer create() {
    	return new BoatJsonSerializer(new BoatClassJsonSerializer());
    }

    public BoatJsonSerializer(JsonSerializer<BoatClass> boatClassJsonSerializer) {
        this.boatClassJsonSerializer = boatClassJsonSerializer;
    }
    
    @Override
    public JSONObject serialize(Boat boat) {
        JSONObject result = new JSONObject();
        result.put(FIELD_NAME, boat.getName());
        result.put(FIELD_SAIL_ID, boat.getSailID());
        if (boatClassJsonSerializer != null) {
            result.put(FIELD_BOAT_CLASS, boatClassJsonSerializer.serialize(boat.getBoatClass()));
        }
        
        return result;
    }
}
