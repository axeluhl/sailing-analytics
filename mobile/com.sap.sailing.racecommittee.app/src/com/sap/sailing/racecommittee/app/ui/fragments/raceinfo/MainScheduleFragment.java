package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.util.ArrayList;
import java.util.Calendar;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.scoring.AdditionalScoringInformationType;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.ess.ESSRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.GateStartRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line.ConfigurableStartModeFlagRacingProcedure;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.impl.SelectionItem;
import com.sap.sailing.racecommittee.app.ui.activities.RacingActivity;
import com.sap.sailing.racecommittee.app.ui.adapters.SelectionAdapter;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.chooser.RaceInfoFragmentChooser;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;
import com.sap.sailing.racecommittee.app.utils.RaceHelper;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MainScheduleFragment extends BaseFragment implements View.OnClickListener, SelectionAdapter.ItemClick {

    public static final String START_TIME = "startTime";
    public static final String DEPENDENT_RACE = "dependentRace";
    public static final String START_TIME_DIFF = "startTimeDiff";
    public static final String RACE_GROUP = "raceGroup";

    private static final String TAG = MainScheduleFragment.class.getName();

    private SelectionAdapter mAdapter;
    private ArrayList<SelectionItem> mItems;
    private SelectionItem mItemStartTime;
    private SelectionItem mItemRaceGroup;
    private SelectionItem mItemStartWind;

    private String mStartTimeString;
    private SimpleRaceLogIdentifier mRaceId;
    private TimePoint mStartTime;
    private Duration mStartTimeDiff;
    private RacingProcedure mRacingProcedure;
    private TimePoint lastTick;

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
        mAdapter = new SelectionAdapter(getActivity(), mItems, this);
        raceData.setLayoutManager(layoutManager);
        raceData.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        raceData.setAdapter(mAdapter);

        View startButton = ViewHelper.get(layout, R.id.start_race);
        if (startButton != null) {
            startButton.setOnClickListener(this);
        }

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
        lastTick = MillisecondsTimePoint.now().minus(2000);
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
                openFragment(CourseFragment.newInstance(START_MODE_PRESETUP, preferences));
            }
        };
        SelectionItem courseItem = new SelectionItem(getString(R.string.course), null, null, false, false, runnable);
        if (getRaceState().getCourseDesign() != null) {
            courseItem.setValue(getCourseName());
        }
        mItems.add(courseItem);
    }

    private void initWind() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                openFragment(WindFragment.newInstance(START_MODE_PRESETUP));
            }
        };
        mItemStartWind = new SelectionItem(getString(R.string.wind), null, null, false, false, runnable);
        mItems.add(mItemStartWind);
    }

    private void initStartMode() {
        mRacingProcedure = getRaceState().getRacingProcedure();
        if (mRacingProcedure != null) {
            Runnable runnableProcedure = new Runnable() {
                @Override
                public void run() {
                    openFragment(StartProcedureFragment.newInstance(START_MODE_PRESETUP));
                }
            };
            mItems.add(new SelectionItem(getString(R.string.start_procedure), mRacingProcedure.getType().toString(),
                    null, false, false, runnableProcedure));
            if (mRacingProcedure instanceof ConfigurableStartModeFlagRacingProcedure) {
                final ConfigurableStartModeFlagRacingProcedure procedure = getRaceState().getTypedRacingProcedure();
                Flags flag = procedure.getStartModeFlag();
                Runnable runnableMode = new Runnable() {
                    @Override
                    public void run() {
                        openFragment(StartModeFragment.newInstance(START_MODE_PRESETUP));
                    }
                };
                Drawable drawable = FlagsResources.getFlagDrawable(getActivity(), flag.name(), mFlagSize);
                mItems.add(new SelectionItem(getString(R.string.start_mode), flag.name(), drawable, false, false,
                        runnableMode));
            } else if (mRacingProcedure instanceof GateStartRacingProcedure) {
                GateStartRacingProcedure procedure = getRaceState().getTypedRacingProcedure();
                if (procedure != null) {
                    Runnable runnablePathfinder = new Runnable() {
                        @Override
                        public void run() {
                            openFragment(GateStartPathFinderFragment.newInstance(START_MODE_PRESETUP));
                        }
                    };
                    mItems.add(new SelectionItem(getString(R.string.gate_start_pathfinder), procedure.getPathfinder(),
                            null, false, false, runnablePathfinder));

                    Runnable runnableTiming = new Runnable() {
                        @Override
                        public void run() {
                            openFragment(GateStartTimingFragment.newInstance(START_MODE_PRESETUP));
                        }
                    };
                    mItems.add(new SelectionItem(getString(R.string.gate_start_timing),
                            RaceHelper.getGateTiming(getActivity(), procedure, getRace().getRaceGroup()), null, false,
                            false, runnableTiming));
                }
            } else if (mRacingProcedure instanceof ESSRacingProcedure) {
                ESSRacingProcedure procedure = getRaceState().getTypedRacingProcedure();
                if (procedure != null) {
                    boolean checked = getArguments().getBoolean(RACE_GROUP,
                            getRaceState().isAdditionalScoringInformationEnabled(
                                    AdditionalScoringInformationType.MAX_POINTS_DECREASE_MAX_SCORE));
                    mItemRaceGroup = new SelectionItem(getString(R.string.race_group), null, null, true, checked, null);
                    mItems.add(mItemRaceGroup);
                }
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
            mStartTimeString = TimeUtils.formatTime(timePoint);
        }

        mStartTimeDiff = (Duration) getArguments().getSerializable(START_TIME_DIFF);
        mRaceId = (SimpleRaceLogIdentifier) getArguments().getSerializable(DEPENDENT_RACE);

        if (mRaceId != null && mStartTimeDiff != null) {
            ManagedRace race = DataManager.create(getActivity()).getDataStore().getRace(mRaceId);
            mStartTimeString = getString(R.string.minutes_after_long, mStartTimeDiff.asMinutes(),
                    RaceHelper.getShortReverseRaceName(race, " / ", getRace()));
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                openFragment(StartTimeFragment.newInstance(getArguments()));
            }
        };
        mItemStartTime = new SelectionItem(getString(R.string.start_time), mStartTimeString, null, false, false,
                runnable);
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
        ConfigurableStartModeFlagRacingProcedure lineStartRacingProcedure = null;

        Flags flag = null;
        if (getRaceState().getRacingProcedure() instanceof ConfigurableStartModeFlagRacingProcedure) {
            lineStartRacingProcedure = getRaceState().getTypedRacingProcedure();
            flag = lineStartRacingProcedure.getStartModeFlag();
        }
        TimePoint now = MillisecondsTimePoint.now();
        getRaceState().setRacingProcedure(now, mRacingProcedure.getType());
        if (mRacingProcedure instanceof ESSRacingProcedure && mItemRaceGroup != null) {
            getRaceState().setAdditionalScoringInformationEnabled(MillisecondsTimePoint.now(),
                    /* enable */ mItemRaceGroup.isChecked(),
                    AdditionalScoringInformationType.MAX_POINTS_DECREASE_MAX_SCORE);
        }
        if (mStartTimeDiff == null && mRaceId == null) {
            getRaceState().forceNewStartTime(now, mStartTime);
        } else {
            getRaceState().forceNewDependentStartTime(now, mStartTimeDiff, mRaceId);
        }
        if (lineStartRacingProcedure != null) {
            lineStartRacingProcedure.setStartModeFlag(MillisecondsTimePoint.now(), flag);
        }
    }

    @Override
    public void notifyTick(TimePoint now) {
        super.notifyTick(now);

        if (!now.minus(1000).before(lastTick)) {
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
                String sensorData = getString(R.string.wind_sensor, TimeUtils.formatTime(wind.getTimePoint()),
                        wind.getFrom().getDegrees(), wind.getKnots());
                mItemStartWind.setValue(sensorData);
            }

            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
            lastTick = now;
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
        if (mItemRaceGroup != null) {
            args.putBoolean(RACE_GROUP, mItemRaceGroup.isChecked());
        }

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

            openFragment(
                    RaceInfoFragmentChooser.on(state.getRacingProcedure().getType()).choose(getActivity(), getRace()));
        }
    }
}
