package com.sap.sailing.android.shared.services.sending;

import android.content.Context;
import android.content.Intent;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.SharedAppConstants;

public class SendDelayedMessagesCaller implements Runnable {

    private final static String TAG = SendDelayedMessagesCaller.class.getName();
    private Context context;

    public SendDelayedMessagesCaller(Context context) {
        this.context = context;
    }

    public void run() {
        ExLog.i(TAG, "The Message Sending Service is called to send possibly delayed intents");
        Intent sendSavedIntent = new Intent(SharedAppConstants.INTENT_ACTION_SEND_SAVED_INTENTS);
        context.startService(sendSavedIntent);
    }
}
