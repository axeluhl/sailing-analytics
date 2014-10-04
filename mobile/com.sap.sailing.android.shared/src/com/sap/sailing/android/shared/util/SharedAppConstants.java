package com.sap.sailing.android.shared.util;


import java.io.File;

import android.content.Context;
import android.os.Environment;

import com.sap.sailing.android.shared.R;

public class SharedAppConstants {    
    public static File getExternalApplicationFolder(Context context) {
        String appFolder = context.getResources().getString(R.string.app_folder);
        File dir = new File(Environment.getExternalStorageDirectory() + File.separator + appFolder);
        dir.mkdirs();
        return dir;
    }
    
    public static final int MESSAGE_RESEND_INTERVAL = 1000 * 30; //30 seconds
    
    // Sending Service
    public final static String URL = "url";
    public final static String PAYLOAD = "payload";
    public final static String CALLBACK_CLASS = "callback";
    public final static String CALLBACK_PAYLOAD = "callbackPayload"; // passed back to callback
    public final static String MESSAGE_ID = "messageId";
    public final static String INTENT_ACTION_SEND_SAVED_INTENTS = "com.sap.sailing.android.shared.action.sendSavedIntents";
    public final static String INTENT_ACTION_SEND_MESSAGE = "com.sap.sailing.android.shared.action.sendMessage";
}
