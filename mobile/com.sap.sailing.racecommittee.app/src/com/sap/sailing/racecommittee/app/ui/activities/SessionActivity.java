package com.sap.sailing.racecommittee.app.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;

import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.services.RaceStateService;
import com.sap.sailing.android.shared.logging.ExLog;

public abstract class SessionActivity extends BaseActivity {

    private static final String TAG = BaseActivity.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ExLog.i(this, TAG, String.format("Logging in from activity %s", this.getClass().getSimpleName()));
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
        ExLog.i(this, TAG, String.format("Logging out from activity %s", this.getClass().getSimpleName()));
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(getString(R.string.logout_dialog_title))
                .setMessage(getString(R.string.logout_dialog_message))
                .setPositiveButton(getString(R.string.logout), new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doLogout();
                    }
                }).setNegativeButton(getString(R.string.cancel), new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /* nothing here */
                    }
                }).create();
        dialog.show();
        return true;
    }

    private void doLogout() {
    	preferences.isSetUp(false);
        unloadAllRaces();
        fadeActivity(LoginActivity.class, true);
    }

    private void unloadAllRaces() {
        ExLog.i(this, TAG, "Issuing intent action clear races");
        Intent intent = new Intent(this, RaceStateService.class);
        intent.setAction(AppConstants.INTENT_ACTION_CLEAR_RACES);
        this.startService(intent);
    }

}
