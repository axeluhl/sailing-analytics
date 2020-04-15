package com.sap.sailing.android.buoy.positioning.app.service;

import com.sap.sailing.android.buoy.positioning.app.R;
import com.sap.sailing.android.buoy.positioning.app.util.CheckinManager;
import com.sap.sailing.android.buoy.positioning.app.util.DatabaseHelper;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.CheckinData;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class MarkerService extends IntentService implements CheckinManager.DataChangedListner {
    private static String TAG = MarkerService.class.getName();

    public MarkerService() {
        super("MarkerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String url = intent.getStringExtra(getString(R.string.check_in_url_key));
        CheckinManager manager = new CheckinManager(url, getApplicationContext());
        manager.setDataChangedListner(this);
        manager.callServerAndGenerateCheckinData();
    }

    @Override
    public void handleData(CheckinData data) {
        if (data != null) {
            try {
                DatabaseHelper helper = DatabaseHelper.getInstance();
                helper.updateMarks(this, data);
            } catch (DatabaseHelper.GeneralDatabaseHelperException e) {
                Log.e(TAG, "Error trying to analyze mark checkin data", e);
            }
        } else {
            Log.e(TAG, "checkin data was null");
        }
    }

}
