package com.sap.sailing.racecommittee.app;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppPreferences {

    private final static String PREFERENCE_SERVICE_URL = "webserviceUrlPref";
    private final static String PREFERENCE_SENDING_ACTIVE = "sendingActivePref";
    private final static String PREFERENCE_WIND_BEARING = "windBearingPref";
    private final static String PREFERENCE_WIND_SPEED = "windSpeedPref";
    private final static String PREFERENCE_MAIL_RECIPIENT = "mailRecipientPreference";
    private final static String PREFERENCE_MANAGED_COURSE_AREAS = "courseAreasPref";
    
    public static List<String> getManagedCourseAreaNames(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String value = sp.getString(PREFERENCE_MANAGED_COURSE_AREAS, "");
        return Arrays.asList(value.split(","));
    }
    
    public static double getWindBearing(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        long windBearingAsLong = sp.getLong(PREFERENCE_WIND_BEARING, 0);
        return Double.longBitsToDouble(windBearingAsLong);
    }
    
    public static void setWindBearing(Context context, double enteredWindBearing) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        long windBearingAsLong = Double.doubleToLongBits(enteredWindBearing);
        sp.edit().putLong(PREFERENCE_WIND_BEARING, windBearingAsLong).apply();
    }
    
    public static double getWindSpeed(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        long windSpeedAsLong = sp.getLong(PREFERENCE_WIND_SPEED, 0);
        return Double.longBitsToDouble(windSpeedAsLong);
    }
    
    public static void setWindSpeed(Context context, double enteredWindSpeed) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        long windSpeedAsLong = Double.doubleToLongBits(enteredWindSpeed);
        sp.edit().putLong(PREFERENCE_WIND_SPEED, windSpeedAsLong).apply();
    }

    public static String getServerBaseURL(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if(sp.getString(PREFERENCE_SERVICE_URL, "").equals(""))
            return "http://localhost:8889";
        return sp.getString(PREFERENCE_SERVICE_URL, "http://192.168.56.1:8888");
    }

    public static boolean isSendingActive(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREFERENCE_SENDING_ACTIVE, false);
    }

    public static void setSendingActive(Context context, boolean activate) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREFERENCE_SENDING_ACTIVE, activate).apply();
    }
    
    public static String getMailRecipient(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREFERENCE_MAIL_RECIPIENT, context.getString(R.string.settings_advanced_mail_default));
    }
    
}
