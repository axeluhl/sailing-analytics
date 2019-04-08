package com.sap.sailing.server.gateway.serialization.impl;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.tracking.impl.CompetitorJsonConstants;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.common.Color;

public class BoatJsonSerializer implements JsonSerializer<Boat> {
    public static final String FIELD_ID = "id";
    public static final String FIELD_ID_TYPE = "idtype";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_SAIL_ID = "sailId";
    public static final String FIELD_BOAT_CLASS = "boatClass";
    public static final String FIELD_COLOR = "color";
    
    private final JsonSerializer<BoatClass> boatClassJsonSerializer;
    
    public static BoatJsonSerializer create() {
    	return new BoatJsonSerializer(new BoatClassJsonSerializer());
    }

    public BoatJsonSerializer(JsonSerializer<BoatClass> boatClassJsonSerializer) {
        this.boatClassJsonSerializer = boatClassJsonSerializer;
    }
    
    public static JSONObject getBoatIdQuery(Boat boat) {
        JSONObject result = new JSONObject();
        Serializable competitorId = boat.getId() instanceof UUID ? boat.getId().toString() : boat.getId();
        result.put(FIELD_ID, competitorId);
        return result;
    }

    @Override
    public JSONObject serialize(Boat boat) {
        JSONObject result = new JSONObject();
        // Special treatment for UUIDs. They are represented as String because JSON doesn't have a way to represent them otherwise.
        // However, other, e.g., numeric, types used to encode a serializable ID must be preserved according to JSON semantics.
        // Also see the corresponding case distinction in the deserialized which first tries to parse a string as a UUID before
        // returning the ID as is.
        result.put(CompetitorJsonConstants.FIELD_ID_TYPE, boat.getId().getClass().getName());
        Set<Entry<Object, Object>> entries = getBoatIdQuery(boat).entrySet();
        for (Entry<Object, Object> idKeyAndValue : entries) {
            result.put(idKeyAndValue.getKey(), idKeyAndValue.getValue());
        }
        result.put(FIELD_NAME, boat.getName());
        result.put(FIELD_SAIL_ID, boat.getSailID());
        Color color = boat.getColor();
        result.put(FIELD_COLOR, color != null ? color.getAsHtml() : null);
        if (boatClassJsonSerializer != null) {
            result.put(FIELD_BOAT_CLASS, boatClassJsonSerializer.serialize(boat.getBoatClass()));
        }
        return result;
    }
}
