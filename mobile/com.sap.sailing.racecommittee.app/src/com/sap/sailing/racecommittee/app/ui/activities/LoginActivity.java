package com.sap.sailing.racecommittee.app.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.AppUtils;
import com.sap.sailing.android.shared.util.BroadcastManager;
import com.sap.sailing.android.shared.util.EulaHelper;
import com.sap.sailing.android.shared.util.NetworkHelper;
import com.sap.sailing.android.shared.util.NotificationHelper;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.BuildConfig;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.DataStore;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.domain.LoginType;
import com.sap.sailing.racecommittee.app.domain.configuration.impl.PreferencesDeviceConfigurationLoader;
import com.sap.sailing.racecommittee.app.logging.LogEvent;
import com.sap.sailing.racecommittee.app.ui.fragments.LoginListViews;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.AttachedDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.DialogListenerHost;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.CourseAreaListFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.EventListFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.PositionListFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.CourseAreaSelectedListenerHost;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.EventSelectedListenerHost;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.ItemSelectedListener;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.PositionSelectedListenerHost;
import com.sap.sailing.racecommittee.app.utils.QRHelper;
import com.sap.sailing.racecommittee.app.utils.StringHelper;
import com.sap.sailing.racecommittee.app.utils.autoupdate.AutoUpdater;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class LoginActivity extends BaseActivity implements EventSelectedListenerHost, CourseAreaSelectedListenerHost,
        PositionSelectedListenerHost, DialogListenerHost.DialogResultListener {

    private final static String CourseAreaListFragmentTag = "CourseAreaListFragmentTag";
    private final static String AreaPositionListFragmentTag = "AreaPositionListFragmentTag";

    private final static String TAG = LoginActivity.class.getName();

    private final PositionListFragment positionFragment;
    private View backdrop;
    private LoginListViews loginListViews = null;

    private Button sign_in;

    // FIXME weird data redundancy by using different field for setting values makes everything so complex and buggy
    private String eventName = null;
    private String courseAreaName = null;
    private String positionName = null;

    private Serializable mSelectedEventId;
    private UUID mSelectedCourseAreaUUID;

    private IntentReceiver mReceiver;
    private ReadonlyDataManager dataManager;
    private View progressSpinner;

    private ItemSelectedListener<EventBase> eventSelectionListener = new ItemSelectedListener<EventBase>() {

        public void itemSelected(Fragment sender, EventBase event) {

            final Serializable eventId = selectEvent(event);

            // FIXME: its weird to have this button setup in here
            setupSignInButton();

            // prepare views after the event selection

            // close all currently open list views
            if (loginListViews != null) {
                loginListViews.closeAll();
            }
            addCourseAreaListFragment(eventId);

            // send intent to open the course area selection list
            Intent intent = new Intent(AppConstants.INTENT_ACTION_TOGGLE);
            intent.putExtra(AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_AREA);
            BroadcastManager.getInstance(LoginActivity.this).addIntent(intent);
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
            // send intent to open the position selection list
            Intent intent = new Intent(AppConstants.INTENT_ACTION_TOGGLE);
            intent.putExtra(AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_POSITION);
            BroadcastManager.getInstance(LoginActivity.this).addIntent(intent);
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
                    ExLog.i(LoginActivity.this, TAG,
                            "Logged in: " + eventName + " - " + courseAreaName + " - " + positionName);
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

    private void switchToRacingActivity() {
        Intent intent = new Intent(LoginActivity.this, RacingActivity.class);
        intent.putExtra(AppConstants.COURSE_AREA_UUID_KEY, mSelectedCourseAreaUUID);
        intent.putExtra(AppConstants.EventIdTag, mSelectedEventId);
        startActivity(intent);
        finish();
    }

    private Serializable selectEvent(EventBase event) {
        final Serializable eventId = event.getId();
        eventName = event.getName();
        // TODO: explicitly set the header text of the fragment to this name
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
        courseAreaName = courseArea.getName();
        mSelectedCourseAreaUUID = courseArea.getId();
        loginListViews.getCourseAreaContainer().setHeaderText(courseAreaName);
    }

    private boolean isCourseAreaSelected() {
        return (courseAreaName != null && mSelectedCourseAreaUUID != null);
    }

    private void resetCourseArea() {
        courseAreaName = null;
        mSelectedCourseAreaUUID = null;
        loginListViews.getCourseAreaContainer().setHeaderText("");
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
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.position_fragment, positionFragment, AreaPositionListFragmentTag);
            transaction.commitAllowingStateLoss();
        }
    }

    private void addCourseAreaListFragment(Serializable eventId) {
        resetCourseArea();
        updateSignInButtonState();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.area_fragment, CourseAreaListFragment.newInstance(eventId), CourseAreaListFragmentTag);
        transaction.commitAllowingStateLoss();
    }

    private void addEventListFragment() {
        resetEvent();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
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
        ExLog.i(this, TAG, "Starting Login: " + AppUtils.with(this).getBuildInfo());
        String[] addresses = NetworkHelper.getInstance(this).getLocalIpAddress();
        if (addresses != null) {
            for (String address : addresses) {
                ExLog.i(this, TAG, "IP-Addresses: " + address);
            }
        }

        String action = getIntent().getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.app_name);
            if (QRHelper.with(this).saveData(getIntent().getData().toString())) {
                builder.setMessage(getString(R.string.server_deeplink_message, preferences.getServerBaseURL()));
            }
            builder.setPositiveButton(android.R.string.ok, null);
            builder.show();
        }

        // This is required to reactivate the loader manager after configuration change (screen rotation)
        getLoaderManager();

        dataManager = DataManager.create(this);
        DataStore dataStore = dataManager.getDataStore();

        // Check if the user has been logged in before and if so bring him directly to the racing activity
        mSelectedCourseAreaUUID = dataStore.getCourseUUID();
        mSelectedEventId = dataStore.getEventUUID();
        if (mSelectedEventId != null && mSelectedCourseAreaUUID != null) {
            if (preferences.getAccessToken() != null) {
                switchToRacingActivity();
            } else {
                startActivity(new Intent(this, PasswordActivity.class));
                finish();
            }
        }

        setContentView(R.layout.login_view);

        mReceiver = new IntentReceiver();

        // setup the login list views fragment
        loginListViews = new LoginListViews();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.login_listview, loginListViews);
        transaction.commitAllowingStateLoss();

        new AutoUpdater(this).notifyAfterUpdate();

        backdrop = findViewById(R.id.login_view_backdrop);

        if (!EulaHelper.with(this).isEulaAccepted()) {
            EulaHelper.with(this).showEulaDialog(null);
        }

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        int smallIcon = R.drawable.ic_boat_white_24dp;
        CharSequence title = getText(R.string.app_name);
        NotificationHelper.prepareNotificationWith(title, largeIcon, smallIcon);
    }

    @Override
    public void onPositionSelected(LoginType type) {

        // FIXME: this is some kind of exception handling
        /*
         * if (mSelectedCourseAreaUUID == null) { String toastText = getString(R.string.selected_course_area_lost);
         * Toast.makeText(LoginActivity.this, toastText, Toast.LENGTH_LONG).show(); ExLog.e(LoginActivity.this, TAG,
         * "Course area reference was not set - cannot start racing activity."); return; }
         */

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

        if (preferences.needConfigRefresh()) {
            preferences.setNeedConfigRefresh(false);
            Intent intent = new Intent(this, getClass());
            startActivity(intent);
            finish();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.INTENT_ACTION_RESET);
        filter.addAction(AppConstants.INTENT_ACTION_VALID_DATA);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);

        BroadcastManager.getInstance(this).addIntent(new Intent(AppConstants.INTENT_ACTION_CHECK_LOGIN));

        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());
        if (!BuildConfig.DEBUG && resultCode != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().getErrorDialog(this, resultCode, 1).show();
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
        dismissProgressSpinner();
    }

    private void setupDataManager() {
        showProgressSpinner();
        String deviceConfigurationName = AppPreferences.on(getApplicationContext()).getDeviceConfigurationName();
        UUID deviceConfigurationUuid = AppPreferences.on(getApplicationContext()).getDeviceConfigurationUuid();
        LoaderCallbacks<?> configurationLoader = dataManager.createConfigurationLoader(deviceConfigurationName,
                deviceConfigurationUuid, new LoadClient<DeviceConfiguration>() {
                    @Override
                    public void onLoadFailed(Exception reason) {
                        dismissProgressSpinner();

                        preferences.setDefaultProtestTimeDurationInMinutesCustomEditable(true);
                        if (reason instanceof FileNotFoundException) {
                            Toast.makeText(getApplicationContext(), getString(R.string.loading_configuration_not_found),
                                    Toast.LENGTH_LONG).show();
                            ExLog.w(LoginActivity.this, TAG, String.format(
                                    "There seems to be no configuration for this device: %s", reason.toString()));
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.loading_configuration_failed),
                                    Toast.LENGTH_LONG).show();
                            ExLog.ex(LoginActivity.this, TAG, reason);
                        }

                        slideUpBackdropDelayed();
                    }

                    @Override
                    public void onLoadSucceeded(DeviceConfiguration configuration, boolean isCached) {
                        getSupportLoaderManager().destroyLoader(0);

                        dismissProgressSpinner();

                        // this is our 'global' configuration, let's store it in app preferences
                        PreferencesDeviceConfigurationLoader.wrap(configuration, preferences).store();

                        Toast.makeText(LoginActivity.this, getString(R.string.loading_configuration_succeded),
                                Toast.LENGTH_LONG).show();
                        slideUpBackdropDelayed();
                    }
                });

        if (!preferences.isOfflineMode()) {
            // reload the configuration if needed...
            getSupportLoaderManager().restartLoader(0, null, configurationLoader).forceLoad();
        } else {
            dismissProgressSpinner();
            slideUpBackdropDelayed();
        }
    }

    private void showProgressSpinner() {
        if (progressSpinner == null) {
            progressSpinner = findViewById(R.id.progress_spinner);
        }
        if (progressSpinner != null) {
            progressSpinner.setVisibility(View.VISIBLE);
        }
    }

    private void dismissProgressSpinner() {
        if (progressSpinner != null && progressSpinner.getVisibility() == View.VISIBLE) {
            progressSpinner.setVisibility(View.GONE);
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
        final View loginView = findViewById(R.id.login_listview);
        // don't slide up if already up
        if (backdrop.getY() != 0) {
            return;
        }

        int upperRoom = backdrop.getHeight() + (backdrop.getHeight() / 5);
        View subTitle = ViewHelper.get(backdrop, R.id.backdrop_login);
        if (subTitle != null) {
            upperRoom = backdrop.getHeight() - subTitle.getHeight()
                    - getResources().getDimensionPixelSize(R.dimen.default_padding_half);
        }
        ObjectAnimator frameAnimation = ObjectAnimator.ofFloat(backdrop, "y", 0, -upperRoom);
        ValueAnimator heightAnimation = ValueAnimator.ofInt(0, upperRoom);
        heightAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams lpLogin = loginView.getLayoutParams();
                lpLogin.height = val;
                loginView.setLayoutParams(lpLogin);
            }
        });

        Collection<Animator> animators = new ArrayList<>();
        animators.add(heightAnimation);
        animators.add(frameAnimation);
        animators.add(getAlphaRevAnimator(findViewById(R.id.backdrop_title)));
        animators.add(getAlphaAnimator(findViewById(R.id.gradient)));

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animators);
        animatorSet.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.addListener(new AnimatorSetListener());
        animatorSet.start();
    }

    private ObjectAnimator getAlphaAnimator(@NonNull View target) {
        return ObjectAnimator.ofFloat(target, "alpha", 0f, 1f);
    }

    private ObjectAnimator getAlphaRevAnimator(@NonNull View target) {
        return ObjectAnimator.ofFloat(target, "alpha", 1f, 0f);
    }

    /**
     * Reset the data (reload from server)
     *
     * @param force
     *            Reload data, even if the backdrop is moved up
     */
    private void resetData(boolean force) {
        if (!force && backdrop.getY() != 0) {
            return;
        }

        setupDataManager();

        addEventListFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.area_fragment, new Fragment());
        transaction.replace(R.id.position_fragment, new Fragment());
        transaction.commit();
    }

    private class IntentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (AppConstants.INTENT_ACTION_RESET.equals(action)) {
                resetData(true);
            } else if (AppConstants.INTENT_ACTION_VALID_DATA.equals(action)) {
                resetData(false);
            }
        }
    }

    private class AnimatorSetListener implements Animator.AnimatorListener {

        private View submit;

        private AnimatorSetListener() {
            submit = findViewById(R.id.login_submit);
        }

        @Override
        public void onAnimationStart(Animator animation) {
            setAlpha(submit, 0f);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (submit != null) {
                submit.animate().alpha(1f)
                        .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            // no op
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
            setAlpha(submit, 0f);
        }

        private void setAlpha(View view, float alpha) {
            if (view != null) {
                view.setAlpha(alpha);
            }
        }
    }
}
