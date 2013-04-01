package com.sap.sailing.server.gateway.serialization.competitor.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class NationalityJsonSerializer implements JsonSerializer<Nationality> {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_THREE_LETTER_IOC_ACRONYM = "threeLetterIOCAcronym";
    public static final String FIELD_COUNTRY_TWO_LETTER_ISO_CODE = "countryTwoLetterISOCode";
    public static final String FIELD_COUNTRY_THREE_LETTER_IOC_CODE = "countryThreeLetterIOCCode";
    public static final String FIELD_COUNTRY_NAME = "countryName";
    public static final String FIELD_COUNTRY_ITU_CALL_PREFIX = "countryITUCallPrefix";
    public static final String FIELD_COUNTRY_UN_ISO_NUMERIC = "countryUNISONumeric";
    public static final String FIELD_COUNTRY_UN_VEHICLE = "countryUNVehicle";
    public static final String FIELD_COUNTRY_IANA_INTERNET = "countryIANAInternet";
    public static final String FIELD_COUNTRY_THREE_LETTER_ISO_CODE = "countryThreeLetterISOCode";

    @Override
    public JSONObject serialize(Nationality object) {
        JSONObject result = new JSONObject();
        result.put(FIELD_NAME, object.getName());
        result.put(FIELD_THREE_LETTER_IOC_ACRONYM, object.getThreeLetterIOCAcronym());
        result.put(FIELD_COUNTRY_TWO_LETTER_ISO_CODE, object.getCountryCode().getTwoLetterISOCode());
        result.put(FIELD_COUNTRY_THREE_LETTER_IOC_CODE, object.getCountryCode().getThreeLetterIOCCode());
        result.put(FIELD_COUNTRY_NAME, object.getCountryCode().getName());
        result.put(FIELD_COUNTRY_ITU_CALL_PREFIX, object.getCountryCode().getITUCallPrefix());
        result.put(FIELD_COUNTRY_UN_ISO_NUMERIC, object.getCountryCode().getUNISONumeric());
        result.put(FIELD_COUNTRY_UN_VEHICLE, object.getCountryCode().getUNVehicle());
        result.put(FIELD_COUNTRY_IANA_INTERNET, object.getCountryCode().getIANAInternet());
        result.put(FIELD_COUNTRY_THREE_LETTER_ISO_CODE, object.getCountryCode().getThreeLetterISOCode());
        return result;
    }

}
