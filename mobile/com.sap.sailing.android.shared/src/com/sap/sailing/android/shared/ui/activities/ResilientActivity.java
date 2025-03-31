package com.sap.sailing.android.shared.ui.activities;

import android.view.MenuItem;

import com.sap.sailing.android.shared.logging.ExLog;

public class ResilientActivity extends LoggableActivity {
    private static final String TAG = ResilientActivity.class.getName();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            ExLog.i(this, TAG, "Clicked HOME.");
            return onHomeClicked();
        }
        return super.onOptionsItemSelected(item);
    }

    protected boolean onHomeClicked() {
        return false;
    }
}
