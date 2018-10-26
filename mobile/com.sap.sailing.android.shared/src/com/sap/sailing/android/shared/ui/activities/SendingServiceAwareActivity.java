package com.sap.sailing.android.shared.ui.activities;

import java.util.Date;

import com.sap.sailing.android.shared.R;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.services.sending.MessageSendingService;
import com.sap.sailing.android.shared.services.sending.MessageSendingService.MessageSendingBinder;
import com.sap.sailing.android.shared.services.sending.MessageSendingService.MessageSendingServiceLogger;
import com.sap.sailing.android.shared.util.PrefUtils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public abstract class SendingServiceAwareActivity extends ResilientActivity {

    private class MessageSendingServiceConnection implements ServiceConnection, MessageSendingServiceLogger {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MessageSendingBinder binder = (MessageSendingBinder) service;
            sendingService = binder.getService();
            boundSendingService = true;
            sendingService.setMessageSendingServiceLogger(this);
            updateSendingServiceInformation();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg) {
            boundSendingService = false;
        }

        @Override
        public void onMessageSentSuccessful() {
            updateSendingServiceInformation();
        }

        @Override
        public void onMessageSentFailed() {
            updateSendingServiceInformation();
        }
    }

    private static final String TAG = SendingServiceAwareActivity.class.getName();

    protected MenuItem menuItemLive;
    protected int menuItemLiveId = -1;

    protected boolean boundSendingService = false;
    protected MessageSendingService sendingService;
    private MessageSendingServiceConnection sendingServiceConnection;

    private String sendingServiceStatus = "";

    public SendingServiceAwareActivity() {
        this.sendingServiceConnection = new MessageSendingServiceConnection();
    }

    @Override
    public void onStart() {
        super.onStart();

        Intent intent = new Intent(this, MessageSendingService.class);
        bindService(intent, sendingServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (boundSendingService) {
            unbindService(sendingServiceConnection);
            boundSendingService = false;
        }
    }

    protected void updateSendingServiceInformation() {
        if (menuItemLive == null)
            return;

        if (!boundSendingService)
            return;

        int errorCount = sendingService.getDelayedIntentsCount();
        if (errorCount > 0) {
            menuItemLive.setIcon(R.drawable.ic_menu_share_red);
            Date lastSuccessfulSend = this.sendingService.getLastSuccessfulSend();
            sendingServiceStatus = getString(R.string.sending_waiting, errorCount,
                    lastSuccessfulSend == null ? "never" : lastSuccessfulSend);
        } else {
            menuItemLive.setIcon(R.drawable.ic_menu_share);
            sendingServiceStatus = getString(R.string.sending_no_waiting);
        }
    }

    /**
     * @return the resource ID for the options menu, {@code 0} if none. The menu item displaying the connection status
     *         is added automatically.
     */
    protected abstract int getOptionsMenuResId();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu_live_status, menu);
        menuItemLive = menu.findItem(R.id.options_menu_live);
        if (getOptionsMenuResId() != 0) {
            inflater.inflate(getOptionsMenuResId(), menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.options_menu_live == item.getItemId()) {
            ExLog.i(this, TAG, "Clicked LIVE.");
            Toast.makeText(this, getLiveIconText(), Toast.LENGTH_LONG).show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        updateSendingServiceInformation();
        return super.onPrepareOptionsMenu(menu);
    }

    private String getLiveIconText() {
        return String.format("Connected to: %s\n%s",
                PrefUtils.getString(this, R.string.preference_server_url_key, R.string.preference_server_url_default),
                sendingServiceStatus);
    }
}
