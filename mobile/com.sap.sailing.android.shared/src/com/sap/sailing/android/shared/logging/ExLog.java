package com.sap.sailing.android.shared.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.EnumSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.content.Context;
import android.provider.Settings.Secure;
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

    // events 1-200: user triggered events
    public final static int LOGIN_BUTTON_POSITIVE = 1;
    public final static int LOGIN_BUTTON_NEGATIVE = 2;
    public final static int EVENT_SELECTED = 3;
    public final static int COURSE_SELECTED = 4;
    public final static int RACE_SELECTED_ELEMENT = 5;
    public final static int RACE_SELECTED_TITLE = 6;
    public final static int FLAG_XRAY_SET = 7;
    public final static int FLAG_XRAY_REMOVE = 8;
    public final static int FLAG_FIRST_SUBSTITUTE = 9;
    public final static int FLAG_BLUE_SET = 10;
    public final static int FLAG_BLUE_REMOVE = 11;
    public final static int FLAG_NOVEMBER = 12;
    public final static int RACE_START_PHASE_BUTTON_START_PHASE = 13;
    public final static int RACE_START_PHASE_BUTTON_AP = 14;
    public final static int RACE_START_PHASE_BUTTON_RESET_TIME = 15;
    public final static int RACE_SET_TIME = 16;
    public final static int RACE_RESET_TIME = 17;
    public final static int RACE_RUNNING_GENERAL_RECALL_YES = 18;
    public final static int RACE_RUNNING_GENERAL_RECALL_NO = 19;
    public final static int RACE_CHOOSE_START_MODE_PAPA = 20;
    public final static int RACE_CHOOSE_START_MODE_ZULU = 21;
    public final static int RACE_CHOOSE_START_MODE_BLACK = 22;
    public final static int RACE_CHOOSE_START_MODE_INDIA = 23;
    public final static int RACE_CHOOSE_ABORT_ALPHA = 24;
    public final static int RACE_CHOOSE_ABORT_HOTEL = 25;
    public final static int RACE_CHOOSE_ABORT_NONE = 26;
    public final static int FLAG_BLUE_SET_NO = 27;
    public final static int FLAG_BLUE_REMOVE_NO = 28;
    public final static int RACE_RESET_DIALOG_BUTTON = 29;
    public final static int RACE_RESET_YES = 30;
    public final static int RACE_RESET_NO = 31;
    // added 180612
    public final static int RACE_SET_RACE_RUNNING_COURSE = 32;
    public final static int RACE_SET_RACE_RUNNING_COURSE_FAIL = 33;
    public final static int RACE_SET_RACE_RUNNING_COURSE_REMINDER = 34;
    public final static int RACE_SET_RACE_RUNNING_COURSE_REMINDER_FAIL = 35;

    public final static int RACE_SET_TIME_BUTTON_AP = 36;
    public final static int RACE_CHOOSE_ABORT_AP = 37;
    public final static int RACE_CHOOSE_ABORT_NOVEMBER = 38;

    public final static int RACE_RUNNING_REMOVE_GOLF_YES = 39;
    public final static int RACE_RUNNING_REMOVE_GOLF_NO = 40;

    public final static int RACE_START_PHASE_BUTTON_PATHFINDER = 41;
    public final static int RACE_SET_PATHFINDER = 42;
    // added 110712
    public final static int FLAG_GOLF_REMOVED = 43;

    public final static int RACE_START_PHASE_BUTTON_LINEOPENINGTIME = 44;
    public final static int RACE_SET_GATELINE_OPENING_TIME = 45;

    // events > 200: implicitly triggered events
    public final static int STATUS_SET_XRAY = 201;
    public final static int STATUS_SET_BLUE = 202;
    public final static int STATUS_SET_XRAY_COUNTDOWN = 203;
    public final static int RACE_START_PHASE_FLAG_BLACK = 204;
    public final static int RACE_START_PHASE_FLAG_INDIA = 205;
    public final static int RACE_START_PHASE_FLAG_PAPA = 206;
    public final static int RACE_START_PHASE_FLAG_ZULU = 207;
    // added 110712
    public final static int STATUS_SET_GOLF = 208;
    public final static int STATUS_UNSET_GOLF = 209;

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

    public synchronized static void i(int eventID, String msg, Context context) {
        if (UNIQUE_ID == null) {
            UNIQUE_ID = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        }
        i("ID: " + String.valueOf(eventID), System.currentTimeMillis() + ":" + (UNIQUE_ID == null ? "" : UNIQUE_ID)
                + ":" + (msg == null ? "" : msg));
    }

    public static String UNIQUE_ID = null;

    public synchronized static void i(String tag, String msg) {
        getInstance().info(tag, msg);
    }

    public synchronized static void w(String tag, String msg) {
        getInstance().warning(tag, msg);
    }

    public synchronized static void e(String tag, String msg) {
        getInstance().error(tag, msg);
    }
    
    public synchronized static void ex(String tag, Exception e) {
        getInstance().exception(tag, e);
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

    private void info(String tag, String msg) {
        if (isTargetActive(Target.LOGCAT))
            Log.i(tag, msg);

        if (isTargetActive(Target.FILE))
            logToFile(LogLevel.INFORMATION, tag, msg);
    }

    private void warning(String tag, String msg) {
        if (isTargetActive(Target.LOGCAT))
            Log.w(tag, msg);

        if (isTargetActive(Target.FILE))
            logToFile(LogLevel.WARNING, tag, msg);
    }

    private void error(String tag, String msg) {
        if (isTargetActive(Target.LOGCAT))
            Log.e(tag, msg);

        if (isTargetActive(Target.FILE))
            logToFile(LogLevel.ERROR, tag, msg);
    }
    
    private void exception(String tag, Exception e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter stream = new PrintWriter(stringWriter);
        e.printStackTrace(stream);
        String msg = stringWriter.toString();
        
        if (isTargetActive(Target.LOGCAT))
            Log.e(tag, msg);

        if (isTargetActive(Target.FILE))
            logToFile(LogLevel.ERROR, tag, msg);
    }

    private boolean isTargetActive(Target logcat) {
        return activeLoggingTargets.contains(logcat);
    }

    private void logToFile(LogLevel level, String tag, String msg) {

        initializeFileLoggingTask();

        if (logToFile) {
            try {
                logBuffer.put(String.format(logMsgTemplate, new Date().toString(), level.toString(), tag, msg));
            } catch (InterruptedException ie) {
            }
        }
    }

    private void initializeFileLoggingTask() {
        if (fileLoggingTaskInitialized)
            return;

        fileLoggingTaskInitialized = true;
        logBuffer = new ArrayBlockingQueue<String>(LogBufferCapacity);
        fileTask = new FileLoggingTask(logBuffer);
        logToFile = fileTask.tryStartFileLogging();
        if (logToFile) {
            Log.i(TAG, String.format("Logging to file %s.", fileTask.getLogFilePath()));
            new Thread(fileTask).start();
        } else {
            Log.w(TAG, "Logging to file couldn't be enabled.");
        }
    }

}
