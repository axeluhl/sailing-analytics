package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.common.CountryCode;

public class CountryCodeJsonSerializer implements JsonSerializer<CountryCode> {
    public static final String TWO_LETTER_ISO_CODE = "twoLetterIsoCode";
    public static final String THREE_LETTER_IOC_CODE = "threeLetterIocCode";
    public static final String NAME = "name";
    public static final String ITU_CALL_PREFIX = "ituCallPrefix";
    public static final String UN_ISO_NUMERIC = "unIsoNumeric";
    public static final String UN_VEHICLE = "unVehicle";
    public static final String IANA_INTERNET = "ianaInternet";
    public static final String THREE_LETTER_ISO_CODE = "threeLetterIsoCode";

    public static CountryCodeJsonSerializer create() {
        return new CountryCodeJsonSerializer();
    }
    
    @Override
    public JSONObject serialize(CountryCode object) {
        final JSONObject result = new JSONObject();
        result.put(TWO_LETTER_ISO_CODE, object.getTwoLetterISOCode());
        result.put(THREE_LETTER_IOC_CODE, object.getThreeLetterIOCCode());
        result.put(NAME, object.getName());
        result.put(UN_ISO_NUMERIC, object.getUNISONumeric());
        result.put(UN_VEHICLE, object.getUNVehicle());
        result.put(IANA_INTERNET, object.getIANAInternet());
        result.put(THREE_LETTER_ISO_CODE, object.getThreeLetterISOCode());
        return result;
    }
}
