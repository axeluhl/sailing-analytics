package com.sap.sailing.android.shared.ui.activities;

import net.hockeyapp.android.CrashManager;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.sap.sailing.android.shared.BuildConfig;
import com.sap.sailing.android.shared.R;
import com.sap.sailing.android.shared.data.AbstractCheckinData;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.ui.fragments.AbstractHomeFragment;

public abstract class AbstractStartActivity extends CheckinDataActivity{

	private final static String TAG = AbstractStartActivity.class.getName();
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.sap_logo_64_sq);
            toolbar.setPadding(20, 0, 0, 0);
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
    }
	
	@Override
	public void onStart() {
    	super.onStart();

    	// get url if launched via url intent-filter
       
        Intent intent = getIntent();
        Uri uri = intent.getData();

		if (uri != null) {
			if (BuildConfig.DEBUG) {
				ExLog.i(this, TAG, "Matched URL, handling scanned or matched URL.");
			}
			
			getHomeFragment().handleScannedOrUrlMatchedUri(uri);
			
		}
        
        intent.setData(null);
	}
	
	@Override
    public void onCheckinDataAvailable(AbstractCheckinData data) {
        if(data != null)
        {
            getHomeFragment().displayUserConfirmationScreen(data);
        }
    }
	
	public abstract AbstractHomeFragment getHomeFragment();
	
	@Override
    protected void onResume() {
    	super.onResume();
    	checkForCrashes();
        //checkForUpdates();
        
    	int googleServicesResultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
    	if (googleServicesResultCode != ConnectionResult.SUCCESS)
    	{
    		Dialog dialog = GooglePlayServicesUtil.getErrorDialog(googleServicesResultCode, this, 0);
    		dialog.show();
    	}
    }
	
    /**
     * Hockeyapp integration method.
     */
	private void checkForCrashes() {
		CrashManager.register(this, "060ff0c8a907638e3b31d3146091c87b");
	}
}
