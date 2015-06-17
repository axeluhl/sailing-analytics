package com.sap.sailing.racecommittee.app.ui.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.AppUtils;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationIdentifier;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationIdentifierImpl;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.BuildConfig;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.DataStore;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.domain.configuration.impl.PreferencesDeviceConfigurationLoader;
import com.sap.sailing.racecommittee.app.logging.LogEvent;
import com.sap.sailing.racecommittee.app.ui.fragments.LoginListViews;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.AttachedDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.DialogListenerHost;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.LoginDialog.LoginType;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.CourseAreaListFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.EventListFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.PositionListFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.CourseAreaSelectedListenerHost;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.EventSelectedListenerHost;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.ItemSelectedListener;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.PositionSelectedListenerHost;
import com.sap.sailing.racecommittee.app.utils.StringHelper;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;
import com.sap.sailing.racecommittee.app.utils.autoupdate.AutoUpdater;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.UUID;

public class LoginActivity extends BaseActivity
    implements EventSelectedListenerHost, CourseAreaSelectedListenerHost, PositionSelectedListenerHost, DialogListenerHost.DialogResultListener {

    private final static String CourseAreaListFragmentTag = "CourseAreaListFragmentTag";
    private final static String AreaPositionListFragmentTag = "AreaPositionListFragmentTag";

    private final static String TAG = LoginActivity.class.getName();

    private final int RQS_GooglePlayServices = 1;

    private final PositionListFragment positionFragment;
    private View backdrop;
    private LoginListViews loginListViews = null;

    private Button sign_in;

    // FIXME weird data redundancy by using different field for setting values makes everything so complex and buggy
    private String eventName = null;
    private String courseName = null;
    private String positionName = null;

    private Serializable mSelectedEventId;
    private UUID mSelectedCourseAreaUUID;

    private IntentReceiver mReceiver;

    private ProgressDialog progressDialog;

    private ReadonlyDataManager dataManager;

    private ItemSelectedListener<EventBase> eventSelectionListener = new ItemSelectedListener<EventBase>() {

        public void itemSelected(Fragment sender, EventBase event) {

            final Serializable eventId = selectEvent(event);

            //FIXME: its weird to have this button setup in here
            setupSignInButton();

            //prepare views after the event selection

            //close all currently open list views
            if (loginListViews != null) {
                loginListViews.closeAll();
            }
            addCourseAreaListFragment(eventId);

            //send intent to open the course area selection list
            Intent intent = new Intent(AppConstants.INTENT_ACTION_TOGGLE);
            intent.putExtra(AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_AREA);
            LocalBroadcastManager.getInstance(LoginActivity.this).sendBroadcast(intent);
        }
    };
    private ItemSelectedListener<CourseArea> courseAreaSelectionListener = new ItemSelectedListener<CourseArea>() {

        public void itemSelected(Fragment sender, CourseArea courseArea) {
            ExLog.i(LoginActivity.this, TAG, "Starting view for " + courseArea.getName());
            ExLog.i(LoginActivity.this, LogEvent.COURSE_SELECTED, courseArea.getName());

            selectCourseArea(courseArea);

            // prepare views after area selection

            // close all currently open list views
            if (loginListViews != null) {
                loginListViews.closeAll();
            }
            addAreaPositionListFragment();
            //send intent to open the position selection list
            Intent intent = new Intent(AppConstants.INTENT_ACTION_TOGGLE);
            intent.putExtra(AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_POSITION);
            LocalBroadcastManager.getInstance(LoginActivity.this).sendBroadcast(intent);
        }
    };

    public LoginActivity() {
        positionFragment = PositionListFragment.newInstance();
    }

    private void setupSignInButton() {
        sign_in = (Button) findViewById(R.id.login_submit);
        if (sign_in != null) {
            sign_in.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ExLog.i(LoginActivity.this, TAG, "Logged in: " + eventName + " - " + courseName + " - " + positionName);
                    login();
                }
            });
        }
    }

    private void login() {
        DataStore dataStore = dataManager.getDataStore();
        dataStore.setEventUUID(mSelectedEventId);
        dataStore.setCourseUUID(mSelectedCourseAreaUUID);
        switchToRacingActivity();
    }

    private void switchToRacingActivity(){
        Intent intent = new Intent(LoginActivity.this, RacingActivity.class);
        intent.putExtra(AppConstants.COURSE_AREA_UUID_KEY, mSelectedCourseAreaUUID);
        intent.putExtra(AppConstants.EventIdTag, mSelectedEventId);
        startActivity(intent);
    }

    private Serializable selectEvent(EventBase event) {
        final Serializable eventId = event.getId();
        eventName = event.getName();
        //TODO: explicitly set the header text of the fragment to this name
        selectEvent(eventId);
        loginListViews.getEventContainer().setHeaderText(eventName);
        return eventId;
    }

    // FIXME / DEPRECATED: only use the above setter for the whole event object
    @Deprecated
    private void selectEvent(Serializable eventId) {
        mSelectedEventId = eventId;
        ExLog.i(LoginActivity.this, LogEvent.EVENT_SELECTED, eventId.toString());
    }

    private void resetEvent() {
        mSelectedEventId = null;
        eventName = null;
        loginListViews.getEventContainer().setHeaderText("");
        resetCourseArea();
    }

    private boolean isEventSelected() {
        return (eventName != null && mSelectedEventId != null);
    }

    private void selectCourseArea(CourseArea courseArea) {
        courseName = courseArea.getName();
        mSelectedCourseAreaUUID = courseArea.getId();
        loginListViews.getAreaContainer().setHeaderText(courseName);
    }

    private boolean isCourseAreaSelected() {
        return (courseName != null && mSelectedCourseAreaUUID != null);
    }

    private void resetCourseArea() {
        courseName = null;
        mSelectedCourseAreaUUID = null;
        loginListViews.getAreaContainer().setHeaderText("");
        resetPosition();
    }

    private void selectPosition(LoginType type) {
        preferences.setLoginType(type);
        positionName = positionFragment.getAuthor().getName();
        String header = StringHelper.on(this).getAuthor(positionName);
        loginListViews.getPositionContainer().setHeaderText(header);
    }

    private boolean isPositionSelected() {
        return (positionName != null);
    }

    private void resetPosition() {
        positionName = null;
        loginListViews.getPositionContainer().setHeaderText("");
    }

    private void addAreaPositionListFragment() {
        resetPosition();
        updateSignInButtonState();
        if (getFragmentManager().findFragmentByTag(AreaPositionListFragmentTag) == null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.position_fragment, positionFragment, AreaPositionListFragmentTag);
            transaction.commitAllowingStateLoss();
        }
    }

    private void addCourseAreaListFragment(Serializable eventId) {
        resetCourseArea();
        updateSignInButtonState();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.area_fragment, CourseAreaListFragment.newInstance(eventId), CourseAreaListFragmentTag);
        transaction.commitAllowingStateLoss();
    }

    private void addEventListFragment() {
        resetEvent();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.event_fragment, EventListFragment.newInstance());
        transaction.commitAllowingStateLoss();

    }

    public ItemSelectedListener<CourseArea> getCourseAreaSelectionListener() {
        return courseAreaSelectionListener;
    }

    public ItemSelectedListener<EventBase> getEventSelectionListener() {
        return eventSelectionListener;
    }

    private void updateSignInButtonState() {
        if (sign_in == null) {
            return;
        }
        if (isValidForSignIn()) {
            sign_in.setEnabled(true);
        } else {
            sign_in.setEnabled(false);
        }
    }

    private boolean isValidForSignIn() {
        return isEventSelected() && isCourseAreaSelected() && isPositionSelected();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ExLog.i(this, TAG, "Starting Login: " + AppUtils.getBuildInfo(this));

        dataManager = DataManager.create(this);
        DataStore dataStore = dataManager.getDataStore();

        // Check if the user has been logged in before and if so bring him directly to the racing activity
        mSelectedCourseAreaUUID = dataStore.getCourseUUID();
        mSelectedEventId = dataStore.getEventUUID();
        if (mSelectedEventId != null && mSelectedCourseAreaUUID != null) {
            switchToRacingActivity();
        }

        ThemeHelper.setTheme(this);

        setContentView(R.layout.login_view);

        mReceiver = new IntentReceiver();

        // setup the login list views fragment
        loginListViews = new LoginListViews();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.login_listview, loginListViews).commitAllowingStateLoss();

        new AutoUpdater(this).notifyAfterUpdate();

        //setup the backdrop click listener
        backdrop = findViewById(R.id.login_view_backdrop);
        if (backdrop != null) {
            backdrop.setOnClickListener(new BackdropClick());
        }
    }

    @Override
    public void onPositionSelected(LoginType type) {

        //FIXME: this is some kind of exception handling
        /*if (mSelectedCourseAreaUUID == null) {
            String toastText = getString(R.string.selected_course_area_lost);
            Toast.makeText(LoginActivity.this, toastText, Toast.LENGTH_LONG).show();
            ExLog.e(LoginActivity.this, TAG, "Course area reference was not set - cannot start racing activity.");
            return;
        }*/

        selectPosition(type);
        // prepare views after position selected

        if (loginListViews != null) {
            loginListViews.closeAll();
        }
        updateSignInButtonState();
    }


    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.INTENT_ACTION_RESET);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);

        Intent intent = new Intent();
        intent.setAction(AppConstants.INTENT_ACTION_RESET);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        if (!BuildConfig.DEBUG) {
            if (resultCode != ConnectionResult.SUCCESS) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, RQS_GooglePlayServices).show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    @Override
    public void onStop() {
        super.onStop();
        dismissProgressDialog();
    }

    private void setupDataManager() {
        showProgressDialog();

        DeviceConfigurationIdentifier identifier = new DeviceConfigurationIdentifierImpl(AppPreferences.on(getApplicationContext())
            .getDeviceIdentifier());

        LoaderCallbacks<?> configurationLoader = dataManager.createConfigurationLoader(identifier, new LoadClient<DeviceConfiguration>() {

            @Override
            public void onLoadFailed(Exception reason) {
                dismissProgressDialog();
                if (reason instanceof FileNotFoundException) {
                    Toast.makeText(getApplicationContext(), getString(R.string.loading_configuration_not_found), Toast.LENGTH_LONG).show();
                    ExLog.w(LoginActivity.this, TAG, String.format("There seems to be no configuration for this device: %s", reason.toString()));
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.loading_configuration_failed), Toast.LENGTH_LONG).show();
                    ExLog.ex(LoginActivity.this, TAG, reason);
                }

                // Slide up, if events are available
                if (!dataManager.getDataStore().getEvents().isEmpty()) {
                    slideUpBackdropDelayed();
                }
            }

            @Override
            public void onLoadSucceeded(DeviceConfiguration configuration, boolean isCached) {
                dismissProgressDialog();

                // this is our 'global' configuration, let's store it in app preferences
                PreferencesDeviceConfigurationLoader.wrap(configuration, preferences).store();

                Toast.makeText(LoginActivity.this, getString(R.string.loading_configuration_succeded), Toast.LENGTH_LONG).show();
                // showCourseAreaListFragment(eventId);
                slideUpBackdropDelayed();
            }
        });

        if (!AppPreferences.on(this).isOfflineMode()) {
            // always reload the configuration...
            getLoaderManager().restartLoader(0, null, configurationLoader).forceLoad();
        } else {
            dismissProgressDialog();
        }
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.loading_configuration));
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
        }
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onDialogNegativeButton(AttachedDialogFragment dialog) {

    }

    @Override
    public void onDialogPositiveButton(AttachedDialogFragment dialog) {

    }

    private void slideUpBackdropDelayed() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                slideUpBackdrop();
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    private void slideUpBackdrop() {
        // don't slide up if already up
        if (backdrop.getY() != 0) {
            return;
        }
        long aniTime = getResources().getInteger(android.R.integer.config_longAnimTime);
        final View bottomView = findViewById(R.id.login_listview);

        View title = findViewById(R.id.backdrop_title);
        View subTitle = findViewById(R.id.backdrop_subtitle);
        View info = findViewById(R.id.technical_info);
        View settings = findViewById(R.id.settings_button);

        subTitle.setAlpha(0f);

        ObjectAnimator frameAnimation = ObjectAnimator.ofFloat(backdrop, "y", 0, -backdrop.getHeight() + (backdrop.getHeight() / 5));
        ObjectAnimator titleAnimation = ObjectAnimator.ofFloat(title, "alpha", 1f, 0f);
        ObjectAnimator subTitleAnimation = ObjectAnimator.ofFloat(subTitle, "alpha", 0f, 1f);
        ObjectAnimator infoAnimation = ObjectAnimator.ofFloat(info, "alpha", 0f, 1f);
        ObjectAnimator settingsAnimation = ObjectAnimator.ofFloat(settings, "alpha", 0f, 1f);

        ValueAnimator heightAnimation = ValueAnimator.ofInt(0, backdrop.getHeight() - (backdrop.getHeight() / 5));
        heightAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = bottomView.getLayoutParams();
                layoutParams.height = val;
                bottomView.setLayoutParams(layoutParams);
            }
        });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(heightAnimation, frameAnimation, titleAnimation, subTitleAnimation, infoAnimation, settingsAnimation);
        animatorSet.setDuration(aniTime);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }

    private class BackdropClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            slideUpBackdrop();
        }
    }

    private class IntentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            setupDataManager();

            addEventListFragment();

            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.area_fragment, new Fragment());
            transaction.replace(R.id.position_fragment, new Fragment());
            transaction.commit();
        }
    }
}
