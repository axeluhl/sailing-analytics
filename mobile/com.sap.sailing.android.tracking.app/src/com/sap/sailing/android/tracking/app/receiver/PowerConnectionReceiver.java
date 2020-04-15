package com.sap.sailing.android.tracking.app.receiver;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.BuildConfig;
import com.sap.sailing.android.tracking.app.utils.AppPreferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PowerConnectionReceiver extends BroadcastReceiver {

    private final String TAG = PowerConnectionReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
            storeBatteryIsCharging(context, true);
        } else if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
            storeBatteryIsCharging(context, false);
        }
    }

    private void storeBatteryIsCharging(Context context, boolean batteryIsCharging) {
        if (BuildConfig.DEBUG) {
            ExLog.i(context, TAG, "Storing battery is charging: " + batteryIsCharging);
        }
        new AppPreferences(context).setBatteryIsCharging(batteryIsCharging);
    }
}
