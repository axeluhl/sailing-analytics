package com.sap.sailing.android.tracking.app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.services.TransmittingService;
import com.sap.sailing.android.tracking.app.ui.fragments.HomeFragment;

public class StartActivity extends BaseActivity {
    
    private final static String TAG = StartActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_container);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            
            toolbar.setNavigationIcon(R.drawable.ic_launcher);
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(false);
        }

        replaceFragment(R.id.content_frame, new HomeFragment());
    }
    
    @Override
    protected void onStart() {
    	// TODO Auto-generated method stub
    	super.onStart();
        
        // start transmitting service that will henceforth run in the background
        
        Intent intent = new Intent(this, TransmittingService.class);
        intent.setAction(getString(R.string.transmitting_service_start));
        this.startService(intent);
    }
}
