package com.sap.sailing.android.shared.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class ActivityHelper {

    private Activity mActivity;

    private ActivityHelper(Activity activity) {
        mActivity = activity;
    }

    public static ActivityHelper with(Activity activity) {
        return new ActivityHelper(activity);
    }

    public void hideKeyboard() {
        View view = mActivity.getCurrentFocus();
        if (view != null) {
            InputMethodManager manager = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
