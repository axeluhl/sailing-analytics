package com.sap.sailing.racecommittee.app;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;

import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.impl.RaceLogEventAuthorImpl;
import com.sap.sailing.racecommittee.app.domain.coursedesign.BoatClassType;
import com.sap.sailing.racecommittee.app.domain.coursedesign.CourseLayouts;
import com.sap.sailing.racecommittee.app.domain.coursedesign.NumberOfRounds;
import com.sap.sailing.racecommittee.app.domain.coursedesign.TrapezoidCourseLayouts;
import com.sap.sailing.racecommittee.app.domain.coursedesign.WindWardLeeWardCourseLayouts;

/**
 * Wrapper for {@link SharedPreferences} for all hidden and non-hidden preferences and state variables.
 */
public class AppPreferences {

    public static AppPreferences on(Context context) {
        return new AppPreferences(context);
    }

    private final static String HIDDEN_PREFERENCE_SENDING_ACTIVE = "sendingActivePref";

    private final static String HIDDEN_PREFERENCE_AUTHOR_NAME = "authorName";
    private final static String HIDDEN_PREFERENCE_AUTHOR_PRIORITY = "authorPriority";

    private final static String HIDDEN_PREFERENCE_WIND_BEARING = "windBearingPref";
    private final static String HIDDEN_PREFERENCE_WIND_SPEED = "windSpeedPref";

    private final static String HIDDEN_PREFERENCE_BOAT_CLASS = "boatClassPref";
    private final static String HIDDEN_PREFERENCE_COURSE_LAYOUT = "courseLayoutPref";
    private final static String HIDDEN_PREFERENCE_NUMBER_OF_ROUNDS = "numberOfRoundsPref";
    
    private final static String HIDDEN_PREFERENCE_PROCEDURE_CONFIG_OVERWRITE = "procedureConfigOverwriteAllowed";

    private final SharedPreferences preferences;
    private final Context context;

    public AppPreferences(Context context) {
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }
    
    private String key(int keyId) {
        return context.getString(keyId);
    }

    public String getDeviceIdentifier() {
        String identifier = preferences.getString(key(R.string.preference_identifier_key), "");
        return identifier.isEmpty() ? Secure.getString(context.getContentResolver(), Secure.ANDROID_ID) : identifier;
    }

    public boolean isSendingActive() {
        return preferences.getBoolean(HIDDEN_PREFERENCE_SENDING_ACTIVE, false);
    }

    public void setSendingActive(boolean activate) {
        preferences.edit().putBoolean(HIDDEN_PREFERENCE_SENDING_ACTIVE, activate).commit();
    }

    public void setAuthor(RaceLogEventAuthor author) {
        preferences.edit().putString(HIDDEN_PREFERENCE_AUTHOR_NAME, author.getName()).commit();
        preferences.edit().putInt(HIDDEN_PREFERENCE_AUTHOR_PRIORITY, author.getPriority()).commit();
    }

    public RaceLogEventAuthor getAuthor() {
        String authorName = preferences.getString(HIDDEN_PREFERENCE_AUTHOR_NAME, "<anonymous>");
        int authorPriority = preferences.getInt(HIDDEN_PREFERENCE_AUTHOR_PRIORITY, 0);
        return new RaceLogEventAuthorImpl(authorName, authorPriority);
    }

    public BoatClassType getBoatClass() {
        String boatClass = preferences.getString(HIDDEN_PREFERENCE_BOAT_CLASS, null);
        if (boatClass == null) {
            return null;
        }
        return BoatClassType.valueOf(boatClass);
    }

    public void setBoatClass(BoatClassType boatClass) {
        String boatClassString = boatClass.name();
        preferences.edit().putString(HIDDEN_PREFERENCE_BOAT_CLASS, boatClassString).commit();
    }

    public CourseLayouts getCourseLayout() {
        String courseLayout = preferences.getString(HIDDEN_PREFERENCE_COURSE_LAYOUT, null);
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
        preferences.edit().putString(HIDDEN_PREFERENCE_COURSE_LAYOUT, courseLayoutString).commit();
    }

    public NumberOfRounds getNumberOfRounds() {
        String numberOfRounds = preferences.getString(HIDDEN_PREFERENCE_NUMBER_OF_ROUNDS, null);
        if (numberOfRounds == null)
            return null;
        return NumberOfRounds.valueOf(numberOfRounds);
    }

    public void setNumberOfRounds(NumberOfRounds numberOfRounds) {
        String numberOfRoundsString = numberOfRounds.name();
        preferences.edit().putString(HIDDEN_PREFERENCE_NUMBER_OF_ROUNDS, numberOfRoundsString).commit();
    }

    public double getWindBearingFromDirection() {
        long windBearingAsLong = preferences.getLong(HIDDEN_PREFERENCE_WIND_BEARING, 0);
        return Double.longBitsToDouble(windBearingAsLong);
    }

    public void setWindBearingFromDirection(double enteredWindBearing) {
        long windBearingAsLong = Double.doubleToLongBits(enteredWindBearing);
        preferences.edit().putLong(HIDDEN_PREFERENCE_WIND_BEARING, windBearingAsLong).commit();
    }

    public double getWindSpeed() {
        long windSpeedAsLong = preferences.getLong(HIDDEN_PREFERENCE_WIND_SPEED, 0);
        return Double.longBitsToDouble(windSpeedAsLong);
    }

    public void setWindSpeed(double enteredWindSpeed) {
        long windSpeedAsLong = Double.doubleToLongBits(enteredWindSpeed);
        preferences.edit().putLong(HIDDEN_PREFERENCE_WIND_SPEED, windSpeedAsLong).commit();
    }

    public List<String> getManagedCourseAreaNames() {
        Set<String> values = preferences.getStringSet(key(R.string.preference_course_areas_key), new HashSet<String>());
        return new ArrayList<String>(values);
    }

    public void setManagedCourseAreaNames(List<String> courseAreaNames) {
        preferences
                .edit()
                .putStringSet(key(R.string.preference_course_areas_key),
                        new HashSet<String>(courseAreaNames)).commit();
    }

    public String getServerBaseURL() {
        String value = preferences.getString(key(R.string.preference_server_url_key), "");
        if (value.equals("")) {
            return "http://localhost:8889";
        }
        return value;
    }

    public String getMailRecipient() {
        return preferences.getString(key(R.string.preference_mail_key), "");
    }

    public void setMailRecipient(String mail) {
        preferences.edit().putString(key(R.string.preference_mail_key), mail).commit();
    }
    
    public boolean isDefaultRacingProcedureTypeOverridden() {
        return preferences.getBoolean(key(R.string.preference_racing_procedure_is_overridden_key), false);
    }
    
    public void setDefaultRacingProcedureTypeOverridden(boolean isOverridden) {
        preferences.edit().putBoolean(key(R.string.preference_racing_procedure_is_overridden_key), isOverridden).commit();
    }

    public RacingProcedureType getDefaultRacingProcedureType() {
        String defaultStartProcedureType = preferences.getString(key(R.string.preference_racing_procedure_override_key), "");
        return RacingProcedureType.valueOf(defaultStartProcedureType);
    }
    
    public void setDefaultRacingProcedureType(RacingProcedureType type) {
        preferences.edit().putString(key(R.string.preference_racing_procedure_override_key), type.name()).commit();
    }
    
    public boolean isDefaultCourseDesignerModeOverridden() {
        return preferences.getBoolean(key(R.string.preference_course_designer_is_overridden_key), false);
    }
    
    public void setDefaultCourseDesignerModeOverridden(boolean isOverridden) {
        preferences.edit().putBoolean(key(R.string.preference_course_designer_is_overridden_key), isOverridden).commit();
    }

    public CourseDesignerMode getDefaultCourseDesignerMode() {
        String mode = preferences.getString(key(R.string.preference_course_designer_override_key), "");
        return CourseDesignerMode.valueOf(mode);
    }
    
    public void setDefaultCourseDesignerMode(CourseDesignerMode mode) {
        preferences.edit().putString(key(R.string.preference_course_designer_override_key), mode.name()).commit();
    }

    public List<String> getByNameCourseDesignerCourseNames() {
        Set<String> values = preferences.getStringSet(key(R.string.preference_course_designer_by_name_course_names_key), new HashSet<String>());
        return new ArrayList<String>(values);
    }

    public void setByNameCourseDesignerCourseNames(List<String> courseNames) {
        preferences
                .edit()
                .putStringSet(key(R.string.preference_course_designer_by_name_course_names_key),
                        new HashSet<String>(courseNames)).commit();
    }

    public boolean isRacingProcedureConfigurationOverwriteAllowed() {
        return preferences.getBoolean(HIDDEN_PREFERENCE_PROCEDURE_CONFIG_OVERWRITE, true);
    }

    public void setRacingProcedureConfigurationOverwriteAllowed(boolean allowed) {
        preferences.edit().putBoolean(HIDDEN_PREFERENCE_PROCEDURE_CONFIG_OVERWRITE, allowed).commit();
    }
    
    public void setRacingProcedureClassFlag(RacingProcedureType type, Flags flag) {
        String key = getRacingProcedureClassFlagKey(type);
        preferences.edit().putString(key, flag.name()).commit();
    }
    
    public Flags getRacingProcedureClassFlag(RacingProcedureType type) {
        String key = getRacingProcedureClassFlagKey(type);
        return Flags.valueOf(preferences.getString(key, Flags.CLASS.name()));
    }
    
    public void setRacingProcedureHasIndividualRecall(RacingProcedureType type, Boolean hasRecall) {
        String key = getRacingProcedureHasIndividualRecallKey(type);
        preferences.edit().putBoolean(key, hasRecall).commit();
    }

    public boolean getRacingProcedureHasIndividualRecall(RacingProcedureType type) {
        String key = getRacingProcedureHasIndividualRecallKey(type);
        return preferences.getBoolean(key, false);
    }
    
    public void setRRS26StartmodeFlags(Set<Flags> flags) {
        Set<String> flagNames = new HashSet<String>();
        for (Flags flag : flags) {
            flagNames.add(flag.name());
        }
        preferences.edit().putStringSet(key(R.string.preference_racing_procedure_rrs26_startmode_flags_key), flagNames).commit();
    }
    
    public Set<Flags> getRRS26StartmodeFlags() {
        Set<String> flagNames = preferences.getStringSet(key(R.string.preference_racing_procedure_rrs26_startmode_flags_key), new HashSet<String>());
        if (flagNames != null) {
            Set<Flags> flags = new HashSet<Flags>();
            for (String flagName : flagNames) {
                flags.add(Flags.valueOf(flagName));
            }
            return flags;
        }
        return null;
    }
    
    public void setGateStartHasPathfinder(boolean hasPathfinder) {
        preferences.edit().putBoolean(key(R.string.preference_racing_procedure_gatestart_haspathfinder_key), hasPathfinder).commit();
    }
    
    public boolean getGateStartHasPathfinder() {
        return preferences.getBoolean(key(R.string.preference_racing_procedure_gatestart_haspathfinder_key), true);
    }

    public void setGateStartHasAdditionalGolfDownTime(boolean hasAdditionalGolfDownTime) {
        preferences.edit().putBoolean(key(R.string.preference_racing_procedure_gatestart_hasadditionalgolfdowntime_key), hasAdditionalGolfDownTime).commit();
    }

    public boolean getGateStartHasAdditionalGolfDownTime() {
        return preferences.getBoolean(key(R.string.preference_racing_procedure_gatestart_hasadditionalgolfdowntime_key), true);
    }

    private String getRacingProcedureClassFlagKey(RacingProcedureType type) {
        switch (type) {
        case RRS26:
            return key(R.string.preference_racing_procedure_rrs26_classflag_key);
        case GateStart:
            return key(R.string.preference_racing_procedure_gatestart_classflag_key);
        case ESS:
            return key(R.string.preference_racing_procedure_ess_classflag_key);
        case BASIC:
            return key(R.string.preference_racing_procedure_basic_classflag_key);
        default:
            throw new IllegalArgumentException("Unknown racing procedure type.");
        }
    }
    
    private String getRacingProcedureHasIndividualRecallKey(RacingProcedureType type) {
        switch (type) {
        case RRS26:
            return key(R.string.preference_racing_procedure_rrs26_hasxray_key);
        case GateStart:
            return key(R.string.preference_racing_procedure_gatestart_hasxray_key);
        case ESS:
            return key(R.string.preference_racing_procedure_ess_hasxray_key);
        case BASIC:
            return key(R.string.preference_racing_procedure_basic_hasxray_key);
        default:
            throw new IllegalArgumentException("Unknown racing procedure type.");
        }
    }
}
