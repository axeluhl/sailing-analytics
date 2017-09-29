package com.sap.sailing.domain.common.tracking.impl;

/**
 * Used to assemble a compatible JSON string by themselves, using the constants from here.The field identified by this
 * name tells the fully-qualified class name from which the value of the field identified by {@link #FIELD_ID} has been
 * created. When deserializing, the ID needs to be re-constructed as an instance of that type.
 */
public class CompetitorJsonConstants {
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_SHORT_NAME = "shortName";
    public static final String FIELD_SAIL_ID = "sailID";
    public static final String FIELD_NATIONALITY = "nationality";
    public static final String FIELD_COUNTRY_CODE = "countryCode";
    public static final String FIELD_BOAT_CLASS_NAME = "boatClassName";
    public static final String FIELD_COLOR = "color";
    public static final String FIELD_FLAG_IMAGE = "flagImage";

    public static final String FIELD_ID_TYPE = "idtype";
    public static final String FIELD_NATIONALITY_ISO2 = "nationalityISO2";
    public static final String FIELD_NATIONALITY_ISO3 = "nationalityISO3";
    public static final String FIELD_TEAM = "team";
    public static final String FIELD_BOAT = "boat";
    public static final String FIELD_BOAT_ID_TYPE = "boatidtype";
    public static final String FIELD_BOAT_ID = "boatId";
    public static final String FIELD_DISPLAY_COLOR = "displayColor";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_SEARCHTAG = "searchTag";
    public static final String FIELD_FLAG_IMAGE_URI = "flagImageUri";
    public static final String FIELD_TEAM_IMAGE_URI = "teamImageUri";
    public static final String FIELD_TIME_ON_TIME_FACTOR = "timeOnTimeFactor";
    public static final String FIELD_TIME_ON_DISTANCE_ALLOWANCE_IN_SECONDS_PER_NAUTICAL_MILE = "timeOnDistanceAllowanceInSecondsPerNauticalMile";
}
