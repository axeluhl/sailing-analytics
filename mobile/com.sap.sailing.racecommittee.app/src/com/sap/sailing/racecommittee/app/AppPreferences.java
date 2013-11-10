package com.sap.sailing.racecommittee.app;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.util.Log;

import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.impl.RaceLogEventAuthorImpl;
import com.sap.sailing.racecommittee.app.domain.coursedesign.BoatClassType;
import com.sap.sailing.racecommittee.app.domain.coursedesign.CourseLayouts;
import com.sap.sailing.racecommittee.app.domain.coursedesign.NumberOfRounds;
import com.sap.sailing.racecommittee.app.domain.coursedesign.TrapezoidCourseLayouts;
import com.sap.sailing.racecommittee.app.domain.coursedesign.WindWardLeeWardCourseLayouts;

public class AppPreferences {
    
    private static String TAG = AppPreferences.class.getName();

    private final static String PREFERENCE_SERVICE_URL = "webserviceUrlPref";
    private final static String PREFERENCE_SENDING_ACTIVE = "sendingActivePref";
    private final static String PREFERENCE_AUTHOR_NAME = "authorName";
    private final static String PREFERENCE_AUTHOR_PRIORITY = "authorPriority";
    private final static String PREFERENCE_WIND_BEARING = "windBearingPref";
    private final static String PREFERENCE_WIND_SPEED = "windSpeedPref";
    
    private final static String PREFERENCE_BOAT_CLASS = "boatClassPref";
    private final static String PREFERENCE_COURSE_LAYOUT = "courseLayoutPref";
    private final static String PREFERENCE_NUMBER_OF_ROUNDS = "numberOfRoundsPref";
    //private final static String PREFERENCE_TARGET_TIME = "targetTimePref";
    
    private final static String PREFERENCE_MAIL_RECIPIENT = "mailRecipientPreference";
    private final static String PREFERENCE_MANAGED_COURSE_AREAS = "courseAreasPref";
    private final static String PREFERENCE_MIN_ROUNDS = "minRoundsPreference";
    private final static String PREFERENCE_MAX_ROUNDS = "maxRoundsPreference";
    private final static String PREFERENCE_DEFAULT_START_PROCEDURE_TYPE = "defaultStartProcedureType";
    
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
        String[] managedCourseAreas = value.split(",");
        for (int i = 0; i < managedCourseAreas.length; i++) {
            managedCourseAreas[i] = managedCourseAreas[i].trim();
        }
        return Arrays.asList(managedCourseAreas);
    }
    
    public static void setManagedCourseAreaNames(Context context, List<String> courseAreaNames) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        StringBuilder builder = new StringBuilder();
        for (String name : courseAreaNames) {
            builder.append(name);
            builder.append(",");
        }
        sp.edit().putString(PREFERENCE_MANAGED_COURSE_AREAS, builder.substring(0, builder.length() - 1)).commit();
    }
    
    public static double getWindBearingFromDirection(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        long windBearingAsLong = sp.getLong(PREFERENCE_WIND_BEARING, 0);
        return Double.longBitsToDouble(windBearingAsLong);
    }
    
    public static void setWindBearingFromDirection(Context context, double enteredWindBearing) {
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
    
    public static void setMailRecipient(Context context, String mail) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREFERENCE_MAIL_RECIPIENT, mail).commit();
    }
    
    public static void setAuthor(Context context, RaceLogEventAuthor author) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREFERENCE_AUTHOR_NAME, author.getName()).apply();
        sp.edit().putInt(PREFERENCE_AUTHOR_PRIORITY, author.getPriority()).apply();
    }
    
    public static RaceLogEventAuthor getAuthor(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String authorName = sp.getString(PREFERENCE_AUTHOR_NAME, "<anonymous>");
        int authorPriority = sp.getInt(PREFERENCE_AUTHOR_PRIORITY, 0);
        return new RaceLogEventAuthorImpl(authorName, authorPriority);
    }
    
    public static int getMaxRounds(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String maxRoundsStr = sp.getString(PREFERENCE_MAX_ROUNDS, "3");
        int maxRounds = 3;
        try {
             maxRounds = Integer.valueOf(maxRoundsStr);
        } catch (NumberFormatException e){
            Log.e(TAG, "Unable to parse maximum rounds setting to integer");
        }
        return maxRounds; 
    }
    
    public static void setMaxRounds(Context context, int maxRounds) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREFERENCE_MAX_ROUNDS, String.valueOf(maxRounds)).commit();
    }
    
    public static int getMinRounds(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String minRoundsStr = sp.getString(PREFERENCE_MIN_ROUNDS, "2");
        int minRounds = 2;
        try {
             minRounds = Integer.valueOf(minRoundsStr);
        } catch (NumberFormatException e){
            Log.e(TAG, "Unable to parse minimum rounds setting to integer");
        }
        return minRounds; 
    }
    
    public static void setMinRounds(Context context, int minRounds) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREFERENCE_MIN_ROUNDS, String.valueOf(minRounds)).commit();
    }
    
    public static RacingProcedureType getDefaultStartProcedureType(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String defaultStartProcedureType = sp.getString(PREFERENCE_DEFAULT_START_PROCEDURE_TYPE, "RRS26");
        RacingProcedureType type = RacingProcedureType.valueOf(defaultStartProcedureType);
        return type;
    }
    
    public static String getAndroidIdentifier(Context context) {
        return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
    }
    
}
