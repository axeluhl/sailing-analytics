package com.sap.sailing.polars.jaxrs.serialization;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class BoatClassSerializer implements JsonSerializer<BoatClass> {

    public static final String FIELD_HULL_TYPE = "hullType";
    public static final String FIELD_HULL_BEAM = "hullBeam";
    public static final String FIELD_HULL_LENGTH = "hullLength";
    public static final String FIELD_DISPLAY_NAME = "displayName";
    public static final String FIELD_TYPICALLY_STARTS_UPWIND = "typicallyStartsUpwind";
    public static final String FIELD_NAME = "name";

    @Override
    public JSONObject serialize(BoatClass object) {
        JSONObject boatJSON = new JSONObject();

        boatJSON.put(FIELD_NAME, object.getName());
        boatJSON.put(FIELD_TYPICALLY_STARTS_UPWIND, object.typicallyStartsUpwind());
        boatJSON.put(FIELD_DISPLAY_NAME, object.getDisplayName());
        boatJSON.put(FIELD_HULL_LENGTH, object.getHullLength().getMeters());
        boatJSON.put(FIELD_HULL_BEAM, object.getHullBeam().getMeters());
        boatJSON.put(FIELD_HULL_TYPE, object.getHullType());

        return boatJSON;
    }

}
