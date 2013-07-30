package com.sap.sailing.racecommittee.app;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.ref.WeakReference;
import java.util.Date;

import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.logging.FileLoggingTask;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * Register a additional exception handler for uncaught exception to have some crash logging.
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
        
        stringContext = new StringContext(new WeakReference<Context>(getApplicationContext()));
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

}
