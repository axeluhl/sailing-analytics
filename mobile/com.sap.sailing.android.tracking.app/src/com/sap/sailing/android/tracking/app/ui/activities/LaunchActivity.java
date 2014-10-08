package com.sap.sailing.android.tracking.app.ui.activities;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.services.TrackingService;
import com.sap.sailing.android.tracking.app.services.TrackingService.TrackingServiceBinder;

public class LaunchActivity extends BaseActivity {
    private static final String TAG = LaunchActivity.class.getName();

    private final TrackingServiceConnection trackingServiceConnection = new TrackingServiceConnection();
    private TrackingService trackingService;
    private boolean boundTrackingService = false;
    private TextView status;

    private class TrackingServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            TrackingServiceBinder binder = (TrackingServiceBinder) service;
            trackingService = binder.getService();
            trackingService.startTracking();
            boundTrackingService = true;
            status.setText(R.string.tracking);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg) {
            boundTrackingService = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launch_activity);
        status = (TextView) findViewById(R.id.status);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (servicesConnected()){
            bindService(new Intent(getApplicationContext(), TrackingService.class), trackingServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (boundTrackingService) {
            trackingService.stopTracking();
            status.setText(R.string.not_tracking);
            unbindService(trackingServiceConnection);
        }
    }
    
    private boolean servicesConnected() {
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        
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
         * @param dialog An error dialog
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
