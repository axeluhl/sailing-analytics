/**
 * 
 */
package com.sap.sailing.android.shared.services.sending;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Informs {@link MessageSendingService} whenever connectivity is restored, so that it can start sending messages again.
 * 
 * Register in manifest:
 * 
 * <pre>
 * {@code
 * <receiver android:name="com.sap.sailing.android.shared.services.sending.ConnectivityChangedReceiver" >
 *   <intent-filter>
 *     <action android:name="android.net.conn.CONNECTIVITY_CHANGE" />
 *   </intent-filter>
 * </receiver>
 * }
 * </pre>
 */
public class ConnectivityChangedReceiver extends BroadcastReceiver {

    private final static String TAG = ConnectivityChangedReceiver.class.getName();

    /*
     * (non-Javadoc)
     * 
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cService = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cService.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            context.unregisterReceiver(this);
            context.startService(MessageSendingService.createSendDelayedIntent(context));
        }
    }
}
