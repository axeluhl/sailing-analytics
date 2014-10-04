package com.sap.sailing.racecommittee.app.utils;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;

import android.util.Log;

import com.sap.sailing.android.shared.logging.FileLoggingTask;

public class LoggingExceptionHandler implements UncaughtExceptionHandler {
    
    private static final String TAG = LoggingExceptionHandler.class.getName();

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