package com.sap.sailing.racecommittee.app.ui.activities;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.RaceInfoListener;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.unscheduled.WindFragment;
import com.sap.sailing.racecommittee.app.utils.TickListener;
import com.sap.sailing.racecommittee.app.utils.TickSingleton;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Collection;

public class RacingActivity extends SessionActivity implements RaceInfoListener, RaceListCallbacks, TickListener, OnClickListener {
    private static final String TAG = RacingActivity.class.getName();
    private static final String WIND = "wind";
    private static final String RACE = "race";

    private static final int RacesLoaderId = 2;
    private static final int EventsLoaderId = 1;
    private static final int CourseLoaderId = 0;

    private static ProgressBar mProgressSpinner;

    private TextView currentTime;
    private TextView headerTime;
    private TextView timeStart;
    private TextView timeFinish;
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
    private RelativeLayout mStartProcedureLayout;
    private RelativeLayout mStartModeLayout;
    private RelativeLayout mCourseLayout;
    private RelativeLayout mWindLayout;
    private RelativeLayout mAbandonFlagsLayout;
    private RelativeLayout mRecallFlagsLayout;
    private RelativeLayout mPostponeFlagsLayout;
    private RelativeLayout mCourseFlagsLayout;
    private RelativeLayout mMoreFlagsLayout;
    private ImageView mStartProcedureMarker;
    private ImageView mStartModeMarker;
    private ImageView mCourseMarker;
    private ImageView mWindMarker;
    private ImageView mAbandonFlagsMarker;
    private ImageView mRecallFlagsMarker;
    private ImageView mPostponeFlagsMarker;
    private ImageView mCourseFlagsMarker;
    private ImageView mMoreFlagsMarker;
    private SimpleDateFormat dateFormat;

    private void setupFragments() {
        Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                loadRaces(mCourseArea);
                loadNavDrawer(mCourseArea, mEvent);
                loadWelcomeFragment();
            }
        };
        handler.post(r);
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

            showMarker(mWindMarker, 1);
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
    public void notifyTick() {
        TimePoint now = MillisecondsTimePoint.now();

        currentTime = (TextView) findViewById(R.id.current_time);
        if (currentTime != null) {
            currentTime.setText(dateFormat.format(now.asMillis()));
        }

        if (mSelectedRace != null && mSelectedRace.getState() != null) {
            TimePoint startTime = mSelectedRace.getState().getStartTime();

            timeStart = (TextView) findViewById(R.id.time_start);
            if (timeStart != null) {
                timeStart.setText(getString(R.string.time_start).replace("#TIME#", dateFormat.format(startTime.asDate())));
            }

            timeFinish = (TextView) findViewById(R.id.time_finish);
            //TODO Add this later

            headerTime = (TextView) findViewById(R.id.timer_text);
            if (headerTime != null) {
                String time;
                if (startTime.asMillis() > now.asMillis()) {
                    time = TimeUtils.formatDurationUntil(startTime.minus(now.asMillis()).asMillis());
                } else {
                    time = TimeUtils.formatDurationSince(now.minus(startTime.asMillis()).asMillis());
                }
                headerTime.setText(getString(R.string.time).replace("#TIME#", time));
            }
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(getString(R.string.intent_update_ui)));
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
                setRightPanelVisibility(View.VISIBLE);
                resetMarker();
            }
        } else {
            logoutSession();
        }
    }

    @Override
    public void onClick(View view) {
        resetMarker();
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

        setContentView(R.layout.racing_view);

        dataManager = DataManager.create(this);
        mRaceList = (RaceListFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            mProgressSpinner = (ProgressBar) findViewById(R.id.progress_spinner);
        }
        dateFormat = new SimpleDateFormat("HH:mm:ss", getResources().getConfiguration().locale);

        Serializable courseAreaId = getCourseAreaIdFromIntent();
        mCourseAreaId = courseAreaId;
        if (courseAreaId == null) {
            throw new IllegalStateException("There was no course area id transmitted...");
        }
        ExLog.i(this, this.getClass().toString(), "trying to load courseArea via id: " + courseAreaId);
        mCourseArea = dataManager.getDataStore().getCourseArea(courseAreaId);

        getPanelWidgets();

        Serializable eventId = getEventIdFromIntent();
        if (eventId == null) {
            throw new IllegalStateException("There was no event id transmitted...");
        }
        mEventId = eventId;

        EventBase e = dataManager.getDataStore().getEvent(eventId);
        if (e == null) {
            ExLog.e(this, TAG, "Noooo the event is null :/");
            loadEvents();
            // setupDataStore();
            // loadNavDrawer( courseAreaId, eventId);
        } else {
            if (mCourseArea != null) {
                loadRaces(mCourseArea);
                ExLog.i(this, this.getClass().toString(), "did load courseArea!");
            } else {
                ExLog.i(this, this.getClass().toString(), "courseArea == null :(");
                Toast.makeText(this, getString(R.string.racing_course_area_missing), Toast.LENGTH_LONG).show();
            }
            loadNavDrawer(mCourseArea, e);
            loadWelcomeFragment();
        }
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
                // .setCustomAnimations(R.animator.slide_in, R.animator.slide_out)
                .replace(R.id.racing_view_container, infoFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
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

        setRightPanelVisibility(View.VISIBLE);
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

    private void getPanelWidgets() {
        // Left Panel
        mStartProcedureLayout = (RelativeLayout) findViewById(R.id.start_procedure);
        if (mStartProcedureLayout != null) {
            mStartProcedureLayout.setOnClickListener(this);
        }
        mStartProcedureMarker = (ImageView) findViewById(R.id.start_procedure_marker);
        mStartModeLayout = (RelativeLayout) findViewById(R.id.start_mode);
        if (mStartModeLayout != null) {
            mStartModeLayout.setOnClickListener(this);
        }
        mStartModeMarker = (ImageView) findViewById(R.id.start_mode_marker);
        mCourseLayout = (RelativeLayout) findViewById(R.id.course);
        if (mCourseLayout != null) {
            mCourseLayout.setOnClickListener(this);
        }
        mCourseMarker = (ImageView) findViewById(R.id.course_marker);
        mWindLayout = (RelativeLayout) findViewById(R.id.wind);
        if (mWindLayout != null) {
            mWindLayout.setOnClickListener(this);
        }
        mWindMarker = (ImageView) findViewById(R.id.wind_marker);

        // Right Panel
        mAbandonFlagsLayout = (RelativeLayout) findViewById(R.id.abandon_flags);
        if (mAbandonFlagsLayout != null) {
            mAbandonFlagsLayout.setOnClickListener(this);
        }
        mAbandonFlagsMarker = (ImageView) findViewById(R.id.abandon_flags_marker);
        mRecallFlagsLayout = (RelativeLayout) findViewById(R.id.recall_flags);
        if (mRecallFlagsLayout != null) {
            mRecallFlagsLayout.setOnClickListener(this);
        }
        mRecallFlagsMarker = (ImageView) findViewById(R.id.recall_flags_marker);
        mPostponeFlagsLayout = (RelativeLayout) findViewById(R.id.postpone_flags);
        if (mPostponeFlagsLayout != null) {
            mPostponeFlagsLayout.setOnClickListener(this);
        }
        mPostponeFlagsMarker = (ImageView) findViewById(R.id.postpone_flags_marker);
        mCourseFlagsLayout = (RelativeLayout) findViewById(R.id.course_flags);
        if (mCourseFlagsLayout != null) {
            mCourseFlagsLayout.setOnClickListener(this);
        }
        mCourseFlagsMarker = (ImageView) findViewById(R.id.course_flags_marker);
        mMoreFlagsLayout = (RelativeLayout) findViewById(R.id.more_flags);
        if (mMoreFlagsLayout != null) {
            mMoreFlagsLayout.setOnClickListener(this);
        }
        mMoreFlagsMarker = (ImageView) findViewById(R.id.more_flags_marker);

        resetMarker();
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


    public void setRightPanelVisibility(int visibility) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.panel_right);
        if (layout != null) {
            layout.setVisibility(visibility);
        }
    }

    public void showRightButtonCount(int count) {
        switch (count) {
            case 2:
                mAbandonFlagsLayout.setVisibility(View.GONE);
                mRecallFlagsLayout.setVisibility(View.GONE);
                mPostponeFlagsLayout.setVisibility(View.VISIBLE);
                mCourseFlagsMarker.setVisibility(View.VISIBLE);
                mMoreFlagsLayout.setVisibility(View.GONE);
                break;

            case 3:
                mAbandonFlagsLayout.setVisibility(View.VISIBLE);
                mRecallFlagsLayout.setVisibility(View.VISIBLE);
                mPostponeFlagsMarker.setVisibility(View.GONE);
                mCourseFlagsLayout.setVisibility(View.GONE);
                mMoreFlagsLayout.setVisibility(View.VISIBLE);
                break;

            case 4:
                mAbandonFlagsLayout.setVisibility(View.VISIBLE);
                mRecallFlagsLayout.setVisibility(View.VISIBLE);
                mPostponeFlagsLayout.setVisibility(View.GONE);
                mCourseFlagsLayout.setVisibility(View.VISIBLE);
                mMoreFlagsLayout.setVisibility(View.VISIBLE);
                break;

            default:
                mAbandonFlagsLayout.setVisibility(View.GONE);
                mRecallFlagsLayout.setVisibility(View.GONE);
                mPostponeFlagsLayout.setVisibility(View.GONE);
                mCourseFlagsLayout.setVisibility(View.GONE);
                mMoreFlagsLayout.setVisibility(View.GONE);
                break;
        }
    }

    private void resetMarker() {
        // Left Panel
        showMarker(mStartProcedureMarker, 0);
        showMarker(mStartModeMarker, 0);
        showMarker(mCourseMarker, 0);
        showMarker(mWindMarker, 0);

        // Right Panel
        showMarker(mAbandonFlagsMarker, 0);
        showMarker(mRecallFlagsMarker, 0);
        showMarker(mPostponeFlagsMarker, 0);
        showMarker(mCourseFlagsMarker, 0);
        showMarker(mMoreFlagsMarker, 0);
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
}
