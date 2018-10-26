package com.sap.sailing.android.shared.ui.activities;

import com.sap.sailing.android.shared.logging.ExLog;

import android.content.Intent;
import android.view.MenuItem;

public class ResilientActivity extends LoggableActivity {
    private static final String TAG = ResilientActivity.class.getName();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            ExLog.i(this, TAG, "Clicked HOME.");
            return onHomeClicked();
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    protected boolean onHomeClicked() {
        return false;
    }

    protected void fadeActivity(Class<?> activity, boolean newTopTask) {
        Intent intent = new Intent(getBaseContext(), activity);
        if (newTopTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        fadeActivity(intent);
    }

    protected void fadeActivity(Intent intent) {
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
