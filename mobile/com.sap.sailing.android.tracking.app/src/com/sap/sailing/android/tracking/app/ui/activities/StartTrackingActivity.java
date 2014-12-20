package com.sap.sailing.android.tracking.app.ui.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.services.TrackingService;

public class StartTrackingActivity extends BaseActivity {
    private static final String TAG = StartTrackingActivity.class.getName();
    private Button startTracking;
    private Button nextActivity;
    private boolean tracking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_tracking_activity);

        startTracking = (Button) findViewById(R.id.btnStartTracking);
        startTracking.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!googlePLayServicesAvailable()) {
                    return;
                }
                
                if (tracking == false){
                	Intent startTrackingIntent = new Intent(StartTrackingActivity.this, TrackingService.class);
                    getBaseContext().startService(startTrackingIntent);
                    startTracking.setText("Stop Tracking");
                } else {
                	Intent startTrackingIntent = new Intent(StartTrackingActivity.this, TrackingService.class);
                    getBaseContext().stopService(startTrackingIntent);
                    startTracking.setText("Start Tracking");
                }
            }
        });

        nextActivity = (Button) findViewById(R.id.btnNextActivity);
        nextActivity.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartTrackingActivity.this, StopTrackingActivity.class);
                fadeActivity(intent);
            }
        });
    }

    private boolean googlePLayServicesAvailable() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (ConnectionResult.SUCCESS == resultCode) {
            ExLog.i(this, TAG, getString(R.string.play_services_available));
            return true;
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), TAG);
            }
            return false;
        }
    }

    public static class ErrorDialogFragment extends DialogFragment {
        private Dialog dialog;

        public ErrorDialogFragment() {
            super();
            dialog = null;
        }

        /**
         * Set the dialog to display
         *
         * @param dialog
         *            An error dialog
         */
        public void setDialog(Dialog dialog) {
            this.dialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return dialog;
        }
    }
}
