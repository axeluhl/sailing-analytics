package com.sap.sailing.racecommittee.app;

import java.lang.ref.WeakReference;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;

import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.logging.LifecycleLogger;
import com.sap.sailing.racecommittee.app.utils.LoggingExceptionHandler;

/**
 * <p>Registers an additional exception handler for uncaught exception to have some crash logging.</p>
 * <p>Offers a static {@link StringContext} to handle i18n in ugly cases.</p>
 * <p>Sets the default preference values (if not set)</p> 
 */
public class RaceApplication extends Application {

    private final static String TAG = RaceApplication.class.getName();
    
    
    /**
     * Whenever you change a preference's type (e.g. from Integer to String) you need to bump
     * this version code to the appropriate app version (see AndroidManifest.xml).
     */
    private final static int LAST_VERSION_COMPATIBLE_WITH_PREFERENCES = 3;
    
    private static StringContext stringContext;
    
    public static StringContext getStringContext() {
        return stringContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ExLog.i(TAG, "Application is starting");
        
        Thread.setDefaultUncaughtExceptionHandler(new LoggingExceptionHandler(Thread
                .getDefaultUncaughtExceptionHandler()));
        
        LifecycleLogger.enableLifecycleLogging(AppConstants.ENABLE_LIFECYCLE_LOGGING);
        stringContext = new StringContext(new WeakReference<Context>(getApplicationContext()));

        setupPreferences();
    }
    
    private void setupPreferences() {
        clearPreferencesIfNeeded();
        PreferenceManager.setDefaultValues(this, R.xml.preference_general, false);
        PreferenceManager.setDefaultValues(this, R.xml.preference_racing_procedure, false);
        PreferenceManager.setDefaultValues(this, R.xml.preference_course_designer, false);
    }

    void clearPreferencesIfNeeded()
    {
        PackageInfo info = getPackageInfo(this);
        if(info == null || info.versionCode < LAST_VERSION_COMPATIBLE_WITH_PREFERENCES)
        {
            ExLog.i(TAG, "Clearing the preference cache.");
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.clear().commit();
        }
    }
    
    public static PackageInfo getPackageInfo(Application app) {
        try {
            return app.getPackageManager().getPackageInfo(app.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            return null;
        }
    }
    
    public static void restartApp(Context context) {
        Intent i = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(i);
    }

}
