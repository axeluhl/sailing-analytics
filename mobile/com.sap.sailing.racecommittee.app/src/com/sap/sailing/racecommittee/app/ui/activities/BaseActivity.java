package com.sap.sailing.racecommittee.app.ui.activities;

import java.util.Date;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.RaceApplication;
import com.sap.sailing.racecommittee.app.data.InMemoryDataStore;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.services.sending.EventSendingService;
import com.sap.sailing.racecommittee.app.services.sending.EventSendingService.EventSendingBinder;
import com.sap.sailing.racecommittee.app.services.sending.EventSendingService.EventSendingServiceLogger;

/**
 * Base activity for all race committee cockpit activities enabling basic menu functionality.
 */
public abstract class BaseActivity extends LoggableActivity {
    
    private class EventSendingServiceConnection implements ServiceConnection, EventSendingServiceLogger {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            EventSendingBinder binder = (EventSendingBinder) service;
            sendingService = binder.getService();
            boundSendingService = true;
            sendingService.setEventSendingServiceLogger(this);
            updateSendingServiceInformation();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg) {
            boundSendingService = false;
        }

        @Override
        public void onEventSentSuccessful() {
            updateSendingServiceInformation();
        }

        @Override
        public void onEventSentFailed() {
            updateSendingServiceInformation();
        }
    }

    private static final String TAG = BaseActivity.class.getName();
    
    protected AppPreferences preferences;

    protected MenuItem menuItemLive;

    protected boolean boundSendingService = false;
    protected EventSendingService sendingService;
    private EventSendingServiceConnection sendingServiceConnection;
    
    private String sendingServiceStatus = "";
    
    public BaseActivity() {
        this.sendingServiceConnection = new EventSendingServiceConnection();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.preferences = AppPreferences.on(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        menuItemLive = menu.findItem(R.id.options_menu_live);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.options_menu_settings:
            ExLog.i(TAG, "Clicked SETTINGS.");
            fadeActivity(SettingsActivity.class, false);
            return true;
        case R.id.options_menu_reload:
            ExLog.i(TAG, "Clicked RESET.");
            InMemoryDataStore.INSTANCE.reset();
            return onReset();
        case R.id.options_menu_live:
            ExLog.i(TAG, "Clicked LIVE.");
            Toast.makeText(this, getLiveIconText(), Toast.LENGTH_LONG).show();
            return true;
        case R.id.options_menu_info:
            ExLog.i(TAG, "Clicked INFO.");
            fadeActivity(SystemInformationActivity.class, false);
            return true;
        case android.R.id.home:
            ExLog.i(TAG, "Clicked HOME.");
            return onHomeClicked();
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        updateSendingServiceInformation();
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ExLog.i(TAG, String.format("Back pressed on activity %s", this.getClass().getSimpleName()));
    }

    protected boolean onHomeClicked() {
        return false;
    }

    protected boolean onReset() {
        fadeActivity(LoginActivity.class, true);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        
        Intent intent = new Intent(this, EventSendingService.class);
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
        
        int errorCount = this.sendingService.getDelayedIntentsCount();
        if (errorCount > 0) {
            menuItemLive.setIcon(R.drawable.ic_menu_share_red);
            Date lastSuccessfulSend = this.sendingService.getLastSuccessfulSend();
            sendingServiceStatus = String.format("Currently %d events waiting to be sent.\nLast successful sent was at %s", 
                    errorCount, lastSuccessfulSend == null ? "never" : lastSuccessfulSend);
        } else {
            menuItemLive.setIcon(R.drawable.ic_menu_share_holo_light);
            sendingServiceStatus = String.format("Currently no event waiting to be sent.", errorCount);
        }
    }

    private String getLiveIconText() {
        return String.format("Connected to: %s\n%s", AppPreferences.on(this).getServerBaseURL(), sendingServiceStatus);
    }

    protected void fadeActivity(Class<?> activity, boolean newTopTask) {
        Intent intent = new Intent(getBaseContext(), activity);
        if (newTopTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        fadeActivity(intent);
    }

    protected void fadeActivity(Intent intent) {
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    
    public RaceApplication getRaceApplication() {
        return (RaceApplication) getApplication();
    }
}
