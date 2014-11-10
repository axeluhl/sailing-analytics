package com.sap.sailing.android.tracking.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.widget.Toast;

public class PowerConnectionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
            checkPluggedState(context, intent);
        } else if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
            Toast.makeText(context, "Power disconnected", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Unknown action: " + action, Toast.LENGTH_LONG).show();
        }
    }

    private void checkPluggedState(Context context, Intent intent) {
        Intent chargingIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        final int pluggedState = chargingIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        final String msg;
        switch (pluggedState) {
        case 0:
            msg = "The device is running on battery";
            break;

        case BatteryManager.BATTERY_PLUGGED_AC:
            msg = "Plugged into AC charger";
            break;

        case BatteryManager.BATTERY_PLUGGED_USB:
            msg = "Plugged into USB charger";
            break;

        case BatteryManager.BATTERY_PLUGGED_WIRELESS:
            msg = "Plugged into wireless charger";
            break;

        default:
            msg = "Unknown state: " + pluggedState;
        }

        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
