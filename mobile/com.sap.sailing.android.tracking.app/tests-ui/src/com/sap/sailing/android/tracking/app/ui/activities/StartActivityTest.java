package com.sap.sailing.android.tracking.app.ui.activities;

import android.app.Activity;
import android.test.ActivityUnitTestCase;
import android.view.ContextThemeWrapper;
import com.sap.sailing.android.tracking.app.R;

public class StartActivityTest extends ActivityUnitTestCase<StartActivity> {

    public StartActivityTest() {
        super(StartActivity.class);
    }

    public void setUp() throws Exception {
        super.setUp();

        ContextThemeWrapper context = new ContextThemeWrapper(getInstrumentation().getTargetContext(),
            R.style.AppTheme);
        setActivityContext(context);
    }

    public void testActivity() throws Exception {
        Activity activity = getActivity();

        assertNull(activity);
    }
}