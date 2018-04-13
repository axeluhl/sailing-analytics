package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class DetailedBoatClassJsonSerializer extends BoatClassJsonSerializer {

    public static final String DISPLAY_NAME = "displayName";
    public static final String HULL_LENGTH_IN_METERS = "hullLengthInMeters";
    public static final String HULL_BEAM_IN_METERS = "hullBeamInMeters";
    public static final String HULL_TYPE = "hullType";

    @Override
    public JSONObject serialize(BoatClass boatClass) {
        JSONObject result = super.serialize(boatClass);
        result.put(DISPLAY_NAME, boatClass.getDisplayName());
        result.put(HULL_LENGTH_IN_METERS, boatClass.getHullLength().getMeters());
        result.put(HULL_BEAM_IN_METERS, boatClass.getHullBeam().getMeters());
        result.put(HULL_TYPE, boatClass.getHullType().toString());
        return result;
    }

}
