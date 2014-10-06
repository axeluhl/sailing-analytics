package com.sap.sailing.android.shared.services.sending;

import android.content.Context;
import android.content.Intent;

import com.sap.sailing.android.shared.R;
import com.sap.sailing.android.shared.logging.ExLog;

public class SendDelayedMessagesCaller implements Runnable {

    private final static String TAG = SendDelayedMessagesCaller.class.getName();
    private Context context;

    public SendDelayedMessagesCaller(Context context) {
        this.context = context;
    }

    public void run() {
        ExLog.i(context, TAG, "The Message Sending Service is called to send possibly delayed intents");
        Intent sendSavedIntent = new Intent(context.getString(R.string.intent_send_saved_intents));
        context.startService(sendSavedIntent);
    }
}
