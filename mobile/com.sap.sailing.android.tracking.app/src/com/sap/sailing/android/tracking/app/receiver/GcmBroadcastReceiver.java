package com.sap.sailing.android.tracking.app.receiver;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.sap.sailing.android.tracking.app.services.GcmIntentService;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName componentName = new ComponentName(context.getPackageName(), GcmIntentService.class.getName());
        startWakefulService(context, (intent.setComponent(componentName)));
        setResultCode(Activity.RESULT_OK);
    }
}
