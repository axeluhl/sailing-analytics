package com.sap.sailing.racecommittee.app.ui.activities;

import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.logging.ExLog;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

public class SessionActivity extends BaseActivity {

    private static final String TAG = BaseActivity.class.getName();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected boolean onHomeClicked() {
        return logoutSession();
    }
    
    @Override
    protected boolean onReset() {
        return logoutSession();
    }
    
    protected boolean logoutSession() {
        ExLog.i(TAG, String.format("Logging out of activity %s", this.getClass().getSimpleName()));
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Sure?").setPositiveButton("Logout", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logout();
            }
        }).setNegativeButton("Cancel", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                
            }
        }).create();
        dialog.show();
        return true;
    }

    private void logout() {
        unloadAllRaces();
        fadeActivity(LoginActivity.class, true);
    }

    private void unloadAllRaces() {
        ExLog.i(TAG, "Issuing intent action clear races");
        Intent intent = new Intent(AppConstants.INTENT_ACTION_CLEAR_RACES);
        this.startService(intent);
    }
    
}
