package com.sap.sailing.android.tracking.app.ui.activities;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.test.ActivityInstrumentationTestCase2;
import com.sap.sailing.android.tracking.app.R;

public class StartActivityTest extends ActivityInstrumentationTestCase2<StartActivity> {

    public StartActivityTest() {
        super(StartActivity.class);
    }

    public void setUp() throws Exception {
        super.setUp();

        getActivity();
    }

    public void testActivity() throws Exception {
        Espresso.onView(ViewMatchers.withId(R.id.noQrCode)).perform(ViewActions.click());
    }
}