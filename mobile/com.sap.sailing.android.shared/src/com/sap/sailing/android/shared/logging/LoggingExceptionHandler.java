package com.sap.sailing.android.shared.logging;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;

import com.sap.sailing.android.shared.R;

import android.content.Context;
import android.util.Log;

public class LoggingExceptionHandler implements UncaughtExceptionHandler {

    private static final String TAG = LoggingExceptionHandler.class.getName();

    private UncaughtExceptionHandler defaultHandler;
    private final Context context;

    public LoggingExceptionHandler(UncaughtExceptionHandler defaultHandler, Context context) {
        this.defaultHandler = defaultHandler;
        this.context = context;
    }

    public void uncaughtException(Thread thread, Throwable ex) {
        FileLoggingTask task = new FileLoggingTask(context);
        if (task.tryStartFileLogging(context.getString(R.string.crash_file_name))) {
            task.log(String.format("%s - Exception occured on thread %s:", new Date(), thread.getId()));
            task.logException(ex);
        } else {
            Log.e(TAG, "Could not log uncaught exception to file.");
        }
        defaultHandler.uncaughtException(thread, ex);
    }

}