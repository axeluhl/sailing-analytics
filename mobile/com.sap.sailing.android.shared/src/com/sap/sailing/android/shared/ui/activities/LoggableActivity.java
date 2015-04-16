package com.sap.sailing.android.shared.ui.activities;

import android.os.Bundle;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.logging.LifecycleLogger;

public abstract class LoggableActivity extends BaseActivity {
    private static final String TAG = LoggableActivity.class.getName();

    private LifecycleLogger lifeLogger;

    public LoggableActivity() {
        this.lifeLogger = new LifecycleLogger();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lifeLogger.onCreate(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        lifeLogger.onStart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        lifeLogger.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        lifeLogger.onPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        lifeLogger.onStop(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lifeLogger.onDestroy(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ExLog.i(this, TAG, String.format("Back pressed on activity %s", this.getClass().getSimpleName()));
    }
}
