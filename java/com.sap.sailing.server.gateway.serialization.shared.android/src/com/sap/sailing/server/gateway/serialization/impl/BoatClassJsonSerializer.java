package com.sap.sailing.server.gateway.serialization.impl;

import java.util.Arrays;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class BoatClassJsonSerializer implements JsonSerializer<BoatClass> {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_TYPICALLY_STARTS_UPWIND = "typicallyStartsUpwind";
    public static final String FIELD_HULL_LENGTH_IN_METERS = "hullLengthInMeters";
    public static final String FIELD_HULL_BEAM_IN_METERS = "hullBeamInMeters";
    public static final String FIELD_DISPLAY_NAME = "displayName";
    public static final String FIELD_ALIAS_NAMES = "aliasNames";
    public static final String FIELD_ICON_URL = "iconUrl";
    
    @Override
    public JSONObject serialize(BoatClass boatClass) {
        JSONObject result = new JSONObject();
        result.put(FIELD_NAME, boatClass.getName());
        result.put(FIELD_TYPICALLY_STARTS_UPWIND, boatClass.typicallyStartsUpwind());
        if (boatClass.getHullLength() != null) {
            result.put(FIELD_HULL_LENGTH_IN_METERS, boatClass.getHullLength().getMeters());
        }
        if (boatClass.getHullBeam() != null) {
            result.put(FIELD_HULL_BEAM_IN_METERS, boatClass.getHullBeam().getMeters());
        }
        final JSONArray aliasNames = new JSONArray();
        if (boatClass.getDisplayName() != null) {
            result.put(FIELD_DISPLAY_NAME, boatClass.getDisplayName());
            final BoatClassMasterdata masterData = BoatClassMasterdata.resolveBoatClass(boatClass.getDisplayName());
            if (masterData != null) {
                result.put(FIELD_ICON_URL, "/gwt/src/main/resources/com/sap/sailing/gwt/ui/client/images/boatclass/"+masterData.name().replaceAll("^_", "")+".png");
                aliasNames.addAll(Arrays.asList(masterData.getAlternativeNames()));
            }
        }
        result.put(FIELD_ALIAS_NAMES, aliasNames);
        return result;
    }
}
