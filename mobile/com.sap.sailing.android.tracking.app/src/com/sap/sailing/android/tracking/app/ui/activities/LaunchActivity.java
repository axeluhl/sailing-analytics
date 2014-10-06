package com.sap.sailing.android.tracking.app.ui.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;

import com.sap.sailing.android.shared.ui.activities.SendingServiceAwareActivity;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.services.TrackingService;
import com.sap.sailing.android.tracking.app.services.TrackingService.TrackingServiceBinder;

public class LaunchActivity extends SendingServiceAwareActivity {
    // private static final String TAG = LaunchActivity.class.getName();

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
        bindService(new Intent(getApplicationContext(), TrackingService.class), trackingServiceConnection,
                Context.BIND_AUTO_CREATE);
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

    @Override
    protected int getOptionsMenuResId() {
        return 0;
    }
}
