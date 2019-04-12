package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.sap.sailing.android.shared.util.AppUtils;
import com.sap.sailing.android.shared.util.BitmapHelper;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinderResult;
import com.sap.sailing.domain.abstractlog.race.impl.SimpleRaceLogIdentifierImpl;
import com.sap.sailing.domain.abstractlog.race.state.RaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.domain.base.racegroup.RaceGroupSeriesFleet;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataStore;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.ui.adapters.DependentRaceSpinnerAdapter;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.util.NaturalComparator;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StartTimeFragment extends BaseFragment
        implements View.OnClickListener, NumberPicker.OnValueChangeListener, TimePicker.OnTimeChangedListener {

    public static final int MODE_TIME_PANEL = 2;

    private static final int FUTURE_DAYS = 25;
    private static final int PAST_DAYS = 3;
    private static final int MAX_DIFF_MIN = 60;
    private static final int NONE = -1;
    private static final int ABSOLUTE = 0;
    private static final int RELATIVE = 1;

    private View mAbsolute;
    private View mRelative;
    private Button mAbsoluteButton;
    private Button mRelativeButton;
    private Button mSetStartAbsolute;
    private Button mSetStartRelative;

    private NumberPicker mDatePicker;
    private NumberPicker mTimeOffset;
    private Spinner mLeaderBoard;
    private Spinner mFleet;
    private Spinner mRace;
    private boolean mRaceSetupFinished;
    private TimePicker mTimePicker;
    private NumberPicker mStartSeconds;
    private TextView mCountdown;
    private TextView mDebugTime;
    private TimePoint mStartTime;
    private SimpleRaceLogIdentifier mRaceId;
    private Duration mStartTimeOffset;
    private boolean mListenerIgnore = true;
    private Map<RaceGroupSeriesFleet, List<ManagedRace>> mGroupHeaders;
    private DependentRaceSpinnerAdapter mLeaderBoardAdapter;
    private DependentRaceSpinnerAdapter mFleetAdapter;
    private DependentRaceSpinnerAdapter mRaceAdapter;
    private SimpleRaceLogIdentifier identifier;

    /**
     * Listens for start time changes
     */
    private RaceStateChangedListener raceStateChangedListener;

    public static StartTimeFragment newInstance(int startMode) {
        StartTimeFragment fragment = new StartTimeFragment();
        Bundle args = new Bundle();
        args.putInt(START_MODE, startMode);
        fragment.setArguments(args);
        return fragment;
    }

    public static StartTimeFragment newInstance(Bundle extraArgs) {
        StartTimeFragment fragment = newInstance(START_MODE_PRESETUP);
        Bundle args = fragment.getArguments();
        if (extraArgs != null) {
            args.putAll(extraArgs);
        }
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_schedule_start_time, container, false);

        mAbsolute = ViewHelper.get(layout, R.id.time_absolute);
        mRelative = ViewHelper.get(layout, R.id.time_relative);

        if (preferences.isDependentRacesAllowed()) {
            View view = ViewHelper.get(layout, R.id.tab_button);
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            }

            mAbsoluteButton = ViewHelper.get(layout, R.id.absolute);
            if (mAbsoluteButton != null) {
                mAbsoluteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showTab(ABSOLUTE);
                    }
                });
            }

            mRelativeButton = ViewHelper.get(layout, R.id.relative);
            if (mRelativeButton != null) {
                mRelativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showTab(RELATIVE);
                    }
                });
            }
        }

        mCountdown = ViewHelper.get(layout, R.id.start_countdown);

        Button syncMinute = ViewHelper.get(layout, R.id.sync_to_minute);
        if (syncMinute != null) {
            syncMinute.setOnClickListener(this);
        }

        mSetStartAbsolute = ViewHelper.get(layout, R.id.set_start_time_absolute);
        if (mSetStartAbsolute != null) {
            mSetStartAbsolute.setOnClickListener(this);
        }

        mSetStartRelative = ViewHelper.get(layout, R.id.set_start_time_relative);
        if (mSetStartRelative != null) {
            mSetStartRelative.setOnClickListener(this);
        }

        mDebugTime = ViewHelper.get(layout, R.id.debug_time);
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        raceStateChangedListener = new BaseRaceStateChangedListener() {
            @Override
            public void onStartTimeChanged(ReadonlyRaceState state) {
                mStartTime = state.getStartTime();
                notifyTick(MillisecondsTimePoint.now());
            }
        };
        Calendar time = Calendar.getInstance();
        if (getView() != null && getArguments() != null) {
            View header = ViewHelper.get(getView(), R.id.header);
            View back = ViewHelper.get(getView(), R.id.header_back);
            View text = ViewHelper.get(getView(), R.id.header_text);
            View sync = ViewHelper.get(getView(), R.id.sync_to_minute);
            switch (getArguments().getInt(START_MODE, START_MODE_PRESETUP)) {
            case START_MODE_PLANNED:
                if (back != null) {
                    back.setVisibility(View.VISIBLE);
                }
                if (text != null) {
                    text.setOnClickListener(this);
                }
                break;

            case MODE_TIME_PANEL:
                if (getRace() != null && getRaceState() != null) {
                    mStartTime = getRaceState().getStartTime();
                    if (mStartTime != null) {
                        time.setTime(mStartTime.asDate());
                    }
                }
                if (AppUtils.with(getActivity()).isLandscape()) {
                    if (header != null) {
                        header.setVisibility(View.GONE);
                    }
                } else {
                    if (back != null) {
                        back.setVisibility(View.VISIBLE);
                    }
                    if (text != null) {
                        text.setOnClickListener(this);
                    }
                }
                if (sync != null) {
                    sync.setVisibility(View.VISIBLE);
                }

                StartTimeFinderResult result = getRaceState().getStartTimeFinderResult();
                if (result != null && result.isDependentStartTime()) {
                    mStartTimeOffset = result.getStartTimeDiff();
                    mRaceId = Util.get(result.getDependingOnRaces(), 0);
                }
                break;

            default: // MODE_SETUP
                mStartTime = (TimePoint) getArguments().getSerializable(MainScheduleFragment.START_TIME);
                if (mStartTime != null) {
                    time.setTime(mStartTime.asDate());
                }
                mStartTimeOffset = (Duration) getArguments().getSerializable(MainScheduleFragment.START_TIME_DIFF);
                mRaceId = (SimpleRaceLogIdentifier) getArguments().getSerializable(MainScheduleFragment.DEPENDENT_RACE);

                View syncButtons = ViewHelper.get(getView(), R.id.buttonBar);
                if (syncButtons != null) {
                    syncButtons.setVisibility(View.GONE);
                }

                View startSeconds = ViewHelper.get(getView(), R.id.start_time_seconds);
                if (startSeconds != null) {
                    startSeconds.setVisibility(View.GONE);
                }
                break;
            }
        }

        if (mRaceId == null && mStartTimeOffset == null) {
            showTab(ABSOLUTE);
        } else {
            showTab(RELATIVE);
        }

        initViewsAbsolute(time);
        initViewsRelative();

        // reset Set Time button after init
        if (getArguments() != null && getArguments().getInt(START_MODE, START_MODE_PRESETUP) == MODE_TIME_PANEL) {
            activateSetTime(NONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        getRaceState().removeChangedListener(raceStateChangedListener);
    }

    @Override
    public void onResume() {
        super.onResume();

        getRaceState().addChangedListener(raceStateChangedListener);
    }

    private void initViewsRelative() {
        mTimeOffset = ViewHelper.get(getView(), R.id.time_offset);
        if (mTimeOffset != null) {
            ViewHelper.disableSave(mTimeOffset);
            ThemeHelper.setPickerColor(getActivity(), mTimeOffset, ThemeHelper.getColor(getActivity(), R.attr.white),
                    ThemeHelper.getColor(getActivity(), R.attr.sap_yellow_1));
            mTimeOffset.setMinValue(0);
            mTimeOffset.setMaxValue(MAX_DIFF_MIN);
            mTimeOffset.setWrapSelectorWheel(false);
            mTimeOffset.setValue((mStartTimeOffset == null) ? preferences.getDependentRacesOffset()
                    : (int) mStartTimeOffset.asMinutes());
            mTimeOffset.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    activateSetTime(RELATIVE);
                }
            });
        }

        final DataStore manager = OnlineDataManager.create(getActivity()).getDataStore();
        mGroupHeaders = new LinkedHashMap<>();
        List<ManagedRace> sortedRaces = new ArrayList<>(manager.getRaces());
        Collections.sort(sortedRaces, new Comparator<ManagedRace>() {
            @Override
            public int compare(ManagedRace lhs, ManagedRace rhs) {
                return new NaturalComparator().compare(lhs.getId(), rhs.getId());
            }
        });

        for (ManagedRace race : sortedRaces) {
            RaceGroupSeriesFleet container = new RaceGroupSeriesFleet(race);

            if (!mGroupHeaders.containsKey(container)) {
                mGroupHeaders.put(container, new LinkedList<ManagedRace>());
            }
            mGroupHeaders.get(container).add(race);
        }

        mLeaderBoard = ViewHelper.get(getView(), R.id.dependent_leaderboard);
        if (mLeaderBoard != null) {
            int leaderBoard = -1;
            mLeaderBoardAdapter = new DependentRaceSpinnerAdapter(getActivity(), R.layout.dependent_race_item);
            for (RaceGroupSeriesFleet races : mGroupHeaders.keySet()) {
                Util.Pair<String, String> data = new Util.Pair<>(races.getRaceGroup().getName(),
                        races.getRaceGroup().getDisplayName());
                int position = mLeaderBoardAdapter.add(data);
                if (position >= 0) {
                    if (mRaceId != null) {
                        if (mRaceId.getRegattaLikeParentName().equals(data.getA())) {
                            leaderBoard = position;
                        }
                    } else {
                        if (leaderBoard == -1
                                && getRace().getRaceGroup().getName().equals(races.getRaceGroup().getName())) {
                            leaderBoard = position;
                        }
                    }
                }
            }

            mLeaderBoard.setAdapter(mLeaderBoardAdapter);
            mLeaderBoard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mLeaderBoardAdapter.setSelected(position);
                    initFleetSpinner();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    mLeaderBoardAdapter.setSelected(0);
                }
            });
            mLeaderBoard.setSelection(leaderBoard);
        }

        mFleet = ViewHelper.get(getView(), R.id.dependent_fleet);
        mRace = ViewHelper.get(getView(), R.id.dependent_racegroup);
    }

    private void initFleetSpinner() {
        if (mLeaderBoard != null && mFleet != null) {
            int fleet = -1;
            mFleetAdapter = new DependentRaceSpinnerAdapter(getActivity(), R.layout.dependent_race_item);
            mFleet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mFleetAdapter.setSelected(position);
                    initRaceSpinner();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    mFleetAdapter.setSelected(0);
                }
            });
            for (RaceGroupSeriesFleet races : mGroupHeaders.keySet()) {
                Util.Pair<String, String> leaderBoard = mLeaderBoardAdapter
                        .getItem(mLeaderBoard.getSelectedItemPosition());
                if (races.getRaceGroup().getName().equals(leaderBoard.getA())) {
                    Util.Pair<String, String> data = new Util.Pair<>(races.getFleet().getName(), null);
                    if (hasRaces(data.getA())) {
                        int position = mFleetAdapter.add(data);
                        if (position >= 0) {
                            if (mRaceId != null) {
                                if (mRaceId.getFleetName().equals(data.getA())) {
                                    fleet = position;
                                }
                            } else {
                                if (fleet == -1) { // TODO add more heuristic here - to be discussed
                                    fleet = position;
                                }
                            }
                        }
                    }
                }
            }

            if (mFleetAdapter.getCount() > 1 || (mFleetAdapter.getCount() == 1
                    && !mFleetAdapter.getItem(0).getA().equals(LeaderboardNameConstants.DEFAULT_FLEET_NAME))) {
                mFleet.setAdapter(mFleetAdapter);
                mFleet.setSelection(fleet);
                mFleet.setVisibility(View.VISIBLE);
            } else {
                mFleet.setAdapter(null);
                mFleet.setVisibility(View.GONE);
                initRaceSpinner();
            }
        }
    }

    private void initRaceSpinner() {
        if (mLeaderBoard != null && mFleet != null && mRace != null) {
            mRaceAdapter = new DependentRaceSpinnerAdapter(getActivity(), R.layout.dependent_race_item);
            mRace.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mRaceAdapter.setSelected(position);
                    String leaderBoard = mLeaderBoardAdapter.getItem(mLeaderBoard.getSelectedItemPosition()).getA();
                    @SuppressWarnings("unchecked")
                    Util.Pair<String, String> fleet = ((Util.Pair<String, String>) mFleet.getSelectedItem());
                    if (fleet == null) {
                        fleet = new Util.Pair<>(LeaderboardNameConstants.DEFAULT_FLEET_NAME, null);
                    }
                    String race = mRaceAdapter.getItem(mRace.getSelectedItemPosition()).getA();
                    identifier = new SimpleRaceLogIdentifierImpl(leaderBoard, race, fleet.getA());
                    if (mRaceSetupFinished) {
                        activateSetTime(RELATIVE);
                    }
                    mRaceSetupFinished = true;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    mRaceAdapter.setSelected(0);
                    identifier = null;
                }
            });

            int racePos = -1;
            for (RaceGroupSeriesFleet races : mGroupHeaders.keySet()) {
                Util.Pair<String, String> leaderBoard = mLeaderBoardAdapter
                        .getItem(mLeaderBoard.getSelectedItemPosition());
                if (races.getRaceGroup().getName().equals(leaderBoard.getA())) {
                    @SuppressWarnings("unchecked")
                    Util.Pair<String, String> fleet = ((Util.Pair<String, String>) mFleet.getSelectedItem());
                    if (fleet == null) {
                        fleet = new Util.Pair<>(LeaderboardNameConstants.DEFAULT_FLEET_NAME, null);
                    }
                    if (races.getFleet().getName().equals(fleet.getA())) {
                        for (ManagedRace race : mGroupHeaders.get(races)) {
                            if (!getRace().equals(race) && race.getStatus() != RaceLogRaceStatus.FINISHED) {
                                Util.Pair<String, String> data = new Util.Pair<>(race.getRaceColumnName(), null);
                                int position = mRaceAdapter.add(data);
                                if (position >= 0) {
                                    if (mRaceId != null) {
                                        if (mRaceId.getRaceColumnName().equals(data.getA())) {
                                            racePos = position;
                                        }
                                    } else {
                                        if (racePos == -1) { // TODO add more heuristic here - to be discussed
                                            racePos = position;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            mRace.setAdapter(mRaceAdapter);
            mRace.setVisibility(View.VISIBLE);
            mRace.setSelection(racePos);
            if (mRaceAdapter.getCount() > 1) {
                mRace.setEnabled(true);
            } else {
                mRace.setEnabled(false);
            }
        }
    }

    private boolean hasRaces(@Nullable String fleet) {
        for (RaceGroupSeriesFleet races : mGroupHeaders.keySet()) {
            Util.Pair<String, String> leaderBoard = mLeaderBoardAdapter.getItem(mLeaderBoard.getSelectedItemPosition());
            if (races.getRaceGroup().getName().equals(leaderBoard.getA())) {
                if (races.getFleet().getName().equals(fleet)) {
                    for (ManagedRace race : mGroupHeaders.get(races)) {
                        if (!getRace().equals(race) && race.getStatus() != RaceLogRaceStatus.FINISHED) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void initViewsAbsolute(Calendar time) {
        mDatePicker = ViewHelper.get(getView(), R.id.start_date_picker);
        if (mDatePicker != null) {
            ViewHelper.disableSave(mDatePicker);
            ThemeHelper.setPickerColor(getActivity(), mDatePicker, ThemeHelper.getColor(getActivity(), R.attr.white),
                    ThemeHelper.getColor(getActivity(), R.attr.sap_yellow_1));
            mDatePicker.setOnValueChangedListener(this);
            TimeUtils.initDatePicker(getActivity(), mDatePicker, time, -PAST_DAYS, FUTURE_DAYS);
            mDatePicker.setValue(PAST_DAYS);
        }
        mTimePicker = ViewHelper.get(getView(), R.id.start_time_picker);
        if (mTimePicker != null) {
            ThemeHelper.setPickerColor(getActivity(), mTimePicker, ThemeHelper.getColor(getActivity(), R.attr.white),
                    ThemeHelper.getColor(getActivity(), R.attr.sap_yellow_1));
            mTimePicker.setOnTimeChangedListener(this);
            mTimePicker.setIs24HourView(true);
            int hours = time.get(Calendar.HOUR_OF_DAY);
            int minutes = time.get(Calendar.MINUTE);
            if (getArguments() != null && getArguments().getInt(START_MODE, START_MODE_PRESETUP) != MODE_TIME_PANEL
                    && getArguments().getSerializable(MainScheduleFragment.START_TIME) == null) {
                // In 10 minutes from now, but always a 5-minute-mark.
                time.add(Calendar.MINUTE, 10);
                hours = time.get(Calendar.HOUR_OF_DAY);
                minutes = time.get(Calendar.MINUTE);
                minutes = (int) (Math.ceil((minutes / 5.0)) * 5.0);
                if (minutes >= 60) {
                    hours++;
                    minutes = 0;
                }
            }
            mTimePicker.setCurrentHour(hours);
            mTimePicker.setCurrentMinute(minutes);
            mTimePicker.setTag(time.get(Calendar.SECOND));
        }

        mStartSeconds = ViewHelper.get(getView(), R.id.start_time_seconds);
        if (mStartSeconds != null) {
            ViewHelper.disableSave(mStartSeconds);
            ThemeHelper.setPickerColor(getActivity(), mStartSeconds,
                    ThemeHelper.getColor(getActivity(), R.attr.sap_light_gray),
                    ThemeHelper.getColor(getActivity(), R.attr.sap_light_gray));
            ThemeHelper.setPickerTextSize(getActivity(), mStartSeconds, R.dimen.textSize_14);
            mStartSeconds.setEnabled(false);
            setSeconds();
        }
    }

    @Override
    public void notifyTick(TimePoint now) {
        super.notifyTick(now);

        if (mAbsolute.getVisibility() == View.VISIBLE) {
            int resId;
            TimePoint timePoint;
            String time;
            if (mStartTime == null) {
                mStartTime = getPickerTime();
            }
            if (mStartTime.after(now)) {
                resId = R.string.race_start_time_in;
                timePoint = mStartTime.minus(now.asMillis());
                time = TimeUtils.formatDurationUntil(timePoint.asMillis());
            } else {
                resId = R.string.race_start_time_ago;
                timePoint = now.minus(mStartTime.asMillis());
                time = TimeUtils.formatDurationSince(timePoint.asMillis());
            }

            if (mCountdown != null) {
                String countdown = getString(resId, time);
                mCountdown.setText(countdown);
                mCountdown.setTag(resId);
            }

            if (mDebugTime != null) {
                mDebugTime.setText(mStartTime.asDate().toString());
            }
        }
    }

    private void showTab(int tab) {
        int colorGrey = ThemeHelper.getColor(getActivity(), R.attr.sap_light_gray);
        int colorOrange = ThemeHelper.getColor(getActivity(), R.attr.sap_yellow_1);

        if (mAbsolute != null) {
            mAbsolute.setVisibility(View.GONE);
        }
        if (mRelative != null) {
            mRelative.setVisibility(View.GONE);
        }
        if (mAbsoluteButton != null) {
            mAbsoluteButton.setTextColor(colorGrey);
            BitmapHelper.setBackground(mAbsoluteButton, null);
        }
        if (mRelativeButton != null) {
            mRelativeButton.setTextColor(colorGrey);
            BitmapHelper.setBackground(mRelativeButton, null);
        }

        Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.nav_drawer_tab_button);
        switch (tab) {
        case RELATIVE:
            if (mRelative != null) {
                mRelative.setVisibility(View.VISIBLE);
            }
            if (mRelativeButton != null) {
                mRelativeButton.setTextColor(colorOrange);
                BitmapHelper.setBackground(mRelativeButton, drawable);
            }
            break;

        default: // ABSOLUTE
            if (mAbsolute != null) {
                mAbsolute.setVisibility(View.VISIBLE);
            }
            if (mAbsoluteButton != null) {
                mAbsoluteButton.setTextColor(colorOrange);
                BitmapHelper.setBackground(mAbsoluteButton, drawable);
            }
            break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.set_start_time_absolute:
            changeFragment(mStartTime);
            break;

        case R.id.set_start_time_relative:
            changeFragment(mStartTime, new MillisecondsDurationImpl(mTimeOffset.getValue() * 60 * 1000), identifier);
            break;

        case R.id.sync_to_minute:
            syncToMinute();
            break;

        case R.id.header_text:
            if (getArguments() != null
                    && getArguments().getInt(START_MODE, START_MODE_PRESETUP) == START_MODE_PRESETUP) {
                changeFragment();
            } else {
                sendIntent(AppConstants.INTENT_ACTION_CLEAR_TOGGLE);
                sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
            }
            break;

        default:
            break;
        }
    }

    private void syncToMinute() {
        final TimePoint now = MillisecondsTimePoint.now();
        mStartTime = mStartTime.getNearestModuloOneMinute(now);
        mListenerIgnore = true;
        setPickerTime();
        activateSetTime(ABSOLUTE);
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        if (!mListenerIgnore) {
            mStartTime = new MillisecondsTimePoint(getPickerTime().asMillis());
            setSeconds();
            activateSetTime(ABSOLUTE);
        }
        mListenerIgnore = false;
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        if (!mListenerIgnore) {
            mStartTime = new MillisecondsTimePoint(getPickerTime().asMillis());
            setSeconds();
            activateSetTime(ABSOLUTE);
        }
        mListenerIgnore = false;
    }

    private void activateSetTime(int tab) {
        switch (tab) {
        case ABSOLUTE:
            if (mSetStartAbsolute != null) {
                mSetStartAbsolute.setEnabled(true);
            }
            break;

        case RELATIVE:
            if (mSetStartRelative != null) {
                mSetStartRelative.setEnabled(true);
            }
            break;

        default:
            if (mSetStartAbsolute != null) {
                mSetStartAbsolute.setEnabled(false);
            }
            if (mSetStartRelative != null) {
                mSetStartRelative.setEnabled(false);
            }
        }
    }

    private void setSeconds() {
        setSeconds(0, 0);
    }

    private void setSeconds(int sec, int msec) {
        if (mStartSeconds != null) {
            double seconds = sec + msec / (double) 1000;
            DecimalFormat format = new DecimalFormat("00.000");
            mStartSeconds.setDisplayedValues(new String[] { format.format(seconds) });
        }
    }

    /**
     * sets the time displayed in the time picker to the value of {@link #mStartTime}
     */
    private void setPickerTime() {
        Calendar today = Calendar.getInstance();
        Calendar newTime = (Calendar) today.clone();
        newTime.setTime(mStartTime.asDate());

        int days = TimeUtils.daysBetween(today, newTime) + PAST_DAYS;

        if (mDatePicker != null) {
            mDatePicker.setValue(days);
        }
        if (mTimePicker != null) {
            mTimePicker.setCurrentHour(newTime.get(Calendar.HOUR_OF_DAY));
            mTimePicker.setCurrentMinute(newTime.get(Calendar.MINUTE));
        }

        setSeconds(newTime.get(Calendar.SECOND), newTime.get(Calendar.MILLISECOND));
    }

    private TimePoint getPickerTime() {
        Calendar calendar = Calendar.getInstance();
        if (mDatePicker != null) {
            calendar.add(Calendar.DAY_OF_MONTH, mDatePicker.getValue() - PAST_DAYS);
        }
        if (mTimePicker != null) {
            calendar.set(Calendar.HOUR_OF_DAY, mTimePicker.getCurrentHour());
            calendar.set(Calendar.MINUTE, mTimePicker.getCurrentMinute());
        }
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return new MillisecondsTimePoint(calendar.getTime());
    }

    private void changeFragment() {
        changeFragment(null, null, null);
    }

    private void changeFragment(TimePoint startTime) {
        changeFragment(startTime, null, null);
    }

    private void changeFragment(TimePoint startTime, Duration startTimeDiff, SimpleRaceLogIdentifier identifier) {
        int viewId = R.id.racing_view_container;
        TimePoint now = MillisecondsTimePoint.now();
        RaceFragment fragment = MainScheduleFragment.newInstance();
        Bundle args = getRecentArguments();
        RacingProcedureType procedureType = getRaceState().getTypedRacingProcedure().getType();
        getRaceState().setRacingProcedure(now, procedureType);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (getArguments() != null && startTime != null) {
            if (getArguments().getInt(START_MODE, START_MODE_PRESETUP) != START_MODE_PRESETUP) {
                if (startTimeDiff == null && identifier == null) {
                    // absolute start time
                    getRaceState().forceNewStartTime(now, startTime);
                } else {
                    // relative start time
                    getRaceState().forceNewDependentStartTime(now, startTimeDiff, identifier);
                }
                fragment = RaceFlagViewerFragment.newInstance();
                viewId = R.id.race_content;
            }
            args.putAll(getArguments());
            if (startTimeDiff == null) {
                args.putSerializable(MainScheduleFragment.START_TIME, startTime);
            } else {
                args.putSerializable(MainScheduleFragment.START_TIME_DIFF, startTimeDiff);
                args.putSerializable(MainScheduleFragment.DEPENDENT_RACE, identifier);
            }
        }
        fragment.setArguments(args);
        transaction.replace(viewId, fragment);
        transaction.commit();
        sendIntent(AppConstants.INTENT_ACTION_CLEAR_TOGGLE);
        if (getActivity().findViewById(R.id.race_edit) != null) {
            sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
        }
    }
}
