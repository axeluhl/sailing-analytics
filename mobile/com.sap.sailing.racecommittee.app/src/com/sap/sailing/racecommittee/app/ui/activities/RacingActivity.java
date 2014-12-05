package com.sap.sailing.racecommittee.app.ui.activities;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.CollectionUtils;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.logging.LogEvent;
import com.sap.sailing.racecommittee.app.services.RaceStateService;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceListDataType;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceListDataTypeHeader;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceListDataTypeRace;
import com.sap.sailing.racecommittee.app.ui.fragments.NavigationDrawerFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.NavigationDrawerFragment.NavigationDrawerCallbacks;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceInfoFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.WelcomeFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.WindFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.RaceInfoListener;
import com.sap.sailing.racecommittee.app.utils.TickListener;
import com.sap.sailing.racecommittee.app.utils.TickSingleton;

public class RacingActivity extends SessionActivity implements RaceInfoListener, NavigationDrawerCallbacks,
        TickListener, OnClickListener {
    private class RaceLoadClient implements LoadClient<Collection<ManagedRace>> {

        private CourseArea courseArea;
        private Collection<ManagedRace> lastSeenRaces = null;

        public RaceLoadClient(CourseArea courseArea) {
            this.courseArea = courseArea;
        }

        @Override
        public void onLoadFailed(Exception ex) {
            setSupportProgressBarIndeterminateVisibility(false);
            AlertDialog.Builder builder = new AlertDialog.Builder(RacingActivity.this);
            builder.setMessage(String.format(getString(R.string.generic_load_failure), ex.getMessage()))
                    .setTitle(getString(R.string.loading_failure)).setIcon(R.drawable.ic_warning_grey600_36dp)
                    .setCancelable(true)
                    .setPositiveButton(getString(R.string.retry), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            setSupportProgressBarIndeterminateVisibility(true);

                            ExLog.i(RacingActivity.this, TAG, "Issuing a reload of managed races");
                            getLoaderManager().restartLoader(RacesLoaderId, null,
                                    dataManager.createRacesLoader(courseArea.getId(), RaceLoadClient.this));
                            dialog.cancel();
                        }
                    }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            builder.create().show();
        }

        @Override
        public void onLoadSucceded(Collection<ManagedRace> data, boolean isCached) {
            // Let's do the setup stuff only when the data is changed (or its the first time)
            if (lastSeenRaces != null && CollectionUtils.isEqualCollection(data, lastSeenRaces)) {
                ExLog.i(RacingActivity.this, TAG, "Same races are already loaded...");
            } else {
                lastSeenRaces = data;

                registerOnService(data);
                navDrawerFragment.setupOn(data);

                Toast.makeText(RacingActivity.this,
                        String.format(getString(R.string.racing_load_success), data.size()), Toast.LENGTH_SHORT).show();
            }

            setSupportProgressBarIndeterminateVisibility(false);
        }
    }

    private static ProgressBar mProgressSpinner;
    private static final int RacesLoaderId = 0;

    private static final String TAG = RacingActivity.class.getName();
    private static int WIND_ACTIVITY_REQUEST_CODE = 7331;

    private Button currentTime;
    private ReadonlyDataManager dataManager;
    private RaceInfoFragment infoFragment;
    private Wind mWind;
    private NavigationDrawerFragment navDrawerFragment;
    private RaceFragment raceFragment;
    private ManagedRace selectedRace;
    private Button windButton;

    private WindFragment windFragment;

    private Serializable getCourseAreaIdFromIntent() {
        if (getIntent() == null || getIntent().getExtras() == null) {
            Log.e(getClass().getName(), "Expected an intent carrying event extras.");
            return null;
        }

        final Serializable courseId = getIntent().getExtras().getSerializable(AppConstants.COURSE_AREA_UUID_KEY);
        if (courseId == null) {
            Log.e(getClass().getName(), "Expected an intent carrying the course area id.");
            return null;
        }
        return courseId;
    }

    private Serializable getEventIdFromItent() {
        if (getIntent() == null || getIntent().getExtras() == null) {
            Log.e(getClass().getName(), "Expected an intent carrying event extras.");
        }

        final Serializable eventId = getIntent().getExtras().getSerializable(AppConstants.EventIdTag);
        if (eventId == null) {
            Log.e(getClass().getName(), "Expected an intent carrying the event id.");
            return null;
        }
        return eventId;
    }

    private void loadRaces(final CourseArea courseArea) {
        setSupportProgressBarIndeterminateVisibility(true);

        ExLog.i(this, TAG, "Issuing loading of managed races from data manager");
        getLoaderManager().initLoader(RacesLoaderId, null,
                dataManager.createRacesLoader(courseArea.getId(), new RaceLoadClient(courseArea)));
    }

    public void loadWindFragment() {
        // check if the fragment is actively shown already, otherwise show it
        if ((windFragment != null && !windFragment.isFragmentUIActive()) || windFragment == null) {
            windFragment = new WindFragment();
            getFragmentManager().beginTransaction()
                    // .setCustomAnimations(R.animator.slide_in, R.animator.slide_out)
                    .replace(R.id.racing_view_right_container, windFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack(null).commit();
        }
    }

    public void logout() {
        logoutSession();
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void notifyTick() {
        if (currentTime != null) {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
            currentTime.setText(format.format(Calendar.getInstance().getTime()));
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.racing_view_right_container);
        if (!(fragment instanceof RaceInfoFragment || fragment instanceof WelcomeFragment)) {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStackImmediate();
                getFragmentManager().beginTransaction().commit();

                // fix for filled out RaceInfoFragment
                if (infoFragment != null && infoFragment.isFragmentUIActive() && selectedRace != null) {
                    ExLog.i(this, this.getClass().getCanonicalName(), "Returning to RaceInfoFragment");
                    getFragmentManager().popBackStackImmediate();
                    onRaceItemClicked(selectedRace);
                }
            }
        } else {
            logoutSession();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.windButton:
            loadWindFragment();
            break;

        default:
            break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // features must be requested before anything else
        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.racing_view);

        dataManager = DataManager.create(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            View view = findViewById(R.id.toolbar_extended);
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            }
            mProgressSpinner = (ProgressBar) findViewById(R.id.progress_spinner);
        }

        navDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        navDrawerFragment.setUp((DrawerLayout) findViewById(R.id.drawer_layout));

        Serializable courseAreaId = getCourseAreaIdFromIntent();
        if (courseAreaId == null) {
            throw new IllegalStateException("There was no course area id transmitted...");
        }
        CourseArea courseArea = dataManager.getDataStore().getCourseArea(courseAreaId);
        if (courseArea != null) {
            loadRaces(courseArea);
        } else {
            Toast.makeText(this, getString(R.string.racing_course_area_missing), Toast.LENGTH_LONG).show();
        }

        currentTime = (Button) findViewById(R.id.currentTime);
        windButton = (Button) findViewById(R.id.windButton);
        if (windButton != null) {
            windButton.setOnClickListener(this);
        }

        if (savedInstanceState != null) {
            mWind = (Wind) savedInstanceState.getSerializable("wind");
            if (mWind != null) {
                onWindEntered(mWind);
            }
            Bundle args = savedInstanceState.getBundle("childBundle");
            if (args != null) {
                infoFragment = new RaceInfoFragment();
                infoFragment.setArguments(args);
            }
        }

        Serializable eventId = getEventIdFromItent();
        if (eventId == null) {
            throw new IllegalStateException("There was no event id transmitted...");
        }

        if (infoFragment == null) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.racing_view_right_container,
                            new WelcomeFragment(dataManager.getDataStore(), courseAreaId, eventId, preferences
                                    .getAuthor())).commit();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(RaceListDataType selectedItem) {
        if (selectedItem instanceof RaceListDataTypeRace) {
            RaceListDataTypeRace selectedElement = (RaceListDataTypeRace) selectedItem;
            selectedElement.setUpdateIndicatorVisible(false);
            // ((ImageView) findViewById(R.id.Welter_Cell_UpdateLabel)).setVisibility(View.GONE);

            selectedRace = selectedElement.getRace();
            ExLog.i(this, LogEvent.RACE_SELECTED_ELEMENT, selectedRace.getId() + " " + selectedRace.getStatus());
            onRaceItemClicked(selectedRace);
        } else if (selectedItem instanceof RaceListDataTypeHeader) {
            // This is for logging purposes only!
            RaceListDataTypeHeader selectedTitle = (RaceListDataTypeHeader) selectedItem;
            ExLog.i(this, LogEvent.RACE_SELECTED_TITLE, selectedTitle.toString());
        }
    }

    public void onRaceItemClicked(ManagedRace managedRace) {
        infoFragment = new RaceInfoFragment();
        infoFragment.setArguments(RaceFragment.createArguments(managedRace));

        getFragmentManager().beginTransaction()
                // .setCustomAnimations(R.animator.slide_in, R.animator.slide_out)
                .replace(R.id.racing_view_right_container, infoFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack(null).commit();
    }

    @Override
    public void onResetTime() {
        infoFragment.onResetTime();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("wind", mWind);
        if (infoFragment != null) {
            outState.putBundle("childBundle", infoFragment.getArguments());
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        TickSingleton.INSTANCE.registerListener(this);
        notifyTick();
    }

    @Override
    public void onStop() {
        super.onStop();

        TickSingleton.INSTANCE.unregisterListener(this);
    }

    public void onWindEntered(Wind windFix) {
        if (windFix != null) {
            windButton.setText(String.format(getString(R.string.wind_info), windFix.getKnots(), windFix.getBearing()
                    .reverse().toString()));
            if (selectedRace != null) {
                selectedRace.getState().setWindFix(MillisecondsTimePoint.now(), windFix);
            }

            mWind = windFix;
        }

        getFragmentManager().popBackStackImmediate();

        if (infoFragment != null && infoFragment.isFragmentUIActive()) {
            ExLog.i(this, this.getClass().getCanonicalName(), "Returning to RaceInfoFragment from WindFragment");
            getFragmentManager().popBackStackImmediate();

            onRaceItemClicked(selectedRace);
        }
    }

    private void registerOnService(Collection<ManagedRace> races) {
        // since the service is the long-living component
        // he should decide whether these races are already
        // registered or not.
        for (ManagedRace race : races) {
            Intent registerIntent = new Intent(this, RaceStateService.class);
            registerIntent.setAction(AppConstants.INTENT_ACTION_REGISTER_RACE);
            registerIntent.putExtra(AppConstants.RACE_ID_KEY, race.getId());
            this.startService(registerIntent);
        }
    }

    public void replaceFragment(RaceFragment fragment) {
        replaceFragment(fragment, R.id.sub_fragment);
    }

    public void replaceFragment(RaceFragment fragment, int viewId) {
        raceFragment = fragment;

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(viewId, raceFragment);
        // transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void setSupportProgressBarIndeterminateVisibility(boolean visible) {
        super.setSupportProgressBarIndeterminateVisibility(visible);

        if (mProgressSpinner != null) {
            if (visible) {
                mProgressSpinner.setVisibility(View.VISIBLE);
            } else {
                mProgressSpinner.setVisibility(View.GONE);
            }
        }
    }
}
