package com.sap.sailing.racecommittee.app.ui.activities;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.test.ActivityInstrumentationTestCase2;
import com.sap.sailing.racecommittee.app.R;

public class LoginActivityTest extends ActivityInstrumentationTestCase2<LoginActivity> {

    public LoginActivityTest() {
        super(LoginActivity.class);
    }

    public void setUp() throws Exception {
        super.setUp();

        getActivity();
    }

    public void testActivity() throws Exception {
        Espresso.onView(ViewMatchers.withId(R.id.login_view_backdrop)).perform(ViewActions.click());
    }
}