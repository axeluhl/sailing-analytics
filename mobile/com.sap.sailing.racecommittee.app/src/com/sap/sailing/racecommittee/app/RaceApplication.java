package com.sap.sailing.racecommittee.app;

import java.lang.ref.WeakReference;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.logging.LifecycleLogger;
import com.sap.sailing.racecommittee.app.utils.LoggingExceptionHandler;
import com.sap.sailing.racecommittee.app.utils.PreferenceHelper;

/**
 * <p>Registers an additional exception handler for uncaught exception to have some crash logging.</p>
 * <p>Offers a static {@link StringContext} to handle i18n in ugly cases.</p>
 * <p>Sets the default preference values (if not set)</p> 
 */
public class RaceApplication extends Application {

    private final static String TAG = RaceApplication.class.getName();
    
    private static StringContext stringContext;
    
    public static StringContext getStringContext() {
        return stringContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ExLog.i(this, TAG, "Application is starting.");
        
        Thread.setDefaultUncaughtExceptionHandler(new LoggingExceptionHandler(Thread
                .getDefaultUncaughtExceptionHandler(), this));
        
        LifecycleLogger.enableLifecycleLogging(AppConstants.ENABLE_LIFECYCLE_LOGGING);
        stringContext = new StringContext(new WeakReference<Context>(getApplicationContext()));

        new PreferenceHelper(this).setupPreferences();
    }
    
    public static PackageInfo getPackageInfo(Context app) {
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
