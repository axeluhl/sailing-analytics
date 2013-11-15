package com.sap.sailing.racecommittee.app;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;

import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.impl.RaceLogEventAuthorImpl;
import com.sap.sailing.racecommittee.app.domain.coursedesign.BoatClassType;
import com.sap.sailing.racecommittee.app.domain.coursedesign.CourseLayouts;
import com.sap.sailing.racecommittee.app.domain.coursedesign.NumberOfRounds;
import com.sap.sailing.racecommittee.app.domain.coursedesign.TrapezoidCourseLayouts;
import com.sap.sailing.racecommittee.app.domain.coursedesign.WindWardLeeWardCourseLayouts;

public class AppPreferences {

    //private static String TAG = AppPreferences.class.getSimpleName();

    public static AppPreferences on(Context context) {
        return new AppPreferences(context);
    }

    private final static String PREFERENCE_SENDING_ACTIVE = "sendingActivePref";

    private final static String PREFERENCE_AUTHOR_NAME = "authorName";
    private final static String PREFERENCE_AUTHOR_PRIORITY = "authorPriority";

    private final static String PREFERENCE_WIND_BEARING = "windBearingPref";
    private final static String PREFERENCE_WIND_SPEED = "windSpeedPref";

    private final static String PREFERENCE_BOAT_CLASS = "boatClassPref";
    private final static String PREFERENCE_COURSE_LAYOUT = "courseLayoutPref";
    private final static String PREFERENCE_NUMBER_OF_ROUNDS = "numberOfRoundsPref";

    private final SharedPreferences preferences;
    private final Context context;

    public AppPreferences(Context context) {
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isSendingActive() {
        return preferences.getBoolean(PREFERENCE_SENDING_ACTIVE, false);
    }

    public void setSendingActive(boolean activate) {
        preferences.edit().putBoolean(PREFERENCE_SENDING_ACTIVE, activate).apply();
    }

    public void setAuthor(RaceLogEventAuthor author) {
        preferences.edit().putString(PREFERENCE_AUTHOR_NAME, author.getName()).apply();
        preferences.edit().putInt(PREFERENCE_AUTHOR_PRIORITY, author.getPriority()).apply();
    }

    public RaceLogEventAuthor getAuthor() {
        String authorName = preferences.getString(PREFERENCE_AUTHOR_NAME, "<anonymous>");
        int authorPriority = preferences.getInt(PREFERENCE_AUTHOR_PRIORITY, 0);
        return new RaceLogEventAuthorImpl(authorName, authorPriority);
    }

    public BoatClassType getBoatClass() {
        String boatClass = preferences.getString(PREFERENCE_BOAT_CLASS, null);
        if (boatClass == null) {
            return null;
        }
        return BoatClassType.valueOf(boatClass);
    }

    public void setBoatClass(BoatClassType boatClass) {
        String boatClassString = boatClass.name();
        preferences.edit().putString(PREFERENCE_BOAT_CLASS, boatClassString).apply();
    }

    public CourseLayouts getCourseLayout() {
        String courseLayout = preferences.getString(PREFERENCE_COURSE_LAYOUT, null);
        if (courseLayout == null)
            return null;
        CourseLayouts storedCourseLayout;
        // FIXME this is not nice
        try {
            storedCourseLayout = TrapezoidCourseLayouts.valueOf(courseLayout);
        } catch (IllegalArgumentException iae) {
            storedCourseLayout = WindWardLeeWardCourseLayouts.valueOf(courseLayout);
        }

        return storedCourseLayout;
    }

    public void setCourseLayout(CourseLayouts courseLayout) {
        String courseLayoutString = courseLayout.name();
        preferences.edit().putString(PREFERENCE_COURSE_LAYOUT, courseLayoutString).apply();
    }

    public NumberOfRounds getNumberOfRounds() {
        String numberOfRounds = preferences.getString(PREFERENCE_NUMBER_OF_ROUNDS, null);
        if (numberOfRounds == null)
            return null;
        return NumberOfRounds.valueOf(numberOfRounds);
    }

    public void setNumberOfRounds(NumberOfRounds numberOfRounds) {
        String numberOfRoundsString = numberOfRounds.name();
        preferences.edit().putString(PREFERENCE_NUMBER_OF_ROUNDS, numberOfRoundsString).apply();
    }

    public double getWindBearingFromDirection() {
        long windBearingAsLong = preferences.getLong(PREFERENCE_WIND_BEARING, 0);
        return Double.longBitsToDouble(windBearingAsLong);
    }

    public void setWindBearingFromDirection(double enteredWindBearing) {
        long windBearingAsLong = Double.doubleToLongBits(enteredWindBearing);
        preferences.edit().putLong(PREFERENCE_WIND_BEARING, windBearingAsLong).apply();
    }

    public double getWindSpeed() {
        long windSpeedAsLong = preferences.getLong(PREFERENCE_WIND_SPEED, 0);
        return Double.longBitsToDouble(windSpeedAsLong);
    }

    public void setWindSpeed(double enteredWindSpeed) {
        long windSpeedAsLong = Double.doubleToLongBits(enteredWindSpeed);
        preferences.edit().putLong(PREFERENCE_WIND_SPEED, windSpeedAsLong).apply();
    }

    public List<String> getManagedCourseAreaNames() {
        String value = preferences.getString(context.getString(R.string.preference_course_areas_key), "");
        String[] managedCourseAreas = value.split(",");
        for (int i = 0; i < managedCourseAreas.length; i++) {
            managedCourseAreas[i] = managedCourseAreas[i].trim();
        }
        return Arrays.asList(managedCourseAreas);
    }

    public void setManagedCourseAreaNames(List<String> courseAreaNames) {
        StringBuilder builder = new StringBuilder();
        for (String name : courseAreaNames) {
            builder.append(name);
            builder.append(",");
        }
        preferences
                .edit()
                .putString(context.getString(R.string.preference_course_areas_key),
                        builder.substring(0, builder.length() - 1)).commit();
    }

    public String getServerBaseURL() {
        String value = preferences.getString(context.getString(R.string.preference_server_url_key), "");
        if (value.equals("")) {
            return "http://localhost:8889";
        }
        return value;
    }

    public String getMailRecipient() {
        return preferences.getString(context.getString(R.string.preference_mail_key), "");
    }

    public void setMailRecipient(String mail) {
        preferences.edit().putString(context.getString(R.string.preference_mail_key), mail).commit();
    }
    
    public int getMinRounds() {
        return preferences.getInt(context.getString(R.string.preference_course_designer_by_name_min_rounds_key), 0);
    }

    public void setMinRounds(int minRounds) {
        preferences.edit().putInt(context.getString(R.string.preference_course_designer_by_name_min_rounds_key), minRounds).commit();
    }

    public int getMaxRounds() {
        return preferences.getInt(context.getString(R.string.preference_course_designer_by_name_max_rounds_key), 0);
    }

    public void setMaxRounds(int maxRounds) {
        preferences.edit().putInt(context.getString(R.string.preference_course_designer_by_name_max_rounds_key), maxRounds).commit();
    }

    public RacingProcedureType getDefaultStartProcedureType() {
        String defaultStartProcedureType = preferences.getString(
                context.getString(R.string.preference_racing_procedure_override_key), "");
        return RacingProcedureType.valueOf(defaultStartProcedureType);
    }

    public String getAndroidIdentifier() {
        return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
    }

}
