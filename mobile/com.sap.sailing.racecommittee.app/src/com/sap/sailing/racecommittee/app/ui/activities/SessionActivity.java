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
    protected void onResume() {
        super.onResume();
        ExLog.i(TAG, String.format("Logging in from activity %s", this.getClass().getSimpleName()));
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
        ExLog.i(TAG, String.format("Logging out from activity %s", this.getClass().getSimpleName()));
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Sure?").setPositiveButton("Logout", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doLogout();
            }
        }).setNegativeButton("Cancel", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                
            }
        }).create();
        dialog.show();
        return true;
    }

    private void doLogout() {
        unloadAllRaces();
        fadeActivity(LoginActivity.class, true);
    }

    private void unloadAllRaces() {
        ExLog.i(TAG, "Issuing intent action clear races");
        Intent intent = new Intent(AppConstants.INTENT_ACTION_CLEAR_RACES);
        this.startService(intent);
    }
    
}
