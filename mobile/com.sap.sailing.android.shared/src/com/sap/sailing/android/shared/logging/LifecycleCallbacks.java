package com.sap.sailing.android.shared.logging;

/**
 * Stolen from API Level 14.
 */
public interface LifecycleCallbacks {
    void onCreate(Object activityOrFragment);

    void onStart(Object activityOrFragment);

    void onResume(Object activityOrFragment);

    void onPause(Object activityOrFragment);

    void onStop(Object activityOrFragment);

    void onSaveInstanceState(Object activityOrFragment);

    void onDestroy(Object activityOrFragment);
}
