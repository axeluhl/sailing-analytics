package com.sap.sailing.android.shared.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Wrapper for {@link SharedPreferences} for all hidden and non-hidden preferences and state variables.
 */
public class SharedAppPreferences {

    public static SharedAppPreferences on(Context context) {
        return new SharedAppPreferences(context);
    }
    
    public static SharedAppPreferences on(Context context, String preferenceName) {
        return new SharedAppPreferences(context, preferenceName);
    }

    private final static String HIDDEN_PREFERENCE_SENDING_ACTIVE = "sendingActivePref";

    protected final SharedPreferences preferences;
    protected final Context context;

    protected SharedAppPreferences(Context context) {
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }
    
    public SharedAppPreferences(Context context, String preferenceName) {
        this.context = context;
        this.preferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
    }

    protected String key(int keyId) {
        return context.getString(keyId);
    }

    public boolean isSendingActive() {
        return preferences.getBoolean(HIDDEN_PREFERENCE_SENDING_ACTIVE, false);
    }
    
    public void setSendingActive(boolean activate) {
        preferences.edit().putBoolean(HIDDEN_PREFERENCE_SENDING_ACTIVE, activate).commit();
    }
}
