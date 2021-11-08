package com.sap.sailing.android.shared.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefUtils {
    private static String key;
    private static SharedPreferences prefs;
    private static Context lastContext;

    private static void setup(Context context, int keyResId) {
        Context appContext = context.getApplicationContext();
        if (appContext != lastContext) {
            lastContext = appContext;
            prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        }
        key = appContext.getString(keyResId);
    }

    public static String getString(Context context, int keyResId, int defaultResId) {
        setup(context, keyResId);
        String defVal = context.getString(defaultResId);
        return prefs.getString(key, defVal);
    }

    public static boolean getBoolean(Context context, int keyResId, int defaultResId) {
        setup(context, keyResId);
        boolean defVal = context.getResources().getBoolean(defaultResId);
        return prefs.getBoolean(key, defVal);
    }

    public static int getInt(Context context, int keyResId, int defaultResId) {
        setup(context, keyResId);
        int defVal = context.getResources().getInteger(defaultResId);
        return prefs.getInt(key, defVal);
    }
}
