package com.sap.sailing.racecommittee.app.ui.activities;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.*;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.CollectionUtils;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.EventBase;
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
import com.sap.sailing.racecommittee.app.ui.fragments.*;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceListFragment.RaceListCallbacks;
import com.sap.sailing.racecommittee.app.ui.fragments.panels.TimePanelFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.RaceInfoListener;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.WindFragment;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;
import com.sap.sailing.racecommittee.app.utils.TickListener;
import com.sap.sailing.racecommittee.app.utils.TickSingleton;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Collection;

public class RacingActivity extends SessionActivity implements RaceInfoListener, RaceListCallbacks, OnClickListener {
    private static final String TAG = RacingActivity.class.getName();
    private static final String WIND = "wind";
    private static final String RACE = "race";

    private static final int RacesLoaderId = 2;
    private static final int EventsLoaderId = 1;
    private static final int CourseLoaderId = 0;

    private static ProgressBar mProgressSpinner;

    private IntentReceiver mReceiver;
    private TextView windValue;
    private ReadonlyDataManager dataManager;
    private RaceInfoFragment infoFragment;
    private Wind mWind;
    private RaceListFragment mRaceList;
    private RaceFragment raceFragment;
    private ManagedRace mSelectedRace;
    private RelativeLayout windButton;
    private WindFragment windFragment;
    private Serializable mEventId;
    private EventBase mEvent;
    private CourseArea mCourseArea;
    private Serializable mCourseAreaId;
    private TimePoint startTime;
    private TimePanelFragment timeFragment;

    private void setupFragments() {
        new Handler().post(new Runnable() {

            public void run() {
                loadRaces(mCourseArea);
                loadNavDrawer(mCourseArea, mEvent);
                if (mSelectedRace == null) {
                    loadWelcomeFragment();
                }
            }
        });
    }

    private Serializable getCourseAreaIdFromIntent() {
        if (getIntent() == null || getIntent().getExtras() == null) {
            ExLog.e(this, TAG, "Expected an intent carrying event extras.");
            return null;
        }

        final Serializable courseId = getIntent().getExtras().getSerializable(AppConstants.COURSE_AREA_UUID_KEY);
        if (courseId == null) {
            ExLog.e(this, TAG, "Expected an intent carrying the course area id.");
            return null;
        }
        return courseId;
    }

    private Serializable getEventIdFromIntent() {
        if (getIntent() == null || getIntent().getExtras() == null) {
            ExLog.e(this, TAG, "Expected an intent carrying event extras.");
        }

        final Serializable eventId = getIntent().getExtras().getSerializable(AppConstants.EventIdTag);
        if (eventId == null) {
            ExLog.e(this, TAG, "Expected an intent carrying the event id.");
            return null;
        }
        return eventId;
    }

    private void loadCourses(final EventBase event) {
        setSupportProgressBarIndeterminateVisibility(true);

        ExLog.i(this, TAG, "Issuing loading of courses from data manager");
        getLoaderManager().initLoader(CourseLoaderId, null,
                dataManager.createCourseAreasLoader(event, new CourseLoadClient()));
    }

    private void loadRaces(final CourseArea courseArea) {
        setSupportProgressBarIndeterminateVisibility(true);

        ExLog.i(this, TAG, "Issuing loading of managed races from data manager");
        getLoaderManager().initLoader(RacesLoaderId, null,
                dataManager.createRacesLoader(courseArea.getId(), new RaceLoadClient(courseArea)));
    }

    private void loadEvents() {
        setSupportProgressBarIndeterminateVisibility(true);

        ExLog.i(this, TAG, "Issuing loading of events from data manager");
        getLoaderManager().initLoader(EventsLoaderId, null, dataManager.createEventsLoader(new EventLoadClient()));
    }

    public void loadWindFragment() {
        // check if the fragment is actively shown already, otherwise show it
        if ((windFragment != null && !windFragment.isFragmentUIActive()) || windFragment == null) {
            windFragment = WindFragment.newInstance(0);

            getFragmentManager().beginTransaction()
                    // .setCustomAnimations(R.animator.slide_in, R.animator.slide_out)
                    .replace(R.id.racing_view_container, windFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack(null).commit();
        }
    }

    public void logout() {
        logoutSession();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.racing_view_container);
        if (!(fragment instanceof RaceInfoFragment || fragment instanceof WelcomeFragment)) {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStackImmediate();
                getFragmentManager().beginTransaction().commit();

                // fix for filled out RaceInfoFragment
                if (infoFragment != null && infoFragment.isFragmentUIActive() && mSelectedRace != null) {
                    ExLog.i(this, this.getClass().getCanonicalName(), "Returning to RaceInfoFragment");

                    getFragmentManager().popBackStackImmediate();
                    onRaceItemClicked(mSelectedRace);
                }
            }
        } else {
            logoutSession();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.wind:
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

        ThemeHelper.setTheme(this);
        setContentView(R.layout.racing_view);

        dataManager = DataManager.create(this);
        mRaceList = (RaceListFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            mProgressSpinner = (ProgressBar) findViewById(R.id.progress_spinner);
        }

        Serializable courseAreaId = getCourseAreaIdFromIntent();
        mCourseAreaId = courseAreaId;
        if (courseAreaId == null) {
            throw new IllegalStateException("There was no course area id transmitted...");
        }
        ExLog.i(this, this.getClass().toString(), "trying to load courseArea via id: " + courseAreaId);
        mCourseArea = dataManager.getDataStore().getCourseArea(courseAreaId);

        Serializable eventId = getEventIdFromIntent();
        if (eventId == null) {
            throw new IllegalStateException("There was no event id transmitted...");
        }
        mEventId = eventId;

        EventBase event = dataManager.getDataStore().getEvent(eventId);
        if (event == null) {
            ExLog.e(this, TAG, "Noooo the event is null :/");
            preferences.isSetUp(false);
            PackageManager pm = getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(getPackageName());
            startActivity(intent);
            finish();
        } else {
            if (mCourseArea != null) {
                loadRaces(mCourseArea);
                ExLog.i(this, this.getClass().toString(), "did load courseArea!");
            } else {
                ExLog.i(this, this.getClass().toString(), "courseArea == null :(");
                Toast.makeText(this, getString(R.string.racing_course_area_missing), Toast.LENGTH_LONG).show();
            }
            loadNavDrawer(mCourseArea, event);
            loadWelcomeFragment();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mReceiver = new IntentReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.INTENT_ACTION_TIME_SHOW);
        filter.addAction(AppConstants.INTENT_ACTION_TIME_HIDE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    private void loadNavDrawer(CourseArea courseArea, EventBase event) {
        String race = getResources().getString(R.string.nav_header).replace("#AREA#", courseArea.getName())
                .replace("#EVENT#", event.getName()).replace("#AUTHOR#", preferences.getAuthor().getName());
        SpannableString header = new SpannableString(race);
        StyleSpan spanBold = new StyleSpan(Typeface.BOLD);
        header.setSpan(spanBold, 0, event.getName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mRaceList.setUp((DrawerLayout) findViewById(R.id.drawer_layout), header);
    }

    private void loadWelcomeFragment() {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.racing_view_container, WelcomeFragment.newInstance())
                .commit();
    }

    public TimePoint getStartTime() {
        return startTime;
    }

    public void setStartTime(TimePoint startTime) {
        this.startTime = startTime;
    }

    @Override
    public void onRaceListItemSelected(RaceListDataType selectedItem) {
        if (selectedItem instanceof RaceListDataTypeRace) {
            RaceListDataTypeRace selectedElement = (RaceListDataTypeRace) selectedItem;
            selectedElement.setUpdateIndicatorVisible(false);
            // ((ImageView) findViewById(R.id.Welter_Cell_UpdateLabel)).setVisibility(View.GONE);

            mSelectedRace = selectedElement.getRace();
            ExLog.i(this, LogEvent.RACE_SELECTED_ELEMENT, mSelectedRace.getId() + " " + mSelectedRace.getStatus());
            onRaceItemClicked(mSelectedRace);
        } else if (selectedItem instanceof RaceListDataTypeHeader) {
            // This is for logging purposes only!
            RaceListDataTypeHeader selectedTitle = (RaceListDataTypeHeader) selectedItem;
            ExLog.i(this, LogEvent.RACE_SELECTED_TITLE, selectedTitle.toString());
        }
    }

    public void onRaceItemClicked(ManagedRace managedRace) {
        mSelectedRace = managedRace;
        infoFragment = new RaceInfoFragment();
        infoFragment.setArguments(RaceFragment.createArguments(managedRace));

        setupActionBar(managedRace);

        getFragmentManager().beginTransaction()
//                .setCustomAnimations(R.animator.slide_in, R.animator.slide_out)
                .replace(R.id.racing_view_container, infoFragment)
//                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    @Override
    public void onResetTime() {
        infoFragment.onResetTime();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mWind = (Wind) savedInstanceState.getSerializable(WIND);
        if (mWind != null) {
            onWindEntered(mWind);
        }

//        //TODO Implement reload after return
//        Serializable raceId  = savedInstanceState.getSerializable(RACE);
//        mSelectedRace = OnlineDataManager.create(this).getDataStore().getRace(raceId);
//        if (mSelectedRace != null) {
//            onRaceItemClicked(mSelectedRace);
//        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(WIND, mWind);
        if (mSelectedRace != null) {
            outState.putSerializable(RACE, mSelectedRace.getId());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.options_menu_reset:
                ExLog.i(this, TAG, "Clicked RESET RACE");
                resetRace();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onWindEntered(Wind windFix) {
        TextView windValue = (TextView) findViewById(R.id.wind_value);
        if (windFix != null) {
            if (windValue != null) {
                windValue.setText(String.format(getString(R.string.wind_info), windFix.getKnots(), windFix.getBearing().reverse().toString()));
            }
            if (mSelectedRace != null) {
                mSelectedRace.getState().setWindFix(MillisecondsTimePoint.now(), windFix);
            }

            mWind = windFix;
        } else {
            if (windValue != null) {
                windValue.setText(getString(R.string.wind_unknown));
            }
        }

        getFragmentManager().popBackStackImmediate();

        if (infoFragment != null && infoFragment.isFragmentUIActive()) {
            ExLog.i(this, this.getClass().getCanonicalName(), "Returning to RaceInfoFragment from WindFragment");
            getFragmentManager().popBackStackImmediate();

            onRaceItemClicked(mSelectedRace);
        }
    }

    private void registerOnService(final Collection<ManagedRace> races) {
        // since the service is the long-living component
        // he should decide whether these races are already
        // registered or not.
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (ManagedRace race : races) {
                    Intent registerIntent = new Intent(RacingActivity.this, RaceStateService.class);
                    registerIntent.setAction(AppConstants.INTENT_ACTION_REGISTER_RACE);
                    registerIntent.putExtra(AppConstants.RACE_ID_KEY, race.getId());
                    RacingActivity.this.startService(registerIntent);
                }
            }
        }).run();
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

    private void setupActionBar(ManagedRace race) {
        String title = race.getSeries().getName() + " / ";
        if (race.getFleet() != null) {
            title += race.getFleet().getName() + " / " + race.getRaceName();
        } else {
            title += race.getRaceName();
        }

        getSupportActionBar().setTitle(title);
    }

    public void resetRace() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.race_reset_confirmation_title));
        builder.setMessage(getString(R.string.race_reset_message));
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.race_reset_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ExLog.i(RacingActivity.this, LogEvent.RACE_RESET_YES, mSelectedRace.getId().toString());
                ExLog.w(RacingActivity.this, TAG, String.format("Race %s is selected for reset.", mSelectedRace.getId()));
                mSelectedRace.getState().setAdvancePass(MillisecondsTimePoint.now());
                onRaceItemClicked(mSelectedRace);
            }
        });
        builder.setNegativeButton(getString(R.string.race_reset_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ExLog.i(RacingActivity.this, LogEvent.RACE_RESET_NO, mSelectedRace.getId().toString());
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    public void showMarker(ImageView view, int level) {
        if (view != null) {
            view.setImageLevel(level);
        }
    }

    public void openDrawer() {
        if (mRaceList != null) {
            mRaceList.openDrawer();
        }
    }

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
                mRaceList.setupOn(data);

                Toast.makeText(RacingActivity.this,
                        String.format(getString(R.string.racing_load_success), data.size()), Toast.LENGTH_SHORT).show();
            }

            setSupportProgressBarIndeterminateVisibility(false);
        }
    }

    private class EventLoadClient implements LoadClient<Collection<EventBase>> {

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
        public void onLoadSucceded(Collection<EventBase> data, boolean isCached) {
            // TODO Auto-generated method stub
            Toast.makeText(RacingActivity.this, getString(R.string.loading_events_succeded), Toast.LENGTH_SHORT).show();
            setSupportProgressBarIndeterminateVisibility(false);

            for (EventBase event : data) {
                dataManager.getDataStore().addEvent(event);
                if (event.getId().toString().equals(mEventId.toString())) {
                    mEvent = event;
                }
            }

            if (mCourseArea != null) {
                setupFragments();
            } else {
                loadCourses(mEvent);
            }
        }
    }

    private class CourseLoadClient implements LoadClient<Collection<CourseArea>> {

        @Override
        public void onLoadFailed(Exception reason) {
            setSupportProgressBarIndeterminateVisibility(false);
            ExLog.e(getApplicationContext(), TAG, "Errors loading CourseData");
            ExLog.e(getApplicationContext(), TAG, reason.getMessage());
        }

        @Override
        public void onLoadSucceded(Collection<CourseArea> data, boolean isCached) {
            Toast.makeText(RacingActivity.this, "Loading of CourseData succeeded", Toast.LENGTH_SHORT).show();
            setSupportProgressBarIndeterminateVisibility(false);

            for (CourseArea course : data) {
                if (course.getId().toString().equals(mCourseAreaId.toString())) {
                    mCourseArea = course;
                    setupFragments();
                }
            }
        }
    }

    private class IntentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            View view = findViewById(R.id.race_panel_top);
            if (view != null) {
                if (AppConstants.INTENT_ACTION_TIME_HIDE.equals(action)) {
                    view.setVisibility(View.GONE);
                }
                if (AppConstants.INTENT_ACTION_TIME_SHOW.equals(action)) {
                    view.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
