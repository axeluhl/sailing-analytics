package com.sap.sailing.racecommittee.app.logging;

public class LogEvent {
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
}
