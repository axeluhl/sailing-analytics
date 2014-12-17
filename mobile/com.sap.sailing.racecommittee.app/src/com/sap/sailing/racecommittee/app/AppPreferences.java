package com.sap.sailing.racecommittee.app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;

import com.google.android.gms.maps.model.LatLng;
import com.sap.sailing.android.shared.data.EventData.Event;
import com.sap.sailing.android.shared.logging.ExLog;
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
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.LoginDialog.LoginType;

/**
 * Wrapper for {@link SharedPreferences} for all hidden and non-hidden preferences and state variables.
 */
public class AppPreferences {
    public interface PollingActiveChangedListener {
        void onPollingActiveChanged(boolean isActive);
    }

    private final static String HIDDEN_PREFERENCE_AUTHOR_NAME = "authorName";

    private final static String HIDDEN_PREFERENCE_AUTHOR_PRIORITY = "authorPriority";

    private final static String HIDDEN_PREFERENCE_BOAT_CLASS = "boatClassPref";
    private final static String HIDDEN_PREFERENCE_COURSE_LAYOUT = "courseLayoutPref";

    private final static String HIDDEN_PREFERENCE_NUMBER_OF_ROUNDS = "numberOfRoundsPref";

    private final static String HIDDEN_PREFERENCE_WIND_BEARING = "windBearingPref";

    private final static String HIDDEN_PREFERENCE_WIND_SPEED = "windSpeedPref";
    
    private final static String HIDDEN_PREFERENCE_WIND_LAT = "windLatPref";
    
    private final static String HIDDEN_PREFERENCE_WIND_LNG = "windLngPref";
    
    private final static String HIDDEN_PREFERENCE_COURSE_UUID_LEAST = "courseUUIDLeast";
    private final static String HIDDEN_PREFERENCE_COURSE_UUID_MOST = "courseUUIDMost";

    private final static String HIDDEN_PREFERENCE_EVENT_ID = "eventId";
    
    private final static String HIDDEN_PREFERENCE_IS_SET_UP = "isSetUp";
    
    private final static String HIDDEN_PREFERENCE_LOGIN_TYPE = "loginType";

    public static AppPreferences on(Context context) {
        return new AppPreferences(context);
    }

    public static AppPreferences on(Context context, String preferenceName) {
        return new AppPreferences(context, preferenceName);
    }
    protected final Context context;

    private OnSharedPreferenceChangeListener pollingActiveChangedListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key(R.string.preference_polling_active_key).equals(key)) {
                for (PollingActiveChangedListener listener : pollingActiveChangedListeners) {
                    listener.onPollingActiveChanged(isPollingActive());
                }
            }
        }
    };
    private Set<PollingActiveChangedListener> pollingActiveChangedListeners = new HashSet<AppPreferences.PollingActiveChangedListener>();

    protected final SharedPreferences preferences;
    protected AppPreferences(Context context) {
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }
    public AppPreferences(Context context, String preferenceName) {
        this.context = context;
        this.preferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
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

    public List<String> getByNameCourseDesignerCourseNames() {
        Set<String> values = preferences.getStringSet(
                key(R.string.preference_course_designer_by_name_course_names_key), new HashSet<String>());
        return new ArrayList<String>(values);
    }

    public Context getContext() {
        return context;
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

    public CourseDesignerMode getDefaultCourseDesignerMode() {
        String mode = preferences.getString(key(R.string.preference_course_designer_override_key), "");
        return CourseDesignerMode.valueOf(mode);
    }

    public RacingProcedureType getDefaultRacingProcedureType() {
        String defaultStartProcedureType = preferences.getString(
                key(R.string.preference_racing_procedure_override_key), "");
        return RacingProcedureType.valueOf(defaultStartProcedureType);
    }

    public String getDeviceIdentifier() {
        String identifier = preferences.getString(key(R.string.preference_identifier_key), "");
        return identifier.isEmpty() ? Secure.getString(context.getContentResolver(), Secure.ANDROID_ID) : identifier;
    }

    public boolean getGateStartHasAdditionalGolfDownTime() {
        return preferences.getBoolean(
                key(R.string.preference_racing_procedure_gatestart_hasadditionalgolfdowntime_key), true);
    }

    public boolean getGateStartHasPathfinder() {
        return preferences.getBoolean(key(R.string.preference_racing_procedure_gatestart_haspathfinder_key), true);
    }

	public LoginType getLoginType() {
		int type = preferences.getInt(HIDDEN_PREFERENCE_LOGIN_TYPE, -1);
		switch( type ){
			case 0:{
				return LoginType.NONE;
			}
			case 1:{
				return LoginType.VIEWER;
			}
			case 2:{
				return LoginType.OFFICER;
			}
			
			default:{
				return LoginType.NONE;
			}
		}
	}
    
    public String getMailRecipient() {
        return preferences.getString(key(R.string.preference_mail_key), "");
    }

    public List<String> getManagedCourseAreaNames() {
        Set<String> values = preferences.getStringSet(key(R.string.preference_course_areas_key), new HashSet<String>());
        return new ArrayList<String>(values);
    }

    public NumberOfRounds getNumberOfRounds() {
        String numberOfRounds = preferences.getString(HIDDEN_PREFERENCE_NUMBER_OF_ROUNDS, null);
        if (numberOfRounds == null)
            return null;
        return NumberOfRounds.valueOf(numberOfRounds);
    }

    /**
     * Gets polling interval in minutes
     */
    public int getPollingInterval() {
        return preferences.getInt(key(R.string.preference_polling_interval_key), 0);
    }

    public Flags getRacingProcedureClassFlag(RacingProcedureType type) {
        String key = getRacingProcedureClassFlagKey(type);
        return Flags.valueOf(preferences.getString(key, Flags.CLASS.name()));
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

    public boolean getRacingProcedureHasIndividualRecall(RacingProcedureType type) {
        String key = getRacingProcedureHasIndividualRecallKey(type);
        return preferences.getBoolean(key, false);
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

    public Set<Flags> getRRS26StartmodeFlags() {
        Set<String> flagNames = preferences.getStringSet(
                key(R.string.preference_racing_procedure_rrs26_startmode_flags_key), new HashSet<String>());
        if (flagNames != null) {
            Set<Flags> flags = new HashSet<Flags>();
            for (String flagName : flagNames) {
                flags.add(Flags.valueOf(flagName));
            }
            return flags;
        }
        return null;
    }

    public String getServerBaseURL() {
        String value = preferences.getString(key(R.string.preference_server_url_key), "");
        if (value.equals("")) {
            return "http://localhost:8889";
        }
        return value;
    }

    public double getWindBearingFromDirection() {
        long windBearingAsLong = preferences.getLong(HIDDEN_PREFERENCE_WIND_BEARING, 0);
        return Double.longBitsToDouble(windBearingAsLong);
    }

    public double getWindSpeed() {
        long windSpeedAsLong = preferences.getLong(HIDDEN_PREFERENCE_WIND_SPEED, 0);
        return Double.longBitsToDouble(windSpeedAsLong);
    }
    
    public LatLng getWindPosition(){
    	double lat = Double.longBitsToDouble(preferences.getLong(HIDDEN_PREFERENCE_WIND_LAT, 0));
    	double lng = Double.longBitsToDouble(preferences.getLong(HIDDEN_PREFERENCE_WIND_LNG, 0));
    	return new LatLng(lat,lng);
    }

    public UUID getCourseUUID(){
    	long least = preferences.getLong(HIDDEN_PREFERENCE_COURSE_UUID_LEAST, 0);
    	long most  = preferences.getLong(HIDDEN_PREFERENCE_COURSE_UUID_MOST, 0);
    	return new UUID(most,least);
    }
    
    public Serializable getEventID(){
    	String id = preferences.getString(HIDDEN_PREFERENCE_EVENT_ID, "");
    	if ( id != "" ){
    		return (Serializable) id;
    	}
    	return null;
    }
    
    public boolean isSetUp(){
    	return preferences.getBoolean(HIDDEN_PREFERENCE_IS_SET_UP, false);
    }
    
    public void isSetUp(boolean isIt){
    	preferences.edit().putBoolean(HIDDEN_PREFERENCE_IS_SET_UP, isIt).commit();
    }
    
    public boolean isPollingActive() {
        return preferences.getBoolean(key(R.string.preference_polling_active_key), false);
    }

    public boolean isSendingActive() {
        return preferences.getBoolean(context.getResources().getString(R.string.preference_isSendingActive_key),
                context.getResources().getBoolean(R.bool.preference_isSendingActive_default));
    }

    protected String key(int keyId) {
        return context.getString(keyId);
    }

    public void registerPollingActiveChangedListener(final PollingActiveChangedListener listener) {
        if (pollingActiveChangedListeners.isEmpty()) {
            preferences.registerOnSharedPreferenceChangeListener(pollingActiveChangedListener);
        }
        pollingActiveChangedListeners.add(listener);
    }

    public void setAuthor(RaceLogEventAuthor author) {
        preferences.edit().putString(HIDDEN_PREFERENCE_AUTHOR_NAME, author.getName()).commit();
        preferences.edit().putInt(HIDDEN_PREFERENCE_AUTHOR_PRIORITY, author.getPriority()).commit();
    }

    public void setBoatClass(BoatClassType boatClass) {
        String boatClassString = boatClass.name();
        preferences.edit().putString(HIDDEN_PREFERENCE_BOAT_CLASS, boatClassString).commit();
    }

    public void setByNameCourseDesignerCourseNames(List<String> courseNames) {
        preferences
                .edit()
                .putStringSet(key(R.string.preference_course_designer_by_name_course_names_key),
                        new HashSet<String>(courseNames)).commit();
    }

    public void setCourseLayout(CourseLayouts courseLayout) {
        String courseLayoutString = courseLayout.name();
        preferences.edit().putString(HIDDEN_PREFERENCE_COURSE_LAYOUT, courseLayoutString).commit();
    }

    public void setDefaultCourseDesignerMode(CourseDesignerMode mode) {
        preferences.edit().putString(key(R.string.preference_course_designer_override_key), mode.name()).commit();
    }

    public void setDefaultRacingProcedureType(RacingProcedureType type) {
        preferences.edit().putString(key(R.string.preference_racing_procedure_override_key), type.name()).commit();
    }

    public void setGateStartHasAdditionalGolfDownTime(boolean hasAdditionalGolfDownTime) {
        preferences
                .edit()
                .putBoolean(key(R.string.preference_racing_procedure_gatestart_hasadditionalgolfdowntime_key),
                        hasAdditionalGolfDownTime).commit();
    }

    public void setGateStartHasPathfinder(boolean hasPathfinder) {
        preferences.edit()
                .putBoolean(key(R.string.preference_racing_procedure_gatestart_haspathfinder_key), hasPathfinder)
                .commit();
    }

	public void setLoginType(LoginType type) {
		
		Editor setEdit = preferences.edit();
		
		switch( type ){
			case NONE:{
				setEdit.putInt(HIDDEN_PREFERENCE_LOGIN_TYPE, 0);
				break;
			}
			case VIEWER:{
				setEdit.putInt(HIDDEN_PREFERENCE_LOGIN_TYPE, 1);
				break;
			}
			case OFFICER:{
				setEdit.putInt(HIDDEN_PREFERENCE_LOGIN_TYPE, 2);
				break;
			}
			
			default:{
				break;
			}
		}
		
		setEdit.commit();
	}
    
    public void setMailRecipient(String mail) {
        preferences.edit().putString(key(R.string.preference_mail_key), mail).commit();
    }

    public void setManagedCourseAreaNames(List<String> courseAreaNames) {
        preferences.edit()
                .putStringSet(key(R.string.preference_course_areas_key), new HashSet<String>(courseAreaNames)).commit();
    }

    public void setNumberOfRounds(NumberOfRounds numberOfRounds) {
        String numberOfRoundsString = numberOfRounds.name();
        preferences.edit().putString(HIDDEN_PREFERENCE_NUMBER_OF_ROUNDS, numberOfRoundsString).commit();
    }

    public void setRacingProcedureClassFlag(RacingProcedureType type, Flags flag) {
        String key = getRacingProcedureClassFlagKey(type);
        preferences.edit().putString(key, flag.name()).commit();
    }

    public void setRacingProcedureHasIndividualRecall(RacingProcedureType type, Boolean hasRecall) {
        String key = getRacingProcedureHasIndividualRecallKey(type);
        preferences.edit().putBoolean(key, hasRecall).commit();
    }

    public void setRRS26StartmodeFlags(Set<Flags> flags) {
        Set<String> flagNames = new HashSet<String>();
        for (Flags flag : flags) {
            flagNames.add(flag.name());
        }
        preferences.edit().putStringSet(key(R.string.preference_racing_procedure_rrs26_startmode_flags_key), flagNames)
                .commit();
    }

    public void setSendingActive(boolean activate) {
        preferences.edit()
                .putBoolean(context.getResources().getString(R.string.preference_isSendingActive_key), activate)
                .commit();
    }

    public void setWindBearingFromDirection(double enteredWindBearing) {
        long windBearingAsLong = Double.doubleToLongBits(enteredWindBearing);
        preferences.edit().putLong(HIDDEN_PREFERENCE_WIND_BEARING, windBearingAsLong).commit();
    }

    public void setWindSpeed(double enteredWindSpeed) {
        long windSpeedAsLong = Double.doubleToLongBits(enteredWindSpeed);
        preferences.edit().putLong(HIDDEN_PREFERENCE_WIND_SPEED, windSpeedAsLong).commit();
    }

    public void setWindPosition(LatLng latLng) {
        long lat = Double.doubleToLongBits(latLng.latitude);
        long lng = Double.doubleToLongBits(latLng.longitude);
        preferences.edit()
        	.putLong(HIDDEN_PREFERENCE_WIND_LAT, lat)
        	.putLong(HIDDEN_PREFERENCE_WIND_LNG, lng)
        .commit();
    }
    
    
    public void setCourseUUID(UUID uuid){
    	long least = uuid.getLeastSignificantBits(); 
    	long most  = uuid.getMostSignificantBits();
    	
    	preferences.edit()
    		.putLong(HIDDEN_PREFERENCE_COURSE_UUID_LEAST, least)
    		.putLong(HIDDEN_PREFERENCE_COURSE_UUID_MOST, most)
    	.commit();
    }
    
    public void setEventID(Serializable id){
    	ExLog.i(getContext(), this.getClass().toString(), "Saving eventId: "+ id);
    	
    	
    	preferences.edit()
    		.putString(HIDDEN_PREFERENCE_EVENT_ID, id.toString())
    	.commit();
    	
    	ExLog.i(getContext(), this.getClass().toString(), "Loading eventId: "+ getEventID());
    }
    
    public void unregisterPollingActiveChangedListener(PollingActiveChangedListener listener) {
        pollingActiveChangedListeners.remove(listener);
        if (pollingActiveChangedListeners.isEmpty()) {
            preferences.unregisterOnSharedPreferenceChangeListener(pollingActiveChangedListener);
        }
    }

    public boolean wakelockEnabled() {
        return preferences.getBoolean(context.getResources().getString(R.string.preference_wakelock_key), context
                .getResources().getBoolean(R.bool.preference_wakelock_default));
    }


}
