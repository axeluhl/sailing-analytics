package com.sap.sailing.racecommittee.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.preference.PreferenceManager;

import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.RaceApplication;
import com.sap.sailing.racecommittee.app.logging.ExLog;

public class PreferenceHelper {
    
    private static final String TAG = PreferenceHelper.class.getName();
    
    /**
     * Whenever you change a preference's type (e.g. from Integer to String) you need to bump
     * this version code to the appropriate app version (see AndroidManifest.xml).
     */
    private final static int LAST_VERSION_COMPATIBLE_WITH_PREFERENCES = 3;

    
    private final Context context;
    
    public PreferenceHelper(Context context) {
        this.context = context;
    }
    
    public void setupPreferences() {
        setupPreferences(false);
    }
    
    public void setupPreferences(boolean forceReset) {
        clearPreferencesIfNeeded(forceReset);
        
        final SharedPreferences defaultValueSp = context.getSharedPreferences(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, 
                Context.MODE_PRIVATE);
        boolean hasSetDefault = defaultValueSp.getBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, false);
        PreferenceManager.setDefaultValues(context, R.xml.preference_course_designer, forceReset ||!hasSetDefault);
        PreferenceManager.setDefaultValues(context, R.xml.preference_general, forceReset ||!hasSetDefault);
        PreferenceManager.setDefaultValues(context, R.xml.preference_racing_procedure, forceReset ||!hasSetDefault);
    }

    void clearPreferencesIfNeeded(boolean forceReset)
    {
        PackageInfo info = RaceApplication.getPackageInfo(context);
        if(forceReset || info == null || info.versionCode < LAST_VERSION_COMPATIBLE_WITH_PREFERENCES)
        {
            ExLog.i(TAG, "Clearing the preference cache.");
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.clear().commit();
        }
    }
}
