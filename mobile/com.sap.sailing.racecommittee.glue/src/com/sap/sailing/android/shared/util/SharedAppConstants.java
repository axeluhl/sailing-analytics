package com.sap.sailing.android.shared.util;


import java.io.File;

import android.os.Environment;

public class SharedAppConstants {

    public static final boolean IS_DATA_OFFLINE = false;
    public static final boolean ENABLE_LIFECYCLE_LOGGING = false;
    private final static String ApplicationFolder = "/racecommittee";
    
    public static File getExternalApplicationFolder() {
        File dir = new File(Environment.getExternalStorageDirectory() + ApplicationFolder);
        dir.mkdirs();
        return dir;
    }
    
    public static final int MESSAGE_RESEND_INTERVAL = 1000 * 30; //30 seconds
    
    // Sending Service
    public final static String URL = "url";
    public final static String PAYLOAD = "payload";
    public final static String CALLBACK_CLASS = "callback";
    public final static String CALLBACK_PAYLOAD = "callbackPayload"; // passed back to callback
    public final static String MESSAGE_ID = "eventId";
    public final static String INTENT_ACTION_SEND_SAVED_INTENTS = "com.sap.sailing.android.shared.action.sendSavedIntents";
    public final static String INTENT_ACTION_SEND_MESSAGE = "com.sap.sailing.android.shared.action.sendMessage";
}
