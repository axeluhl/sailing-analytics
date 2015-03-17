package com.sap.sailing.racecommittee.app.ui.activities;

import android.app.Activity;
import android.test.ActivityUnitTestCase;
import android.view.ContextThemeWrapper;
import com.sap.sailing.racecommittee.app.R;

public class LoginActivityTest extends ActivityUnitTestCase<LoginActivity> {

    public LoginActivityTest() {
        super(LoginActivity.class);
    }

    public void setUp() throws Exception {
        super.setUp();

        ContextThemeWrapper context = new ContextThemeWrapper(getInstrumentation().getTargetContext(), R.style.AppTheme_Dark);
        setActivityContext(context);
    }

    public void testActivity() throws Exception {
        Activity activity = getActivity();

        assertNull(activity);
    }
}