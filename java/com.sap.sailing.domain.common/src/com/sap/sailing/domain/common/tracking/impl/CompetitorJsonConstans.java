package com.sap.sailing.domain.common.tracking.impl;

/**
 * So far this only holds constants that are shared between the server and the android applications e.g. Tracking App.
 * Used to assemble a compatible JSON string by themselves, using the constants from here.
 * Might need to be updated with additional constants.
 */
public class CompetitorJsonConstans {
    public static final String COMPETITOR_ID = "id";
    public static final String COMPETITOR_NAME = "name";
    public static final String COMPETITOR_SAIL_ID = "sailID";
    public static final String COMPETITOR_NATIONALITY = "nationality";
    public static final String COMPETITOR_COUNTRY_CODE = "countryCode";
    public static final String COMPETITOR_BOAT_CLASS_NAME = "boatClassName";
    public static final String COMPETITOR_COLOR = "color";
    public static final String COMPETITOR_FLAG_IMAGE = "flagImage";
    
    /**
     * The field identified by this name tells the fully-qualified class name from which the value of the field identified
     * by {@link #FIELD_ID} has been created. When deserializing, the ID needs to be re-constructed as an instance of that
     * type.
     */
    public static final String FIELD_ID_TYPE = "idtype";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_SAILID = "sailID";
    public static final String FIELD_NATIONALITY = "nationality";
    public static final String FIELD_NATIONALITY_ISO2 = "nationalityISO2";
    public static final String FIELD_NATIONALITY_ISO3 = "nationalityISO3";
    public static final String FIELD_TEAM = "team";
    public static final String FIELD_BOAT = "boat";
    public static final String FIELD_DISPLAY_COLOR = "displayColor";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_FLAG_IMAGE_URI = "flagImageUri";
    public static final String FIELD_TIME_ON_TIME_FACTOR = "timeOnTimeFactor";
    public static final String FIELD_TIME_ON_DISTANCE_ALLOWANCE_IN_SECONDS_PER_NAUTICAL_MILE = "timeOnDistanceAllowanceInSecondsPerNauticalMile";
}
