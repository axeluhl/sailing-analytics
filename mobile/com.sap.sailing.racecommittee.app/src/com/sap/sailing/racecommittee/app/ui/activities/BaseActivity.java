package com.sap.sailing.racecommittee.app.ui.activities;

import java.util.Date;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.services.sending.EventSendingService;
import com.sap.sailing.racecommittee.app.services.sending.EventSendingService.EventSendingBinder;
import com.sap.sailing.racecommittee.app.services.sending.EventSendingService.EventSendingServiceLogger;

/**
 * Base activity for all race committee cockpit activities enabling basic menu functionality.
 */
public abstract class BaseActivity extends Activity {
    
    private class EventSendingServiceConnection implements ServiceConnection, EventSendingServiceLogger {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            EventSendingBinder binder = (EventSendingBinder) service;
            sendingService = binder.getService();
            boundSendingService = true;
            sendingService.setEventSendingServiceLogger(this);
            updateLiveIcon();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg) {
            boundSendingService = false;
        }

        @Override
        public void onEventSentSuccessful() {
            updateLiveIcon();
        }

        @Override
        public void onEventSentFailed() {
            updateLiveIcon();
        }
    }

    protected MenuItem menuItemLive;

    boolean boundSendingService = false;
    
    EventSendingService sendingService;
    EventSendingServiceConnection sendingServiceConnection;
    
    String sendingServiceStatus = "";
    
    public BaseActivity() {
        this.sendingServiceConnection = new EventSendingServiceConnection();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        menuItemLive = menu.findItem(R.id.LiveIcon);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        /*
         * case R.id.SystemInfo: fadeActivity(InformationActivity.class); return true;
         */
        case R.id.OptionsSettings:
            fadeActivity(SettingsActivity.class);
            return true;
        case R.id.LiveIcon:
            Toast.makeText(this, getLiveIconText(), Toast.LENGTH_LONG).show();
            return true;
        case R.id.WindLog: // fadeActivity(WindActivity.class); return false;
        case android.R.id.home:
            return onHomeClicked();
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        updateLiveIcon();
        return super.onPrepareOptionsMenu(menu);
    }

    protected boolean onHomeClicked() {
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, EventSendingService.class);
        bindService(intent, sendingServiceConnection, Context.BIND_AUTO_CREATE);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        
        if (boundSendingService) {
            unbindService(sendingServiceConnection);
            boundSendingService = false;
        }
    }

    private void updateLiveIcon() {
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
        return String.format("Connected to: %s\n%s", AppConstants.getServerBaseURL(this), sendingServiceStatus);
    }

    protected void fadeActivity(Class<?> activity) {
        Intent intent = new Intent(getBaseContext(), activity);
        fadeActivity(intent);
    }

    protected void fadeActivity(Intent intent) {
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
