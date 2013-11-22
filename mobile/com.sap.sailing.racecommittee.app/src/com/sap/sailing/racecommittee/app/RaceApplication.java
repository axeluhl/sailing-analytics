package com.sap.sailing.racecommittee.app;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.ref.WeakReference;
import java.util.Date;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.logging.FileLoggingTask;
import com.sap.sailing.racecommittee.app.logging.LifecycleLogger;

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
        ExLog.i(TAG, "Application is starting");
        
        Thread.setDefaultUncaughtExceptionHandler(new LoggingExceptionHandler(Thread
                .getDefaultUncaughtExceptionHandler()));
        
        LifecycleLogger.enableLifecycleLogging(AppConstants.ENABLE_LIFECYCLE_LOGGING);
        stringContext = new StringContext(new WeakReference<Context>(getApplicationContext()));

        PreferenceManager.setDefaultValues(this, R.xml.preference_general, false);
        PreferenceManager.setDefaultValues(this, R.xml.preference_racing_procedure, false);
        PreferenceManager.setDefaultValues(this, R.xml.preference_course_designer, false);
    }

    private static class LoggingExceptionHandler implements UncaughtExceptionHandler {

        private UncaughtExceptionHandler defaultHandler;

        public LoggingExceptionHandler(UncaughtExceptionHandler defaultHandler) {
            this.defaultHandler = defaultHandler;
        }

        public void uncaughtException(Thread thread, Throwable ex) {
            FileLoggingTask task = new FileLoggingTask();
            if (task.tryStartFileLogging("sap_rc_crash_%s.txt")) {
                task.log(String.format("%s - Exception occured on thread %s:", new Date(), thread.getId()));
                task.logException(ex);
            } else {
                Log.e(TAG, "Could not log uncaught exception to file.");
            }
            defaultHandler.uncaughtException(thread, ex);
        }

    }    
    
    public static void restartApp(Context context) {
        Intent i = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(i);
    }

}
