package com.sap.sailing.android.tracking.app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.services.TrackingService;

public class StopTrackingActivity extends BaseActivity {
    
    private Button stopTracking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stop_tracking_activity);        
        stopTracking = (Button) findViewById(R.id.btnStopTracking);
        stopTracking.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent stopTracking = new Intent(StopTrackingActivity.this, TrackingService.class);
                getBaseContext().stopService(stopTracking);
            }
        });
    }
    
    
}
