package com.sap.sailing.racecommittee.app;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.sap.sailing.racecommittee.app.domain.coursedesign.BoatClassType;
import com.sap.sailing.racecommittee.app.domain.coursedesign.CourseLayouts;
import com.sap.sailing.racecommittee.app.domain.coursedesign.NumberOfRounds;
import com.sap.sailing.racecommittee.app.domain.coursedesign.TrapezoidCourseLayouts;
import com.sap.sailing.racecommittee.app.domain.coursedesign.WindWardLeeWardCourseLayouts;

public class AppPreferences {

    private final static String PREFERENCE_SERVICE_URL = "webserviceUrlPref";
    private final static String PREFERENCE_SENDING_ACTIVE = "sendingActivePref";
    private final static String PREFERENCE_WIND_BEARING = "windBearingPref";
    private final static String PREFERENCE_WIND_SPEED = "windSpeedPref";
    
    private final static String PREFERENCE_BOAT_CLASS = "boatClassPref";
    private final static String PREFERENCE_COURSE_LAYOUT = "courseLayoutPref";
    private final static String PREFERENCE_NUMBER_OF_ROUNDS = "numberOfRoundsPref";
    //private final static String PREFERENCE_TARGET_TIME = "targetTimePref";
    
    private final static String PREFERENCE_MAIL_RECIPIENT = "mailRecipientPreference";
    private final static String PREFERENCE_MANAGED_COURSE_AREAS = "courseAreasPref";
    
    public static BoatClassType getBoatClass(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String boatClass = sp.getString(PREFERENCE_BOAT_CLASS,  null);
        if(boatClass==null)
            return null;
        return BoatClassType.valueOf(boatClass);
    }
    
    public static void setBoatClass(Context context, BoatClassType boatClass) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String boatClassString = boatClass.name();
        sp.edit().putString(PREFERENCE_BOAT_CLASS, boatClassString).apply();
    }
    
    public static CourseLayouts getCourseLayout(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String courseLayout = sp.getString(PREFERENCE_COURSE_LAYOUT, null);
        if(courseLayout==null)
            return null;
        CourseLayouts storedCourseLayout;
        //FIXME this is not nice
        try{
            storedCourseLayout = TrapezoidCourseLayouts.valueOf(courseLayout);
        } catch (IllegalArgumentException iae){
            storedCourseLayout = WindWardLeeWardCourseLayouts.valueOf(courseLayout);
        }
        
        return storedCourseLayout;
    }
    
    public static void setCourseLayout(Context context, CourseLayouts courseLayout) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String courseLayoutString = courseLayout.name();
        sp.edit().putString(PREFERENCE_COURSE_LAYOUT, courseLayoutString).apply();
    }
    
    public static NumberOfRounds getNumberOfRounds(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String numberOfRounds = sp.getString(PREFERENCE_NUMBER_OF_ROUNDS, null);
        if(numberOfRounds==null)
            return null;
        return NumberOfRounds.valueOf(numberOfRounds);
    }
    
    public static void setNumberOfRounds(Context context, NumberOfRounds numberOfRounds) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String numberOfRoundsString = numberOfRounds.name();
        sp.edit().putString(PREFERENCE_NUMBER_OF_ROUNDS, numberOfRoundsString).apply();
    }
    
    /*public static TargetTime getTargetTime(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String targetTime = sp.getString(PREFERENCE_TARGET_TIME, null);
        if(targetTime==null)
            return null;
        return TargetTime.valueOf(targetTime);
    }
    
    public static void setTargetTime(Context context, TargetTime targetTime) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String targetTimeString = targetTime.name();
        sp.edit().putString(PREFERENCE_TARGET_TIME, targetTimeString).apply();
    }*/
    
    public static List<String> getManagedCourseAreaNames(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String value = sp.getString(PREFERENCE_MANAGED_COURSE_AREAS, "");
        value = value.replaceAll("\\s","");
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
