package com.sap.sailing.android.shared.services.sending;

import com.sap.sailing.android.shared.logging.ExLog;

import android.content.Context;

public class SendDelayedMessagesCaller implements Runnable {

    private final static String TAG = SendDelayedMessagesCaller.class.getName();
    private Context context;

    public SendDelayedMessagesCaller(Context context) {
        this.context = context;
    }

    public void run() {
        ExLog.i(context, TAG, "The Message Sending Service is called to send possibly delayed intents");
        context.startService(MessageSendingService.createSendDelayedIntent(context));
    }
}
