package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.rrs26.RRS26RacingProcedure;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.impl.MainScheduleItem;
import com.sap.sailing.racecommittee.app.ui.activities.RacingActivity;
import com.sap.sailing.racecommittee.app.ui.adapters.MainScheduleAdapter;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;
import com.sap.sailing.racecommittee.app.utils.RaceHelper;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class MainScheduleFragment extends BaseFragment implements View.OnClickListener, MainScheduleAdapter.ItemClick {

    public static final String START_TIME = "startTime";
    public static final String DEPENDENT_RACE = "dependentRace";
    public static final String START_TIME_DIFF = "startTimeDiff";

    private static final String TAG = MainScheduleFragment.class.getName();

    private MainScheduleAdapter mAdapter;
    private ArrayList<MainScheduleItem> mItems;
    private MainScheduleItem mItemStartTime;
    private MainScheduleItem mItemStartWind;

    private String mStartTimeString;
    private SimpleRaceLogIdentifier mRaceId;
    private TimePoint mStartTime;
    private Duration mStartTimeDiff;
    private RacingProcedureType mRacingProcedureType;

    private SimpleDateFormat mDateFormat;
    private Calendar mCalendar;

    private RaceStateChangedListener mStateListener;

    private int mFlagSize;

    public MainScheduleFragment() {
        mCalendar = Calendar.getInstance();
    }

    public static MainScheduleFragment newInstance() {
        return new MainScheduleFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_schedule, container, false);

        mItems = new ArrayList<>();
        mStateListener = new RaceStateChangedListener();
        RecyclerView raceData = ViewHelper.get(layout, R.id.race_data);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mAdapter = new MainScheduleAdapter(getActivity(), mItems, this);
        raceData.setLayoutManager(layoutManager);
        raceData.setAdapter(mAdapter);

        View startButton = ViewHelper.get(layout, R.id.start_race);
        if (startButton != null) {
            startButton.setOnClickListener(this);
        }

        mDateFormat = new SimpleDateFormat("HH:mm:ss", getResources().getConfiguration().locale);
        mFlagSize = getResources().getInteger(R.integer.flag_size);

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initStartTime();
        initStartMode();
        initCourse();
        initWind();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getRace() != null && getRaceState() != null) {
            getRaceState().addChangedListener(mStateListener);
        }
    }

    private void initCourse() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                openFragment(CourseFragment.newInstance(0, getRace()));
            }
        };
        MainScheduleItem courseItem = new MainScheduleItem(getString(R.string.course), null, null, runnable);
        if (getRaceState().getCourseDesign() != null) {
            courseItem.setValue(getCourseName());
        }
        mItems.add(courseItem);
    }

    private void initWind() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                openFragment(WindFragment.newInstance(0));
            }
        };
        mItemStartWind = new MainScheduleItem(getString(R.string.wind), null, null, runnable);
        mItems.add(mItemStartWind);
    }

    private void initStartMode() {
        mRacingProcedureType = getRaceState().getRacingProcedure().getType();
        if (mRacingProcedureType != null) {
            Runnable runnableProcedure = new Runnable() {
                @Override
                public void run() {
                    openFragment(StartProcedureFragment.newInstance(0));
                }
            };
            mItems.add(new MainScheduleItem(getString(R.string.start_procedure), null, mRacingProcedureType.toString(), runnableProcedure));
            if (RacingProcedureType.RRS26.equals(mRacingProcedureType)) {
                // LineStart
                RRS26RacingProcedure procedure = getRaceState().getTypedRacingProcedure();
                Flags flag = procedure.getStartModeFlag();
                Runnable runnableMode = new Runnable() {
                    @Override
                    public void run() {
                        openFragment(StartModeFragment.newInstance(0));
                    }
                };
                Drawable drawable = FlagsResources.getFlagDrawable(getActivity(), flag.name(), mFlagSize);
                mItems.add(new MainScheduleItem(getString(R.string.start_mode), drawable, flag.name(), runnableMode));
            } if (RacingProcedureType.GateStart.equals(mRacingProcedureType)) {
                // GateStart
                mItems.add(new MainScheduleItem(getString(R.string.gate_start_pathfinder), null, null, null));
                mItems.add(new MainScheduleItem(getString(R.string.gate_start_timing), null, null, null));
            }
        }
    }

    private void initStartTime() {
        RacingActivity activity = (RacingActivity) getActivity();
        TimePoint timePoint = (TimePoint) getArguments().getSerializable(START_TIME);
        if (timePoint == null && activity != null) {
            timePoint = activity.getStartTime();
        }
        if (timePoint != null) {
            if (activity != null) {
                activity.setStartTime(timePoint);
            }
            mStartTime = timePoint;
            mStartTimeString = mDateFormat.format(timePoint.asDate());
        }

        mStartTimeDiff = (Duration) getArguments().getSerializable(START_TIME_DIFF);
        mRaceId = (SimpleRaceLogIdentifier) getArguments().getSerializable(DEPENDENT_RACE);

        if (mRaceId != null && mStartTimeDiff != null) {
            ManagedRace race = DataManager.create(getActivity()).getDataStore().getRace(mRaceId);
            mStartTimeString = getString(R.string.minutes_after_long, mStartTimeDiff.asMinutes(), RaceHelper.getRaceName(race, " / "));
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                openFragment(StartTimeFragment.newInstance(getArguments()));
            }
        };
        mItemStartTime = new MainScheduleItem(getString(R.string.start_time), null, mStartTimeString, runnable);
        mItems.add(mItemStartTime);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (getRace() != null && getRaceState() != null) {
            getRaceState().removeChangedListener(mStateListener);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_race:
                startRace();
                break;

            default:
                ExLog.i(getActivity(), TAG, "Clicked on " + v);
        }
    }

    private void startRace() {
        RRS26RacingProcedure procedure = null;
        Flags flag = null;
        if (getRaceState().getRacingProcedure().getType().equals(RacingProcedureType.RRS26)) {
            procedure = getRaceState().getTypedRacingProcedure();
            flag = procedure.getStartModeFlag();
        }
        TimePoint now = MillisecondsTimePoint.now();
        getRaceState().setRacingProcedure(now, mRacingProcedureType);
        if (mStartTimeDiff == null && mRaceId == null) {
            getRaceState().forceNewStartTime(now, mStartTime);
        } else {
            getRaceState().forceNewDependentStartTime(now, mStartTimeDiff, mRaceId);
        }
        if (procedure != null) {
            procedure.setStartModeFlag(MillisecondsTimePoint.now(), flag);
        }
    }

    @Override
    public void notifyTick(TimePoint now) {
        super.notifyTick(now);

        if (mItemStartTime != null && !TextUtils.isEmpty(mStartTimeString)) {
            if (mRaceId == null && mStartTimeDiff == null) {
                String startTimeValue = getString(R.string.start_time_value, mStartTimeString, calcCountdown(now));
                mItemStartTime.setValue(startTimeValue);
            } else {
                mItemStartTime.setValue(mStartTimeString);
            }
        }

        if (getRace() != null && getRaceState() != null && getRaceState().getWindFix() != null) {
            Wind wind = getRaceState().getWindFix();
            String sensorData = getString(R.string.wind_sensor, mDateFormat.format(wind.getTimePoint().asDate()), wind.getFrom().getDegrees(), wind
                .getKnots());
            mItemStartWind.setValue(sensorData);
        }

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private String calcCountdown(TimePoint timePoint) {
        Calendar now = (Calendar) mCalendar.clone();
        now.setTime(timePoint.asDate());

        RacingActivity activity = (RacingActivity) getActivity();
        Calendar startTime = (Calendar) mCalendar.clone();
        startTime.setTime(activity.getStartTime().asDate());

        return TimeUtils.calcDuration(TimeUtils.floorTime(now), TimeUtils.floorTime(startTime));
    }

    private void openFragment(RaceFragment fragment) {
        if (fragment.getArguments() != null) {
            fragment.getArguments().putAll(getRecentArguments());
        } else {
            fragment.setArguments(getRecentArguments());
        }

        Bundle args = fragment.getArguments();
        args.putSerializable(START_TIME, mStartTime);
        args.putSerializable(START_TIME_DIFF, mStartTimeDiff);
        args.putSerializable(DEPENDENT_RACE, mRaceId);

        getFragmentManager().beginTransaction().replace(R.id.racing_view_container, fragment).commitAllowingStateLoss();
    }

    @Override
    public void onItemClick(Runnable runnable) {
        if (runnable != null) {
            Handler handler = new Handler();
            handler.post(runnable);
        }
    }

    private class RaceStateChangedListener extends BaseRaceStateChangedListener {
        @Override
        public void onStatusChanged(ReadonlyRaceState state) {
            super.onStatusChanged(state);

            openFragment(RaceInfoRaceFragment.newInstance());
        }
    }
}
