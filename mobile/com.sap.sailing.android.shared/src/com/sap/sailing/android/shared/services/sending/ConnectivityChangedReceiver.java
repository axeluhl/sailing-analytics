/**
 * 
 */
package com.sap.sailing.android.shared.services.sending;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.SharedAppConstants;

public class ConnectivityChangedReceiver extends BroadcastReceiver {
    
    private final static String TAG = ConnectivityChangedReceiver.class.getName();

    /* (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkInfo networkInfo = (NetworkInfo)intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
        if (!networkInfo.isConnected())
            return;
        Intent sendSavedIntent = new Intent(SharedAppConstants.INTENT_ACTION_SEND_SAVED_INTENTS);
        context.startService(sendSavedIntent);

        disable(context);
    }

    /**
     * disables the Connectivity Changed Receiver as there is currently connectivity
     * @param context the context to work on
     */
    public static void disable(Context context) {
        ComponentName receiver = new ComponentName(context, ConnectivityChangedReceiver.class);
        PackageManager packageManager = context.getPackageManager();
        packageManager.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        ExLog.w(context, TAG, "Regained connectivity. ConnectivityChangedReceiver disabled");
    }

    /**
     * enables the Connectivity Changed Receiver to listen on connectivity changes as there is currently no connectivity
     * @param context the context to work on
     */
    public static void enable(Context context) {
        ComponentName receiver = new ComponentName(context, ConnectivityChangedReceiver.class);
        PackageManager packageManager = context.getPackageManager();
        packageManager.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        ExLog.w(context, TAG, "Connectivity lost. ConnectivityChangedReceiver enabled");
    }

}
