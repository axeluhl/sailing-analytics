package com.sap.sailing.racecommittee.app.ui.activities;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.util.Date;

import com.sap.sailing.android.shared.data.http.UnauthorizedException;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.services.sending.MessageSendingService;
import com.sap.sailing.android.shared.services.sending.MessageSendingService.MessageSendingBinder;
import com.sap.sailing.android.shared.services.sending.MessageSendingService.MessageSendingServiceLogger;
import com.sap.sailing.android.shared.ui.activities.ResilientActivity;
import com.sap.sailing.android.shared.util.AuthCheckTask;
import com.sap.sailing.android.shared.util.BitmapHelper;
import com.sap.sailing.android.shared.util.PrefUtils;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public abstract class SendingServiceAwareActivity extends ResilientActivity
        implements AuthCheckTask.AuthCheckTaskListener {

    private static class MessageSendingServiceConnection implements ServiceConnection, MessageSendingServiceLogger {

        WeakReference<SendingServiceAwareActivity> mActivity;

        public MessageSendingServiceConnection(SendingServiceAwareActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            SendingServiceAwareActivity activity = mActivity.get();
            if (activity != null) {
                MessageSendingBinder binder = (MessageSendingBinder) service;
                activity.sendingService = binder.getService();
                activity.boundSendingService = true;
                activity.sendingService.setMessageSendingServiceLogger(this);
                activity.updateSendingServiceInformation();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName arg) {
            SendingServiceAwareActivity activity = mActivity.get();
            if (activity != null) {
                activity.boundSendingService = false;
            }
        }

        @Override
        public void onMessageSentSuccessful() {
            SendingServiceAwareActivity activity = mActivity.get();
            if (activity != null) {
                activity.updateSendingServiceInformation();
            }
        }

        @Override
        public void onMessageSentFailed() {
            SendingServiceAwareActivity activity = mActivity.get();
            if (activity != null) {
                activity.updateSendingServiceInformation();
            }
        }
    }

    private MenuItem menuItemLive;

    protected boolean boundSendingService = false;
    protected MessageSendingService sendingService;
    private MessageSendingServiceConnection sendingServiceConnection;

    private String sendingServiceStatus = "";

    private static String TAG = SendingServiceAwareActivity.class.getName();

    public SendingServiceAwareActivity() {
        sendingServiceConnection = new MessageSendingServiceConnection(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        Intent intent = new Intent(this, MessageSendingService.class);
        ExLog.i(this, TAG, "bindSendingService");
        bindService(intent, sendingServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();

        ExLog.i(this, TAG, "unbindSendingService");
        unbindService(sendingServiceConnection);
        boundSendingService = false;
    }

    protected void updateSendingServiceInformation() {
        if (menuItemLive == null) {
            ExLog.w(this, TAG, "updateSendingServiceInformation -> menuItemLive==null");
            return;
        }
        if (!boundSendingService) {
            ExLog.w(this, TAG, "updateSendingServiceInformation -> !boundSendingService");
            return;
        }

        int errorCount = this.sendingService.getDelayedIntentsCount();
        if (errorCount > 0) {
            ExLog.i(this, TAG, "updateSendingServiceInformation -> errorCount > 0");
            menuItemLive.setIcon(BitmapHelper.getTintedDrawable(this, R.drawable.ic_share_white_36dp,
                    ThemeHelper.getColor(this, R.attr.sap_red_1)));
            Date lastSuccessfulSend = this.sendingService.getLastSuccessfulSend();
            String statusText = getString(R.string.events_waiting_to_be_sent);
            sendingServiceStatus = String.format(statusText, errorCount,
                    lastSuccessfulSend == null ? getString(R.string.never) : lastSuccessfulSend);
        } else {
            ExLog.i(this, TAG, "updateSendingServiceInformation -> errorCount <= 0");
            menuItemLive.setIcon(R.drawable.ic_share_white_36dp);
            sendingServiceStatus = getString(R.string.no_event_to_be_sent);
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
            if (AppPreferences.on(this).getAccessToken() == null) {
                onException(null);
            } else {
                try {
                    AuthCheckTask task = new AuthCheckTask(this, AppPreferences.on(this).getServerBaseURL(), this);
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } catch (MalformedURLException e) {
                    ExLog.e(this, TAG,
                            "Error: Failed to perform check-in due to a MalformedURLException: " + e.getMessage());
                }
            }
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
        return getString(R.string.connected_to_wp,
                PrefUtils.getString(this, R.string.preference_server_url_key, R.string.preference_server_url_default),
                sendingServiceStatus, (boundSendingService ? "bound" : "unbound"));
    }

    @Override
    public void onResultReceived(Boolean authenticated) {
        Toast.makeText(this, getLiveIconText(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onException(Exception exception) {
        if (exception == null || exception instanceof UnauthorizedException) {
            Context context = this;
            if (getSupportActionBar() != null) {
                context = getSupportActionBar().getThemedContext();
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_AlertDialog);
            builder.setTitle(getString(R.string.sending_exception_title));
            builder.setMessage(getString(R.string.sending_exception_message));
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AppPreferences.on(SendingServiceAwareActivity.this).setAccessToken(null);
                    startActivity(new Intent(SendingServiceAwareActivity.this, PasswordActivity.class));
                    finish();
                }
            });
            builder.setNegativeButton(android.R.string.no, null);
            builder.show();
        } else {
            onResultReceived(true);
        }
    }
}
