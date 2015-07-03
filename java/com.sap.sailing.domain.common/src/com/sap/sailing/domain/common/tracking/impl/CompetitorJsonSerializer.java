package com.sap.sailing.domain.common.tracking.impl;

/**
 * So far this only holds constants that are shared between the server and the android applications e.g. Tracking App.
 * Used to assemble a compatible JSON string by themselves, using the constants from here.
 * Might need to be updated with additional constants.
 */
public class CompetitorJsonSerializer {
    public static final String COMPETITOR_ID = "id";
    public static final String COMPETITOR_NAME = "name";
    public static final String COMPETITOR_SAIL_ID = "sailID";
    public static final String COMPETITOR_NATIONALITY = "nationality";
    public static final String COMPETITOR_COUNTRY_CODE = "countryCode";
}
