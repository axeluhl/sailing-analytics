package com.sap.sailing.racecommittee.app.ui.activities;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.racecommittee.app.R;

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
        return logoutSession(); // only will call super.onReset() after user confirmation
    }

    public boolean logoutSession() {
        ExLog.i(this, TAG, String.format("Logging out from activity %s", this.getClass().getSimpleName()));
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
            .setTitle(getString(R.string.logout_dialog_title))
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
        ExLog.i(this, TAG, "Do logout now!");
        preferences.isSetUp(false); //FIXME: Is that flag really needed or just data redundancy?
        super.onReset(); // consistently resets the InMemoryDataStore and the RaceStateService
        fadeActivity(LoginActivity.class, true);
    }

}
