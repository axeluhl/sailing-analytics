package com.sap.sailing.racecommittee.app.ui.activities;

import java.io.Serializable;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.DialogFragmentButtonListener;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.LoginDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.CourseAreaListFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.EventListFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.CourseAreaSelectedListenerHost;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.EventSelectedListenerHost;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.ItemSelectedListener;

public class LoginActivity extends TwoPaneActivity implements EventSelectedListenerHost,
        CourseAreaSelectedListenerHost, DialogFragmentButtonListener {
    private final static String TAG = LoginActivity.class.getName();

    private LoginDialog loginDialog;
    private CourseArea selectedCourse;

    public LoginActivity() {
        this.loginDialog = new LoginDialog();
        this.selectedCourse = null;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.login_view);
        setProgressBarIndeterminateVisibility(false);

        // on first create add event list fragment
        if (savedInstanceState == null) {
            addEventListFragment();
        }
    }

    // Login -> startRaceActivity(selectedCourse);

    private void addEventListFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.leftContainer, new EventListFragment());
        transaction.setTransition(FragmentTransaction.TRANSIT_NONE);
        transaction.commit();
    }

    private void addCourseAreaListFragment(Serializable eventId) {
        Bundle args = new Bundle();
        args.putSerializable(AppConstants.EventIdTag, eventId);

        Fragment fragment = new CourseAreaListFragment();
        fragment.setArguments(args);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.animator.slide_in, R.animator.slide_out);
        transaction.replace(R.id.rightContainer, fragment);
        transaction.commit();
        ExLog.i("LoginActivity", "CourseFragment created.");
    }

    private ItemSelectedListener<EventBase> eventSelectionListener = new ItemSelectedListener<EventBase>() {

        public void itemSelected(Fragment sender, EventBase event) {
            Serializable eventId = event.getId();
            ExLog.i(ExLog.EVENT_SELECTED, eventId.toString(), getBaseContext());
            showCourseAreaListFragment(eventId);
        }
    };

    public ItemSelectedListener<EventBase> getEventSelectionListener() {
        return eventSelectionListener;
    }

    private void showCourseAreaListFragment(Serializable eventId) {
        Toast.makeText(LoginActivity.this, eventId.toString(), Toast.LENGTH_LONG).show();
        getRightLayout().setVisibility(View.VISIBLE);
        addCourseAreaListFragment(eventId);
    }

    private ItemSelectedListener<CourseArea> courseAreaSelectionListener = new ItemSelectedListener<CourseArea>() {

        public void itemSelected(Fragment sender, CourseArea courseArea) {
            ExLog.i(TAG, "Starting view for " + courseArea.getName());
            ExLog.i(ExLog.COURSE_SELECTED, courseArea.getName(), getBaseContext());
            selectCourseArea(courseArea);
        }
    };

    public ItemSelectedListener<CourseArea> getCourseAreaSelectionListener() {
        return courseAreaSelectionListener;
    }

    private void selectCourseArea(CourseArea courseArea) {
        selectedCourse = courseArea;
        loginDialog.show(getFragmentManager(), "LoginDialog");
    }

    public void onDialogNegativeButton() { /* nothing here... */
    }

    public void onDialogPositiveButton() {
        switch (loginDialog.getSelectedLoginType()) {
        case OFFICER:
            ExLog.i(TAG, "Communication with backend is active.");
            AppConstants.setSendingActive(this, true);
            break;
        case VIEWER:
            ExLog.i(TAG, "Communication with backend is inactive.");
            AppConstants.setSendingActive(this, false);
            break;
        default:
            Toast.makeText(this, "Invalid login type. Ignoring.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCourse == null) {
            Toast.makeText(this, "The selected course was lost.", Toast.LENGTH_LONG).show();
            ExLog.e(TAG, "Course reference was not set - cannot start racing activity.");
            return;
        }

        Toast.makeText(this, selectedCourse.getId().toString(), Toast.LENGTH_LONG).show();
        Intent message = new Intent(this, RacingActivity.class);
        message.putExtra(AppConstants.COURSE_AREA_UUID_KEY, selectedCourse.getId());
        fadeActivity(message);
    }

}
