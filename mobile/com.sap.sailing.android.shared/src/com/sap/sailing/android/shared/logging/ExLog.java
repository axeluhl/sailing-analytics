package com.sap.sailing.android.shared.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.EnumSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.content.Context;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;

/**
 * Simple wrapper for logging to various targets.
 *
 * Log by using {@link ExLog#i(String, String)}, {@link ExLog#w(String, String)} and {@link ExLog#e(String, String)}.
 * Activate a set of targets by using {@link ExLog#activate(Target)} or {@link ExLog#activate(EnumSet)}. Currently
 * available are logging to android.util.Log LogCat and logging to a file on SD-card.
 *
 * The file logging in very defensive in a way that opening and writing to the file is secured by various exception
 * handlers trying to silence all possible errors.
 *
 */
public class ExLog {
    private final static String TAG = "ExLogInternal";
    private static EnumSet<Target> activeLoggingTargets;

    static {
        activeLoggingTargets = EnumSet.of(Target.LOGCAT, Target.FILE);
    }

    public enum Target {
        NONE, LOGCAT, FILE
    }

    public synchronized static void activate(EnumSet<Target> targetSet) {
        activeLoggingTargets = targetSet;
    }

    public synchronized static void activate(Target target) {
        activate(EnumSet.of(target));
    }

    public synchronized static void i(Context context, int eventID, String msg) {
        if (UNIQUE_ID == null) {
            UNIQUE_ID = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        }
        i(context, "ID: " + String.valueOf(eventID), System.currentTimeMillis() + ":"
                + (UNIQUE_ID == null ? "" : UNIQUE_ID) + ":" + (msg == null ? "" : msg));
    }

    public static String UNIQUE_ID = null;

    public synchronized static void i(Context context, String tag, String msg) {
        getInstance().info(context, tag, msg);
    }

    public synchronized static void w(Context context, String tag, String msg) {
        getInstance().warning(context, tag, msg);
    }

    public synchronized static void e(Context context, String tag, String msg) {
        getInstance().error(context, tag, msg);
    }

    public synchronized static void ex(Context context, String tag, Exception e) {
        getInstance().exception(context, tag, e);
    }

    private final static String logMsgTemplate = "%s - %s - %s : %s";

    private enum LogLevel {
        INFORMATION, WARNING, ERROR, DISABLED
    }

    private static ExLog instance;

    private static ExLog getInstance() {
        if (instance == null) {
            instance = new ExLog();
        }
        return instance;
    }

    private static final int LogBufferCapacity = 20;
    private BlockingQueue<String> logBuffer;

    private FileLoggingTask fileTask;

    private boolean logToFile;
    private boolean fileLoggingTaskInitialized;

    public ExLog() {
        logToFile = false;
        fileLoggingTaskInitialized = false;
    }

    private void info(Context context, String tag, String msg) {
        msg = (msg == null) ? "" : msg;
        if (isTargetActive(Target.LOGCAT)) {
            Log.i(tag, msg);
        }

        if (isTargetActive(Target.FILE)) {
            logToFile(context, LogLevel.INFORMATION, tag, msg);
        }
    }

    private void warning(Context context, String tag, String msg) {
        msg = (msg == null) ? "" : msg;
        if (!TextUtils.isEmpty(msg)) {
            if (isTargetActive(Target.LOGCAT)) {
                Log.w(tag, msg);
            }

            if (isTargetActive(Target.FILE)) {
                logToFile(context, LogLevel.WARNING, tag, msg);
            }
        }
    }

    private void error(Context context, String tag, String msg) {
        msg = (msg == null) ? "" : msg;
        if (!TextUtils.isEmpty(msg)) {
            if (isTargetActive(Target.LOGCAT)) {
                Log.e(tag, msg);
            }

            if (isTargetActive(Target.FILE)) {
                logToFile(context, LogLevel.ERROR, tag, msg);
            }
        }
    }

    private void exception(Context context, String tag, Exception e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter stream = new PrintWriter(stringWriter);
        e.printStackTrace(stream);
        String msg = stringWriter.toString();

        if (isTargetActive(Target.LOGCAT))
            Log.e(tag, msg);

        if (isTargetActive(Target.FILE))
            logToFile(context, LogLevel.ERROR, tag, msg);
    }

    private boolean isTargetActive(Target logcat) {
        return activeLoggingTargets.contains(logcat);
    }

    private void logToFile(Context context, LogLevel level, String tag, String msg) {

        initializeFileLoggingTask(context);

        if (logToFile) {
            try {
                logBuffer.put(String.format(logMsgTemplate, new Date().toString(), level.toString(), tag, msg));
            } catch (InterruptedException ie) {
            }
        }
    }

    private void initializeFileLoggingTask(Context context) {
        if (fileLoggingTaskInitialized) {
            return;
        }

        fileLoggingTaskInitialized = true;
        logBuffer = new ArrayBlockingQueue<String>(LogBufferCapacity);
        fileTask = new FileLoggingTask(logBuffer, context);
        logToFile = fileTask.tryStartFileLogging();
        if (logToFile) {
            Log.i(TAG, String.format("Logging to file %s.", fileTask.getLogFilePath()));
            new Thread(fileTask).start();
        } else {
            Log.w(TAG, "Logging to file couldn't be enabled.");
        }
    }
}
