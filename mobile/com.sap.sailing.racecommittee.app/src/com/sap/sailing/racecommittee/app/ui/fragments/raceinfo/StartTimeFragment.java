package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

import com.sap.sailing.android.shared.util.BroadcastManager;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinderResult;
import com.sap.sailing.domain.abstractlog.race.impl.SimpleRaceLogIdentifierImpl;
import com.sap.sailing.domain.abstractlog.race.state.RaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataStore;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.impl.RaceGroupSeriesFleet;
import com.sap.sailing.racecommittee.app.ui.adapters.DependentRaceSpinnerAdapter;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.utils.BitmapHelper;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.util.NaturalComparator;

public class StartTimeFragment extends BaseFragment
    implements View.OnClickListener, NumberPicker.OnValueChangeListener, TimePicker.OnTimeChangedListener {

    public static final int MODE_SETUP = 0;
    public static final int MODE_1 = 1;
    public static final int MODE_TIME_PANEL = 2;

    private static final String START_MODE = "startMode";
    private static final int FUTURE_DAYS = 25;
    private static final int PAST_DAYS = -3;
    private static final int MAX_DIFF_MIN = 60;
    private static final String ZERO_TIME = "-00:00:00";
    private static final int ABSOLUTE = 0;
    private static final int RELATIVE = 1;

    private View mAbsolute;
    private View mRelative;
    private Button mAbsoluteButton;
    private Button mRelativeButton;

    private NumberPicker mDatePicker;
    private NumberPicker mTimeOffset;
    private Spinner mLeaderBoard;
    private Spinner mFleet;
    private Spinner mRace;
    private TimePicker mTimePicker;
    private TextView mCountdown;
    private TextView mDebugTime;
    private Button mMinuteInc;
    private Button mMinuteDec;
    private TimePoint mStartTime;
    private Calendar mTimeLeft;
    private Calendar mTimeRight;
    private Calendar mCalendar;
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

    public StartTimeFragment() {
        mCalendar = Calendar.getInstance();
    }

    public static StartTimeFragment newInstance(int startMode) {
        StartTimeFragment fragment = new StartTimeFragment();
        Bundle args = new Bundle();
        args.putInt(START_MODE, startMode);
        fragment.setArguments(args);
        return fragment;
    }

    public static StartTimeFragment newInstance(Bundle extraArgs) {
        StartTimeFragment fragment = newInstance(MODE_SETUP);
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
        mMinuteInc = ViewHelper.get(layout, R.id.minute_inc);
        if (mMinuteInc != null) {
            mMinuteInc.setOnClickListener(this);
        }

        mMinuteDec = ViewHelper.get(layout, R.id.minute_dec);
        if (mMinuteDec != null) {
            mMinuteDec.setOnClickListener(this);
        }

        View setStartAbsolute = ViewHelper.get(layout, R.id.set_start_time_absolute);
        if (setStartAbsolute != null) {
            setStartAbsolute.setOnClickListener(this);
        }

        View setStartRelative = ViewHelper.get(layout, R.id.set_start_time_relative);
        if (setStartRelative != null) {
            setStartRelative.setOnClickListener(this);
        }

        mDebugTime = ViewHelper.get(layout, R.id.debug_time);
        return layout;
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
        if (getView() != null) {
            if (getArguments() != null) {
                switch (getArguments().getInt(START_MODE, MODE_SETUP)) {
                    case MODE_1:
                        View header = ViewHelper.get(getView(), R.id.header_text);
                        if (header != null) {
                            header.setOnClickListener(this);
                        }
                        View back = ViewHelper.get(getView(), R.id.header_back);
                        if (back != null) {
                            back.setVisibility(View.VISIBLE);
                        }
                        break;

                    case MODE_TIME_PANEL:
                        if (getRace() != null && getRaceState() != null) {
                            mStartTime = getRaceState().getStartTime();
                            if (mStartTime != null) {
                                time.setTime(mStartTime.asDate());
                            }
                        }
                        View frame = ViewHelper.get(getView(), R.id.header);
                        if (frame != null) {
                            frame.setVisibility(View.GONE);
                        }

                        StartTimeFinderResult result = getRaceState().getStartTimeFinderResult();
                        if (result != null && result.isDependentStartTime()) {
                            mStartTimeOffset = result.getStartTimeDiff();
                            mRaceId = Util.get(result.getRacesDependingOn(), 0);
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
        }
    }

    private void initViewsRelative() {
        mTimeOffset = ViewHelper.get(getView(), R.id.time_offset);
        if (mTimeOffset != null) {
            ViewHelper.disableSave(mTimeOffset);
            ThemeHelper.setPickerColor(getActivity(), mTimeOffset, ThemeHelper.getColor(getActivity(), R.attr.white), ThemeHelper
                .getColor(getActivity(), R.attr.sap_yellow_1));
            mTimeOffset.setMinValue(0);
            mTimeOffset.setMaxValue(MAX_DIFF_MIN);
            mTimeOffset.setWrapSelectorWheel(false);
            mTimeOffset.setValue((mStartTimeOffset == null) ? preferences.getDependentRacesOffset() : (int) mStartTimeOffset.asMinutes());
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
                Util.Pair<String, String> data = new Util.Pair<>(races.getRaceGroup().getName(), races.getRaceGroup().getDisplayName());
                int position = mLeaderBoardAdapter.add(data);
                if (position >= 0) {
                    if (mRaceId != null) {
                        if (mRaceId.getRegattaLikeParentName().equals(data.getA())) {
                            leaderBoard = position;
                        }
                    } else {
                        if (leaderBoard == -1 && getRace().getRaceGroup().getName().equals(races.getRaceGroup().getName())) {
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
                Util.Pair<String, String> leaderBoard = mLeaderBoardAdapter.getItem(mLeaderBoard.getSelectedItemPosition());
                if (races.getRaceGroup().getName().equals(leaderBoard.getA())) {
                    Util.Pair<String, String> data = new Util.Pair<>(races.getFleet().getName(), null);
                    int position = mFleetAdapter.add(data);
                    if (position >= 0) {
                        if (mRaceId != null) {
                            if (mRaceId.getFleetName().equals(data.getA())) {
                                fleet = position;
                            }
                        } else {
                            if (fleet == -1) { //TODO add more heuristic here - to be discussed
                                fleet = position;
                            }
                        }
                    }
                }
            }

            if (mFleetAdapter.getCount() > 1 || (mFleetAdapter.getCount() == 1 && !mFleetAdapter.getItem(0).getA()
                .equals(LeaderboardNameConstants.DEFAULT_FLEET_NAME))) {
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
                    @SuppressWarnings("unchecked") Util.Pair<String, String> fleet = ((Util.Pair<String, String>) mFleet.getSelectedItem());
                    if (fleet == null) {
                        fleet = new Util.Pair<>(LeaderboardNameConstants.DEFAULT_FLEET_NAME, null);
                    }
                    String race = mRaceAdapter.getItem(mRace.getSelectedItemPosition()).getA();
                    identifier = new SimpleRaceLogIdentifierImpl(leaderBoard, race, fleet.getA());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    mRaceAdapter.setSelected(0);
                    identifier = null;
                }
            });

            int racePos = -1;
            for (RaceGroupSeriesFleet races : mGroupHeaders.keySet()) {
                Util.Pair<String, String> leaderBoard = mLeaderBoardAdapter.getItem(mLeaderBoard.getSelectedItemPosition());
                if (races.getRaceGroup().getName().equals(leaderBoard.getA())) {
                    @SuppressWarnings("unchecked") Util.Pair<String, String> fleet = ((Util.Pair<String, String>) mFleet.getSelectedItem());
                    if (fleet == null) {
                        fleet = new Util.Pair<>(LeaderboardNameConstants.DEFAULT_FLEET_NAME, null);
                    }
                    if (races.getFleet().getName().equals(fleet.getA())) {
                        for (ManagedRace race : mGroupHeaders.get(races)) {
                            if (!getRace().equals(race)) {
                                Util.Pair<String, String> data = new Util.Pair<>(race.getRaceName(), null);
                                int position = mRaceAdapter.add(data);
                                if (position >= 0) {
                                    if (mRaceId != null) {
                                        if (mRaceId.getRaceColumnName().equals(data.getA())) {
                                            racePos = position;
                                        }
                                    } else {
                                        if (racePos == -1) { //TODO add more heuristic here - to be discussed
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
        }
    }

    private void initViewsAbsolute(Calendar time) {
        mDatePicker = ViewHelper.get(getView(), R.id.start_date_picker);
        if (mDatePicker != null) {
            ViewHelper.disableSave(mDatePicker);
            ThemeHelper.setPickerColor(getActivity(), mDatePicker, ThemeHelper.getColor(getActivity(), R.attr.white), ThemeHelper
                .getColor(getActivity(), R.attr.sap_yellow_1));
            mDatePicker.setOnValueChangedListener(this);
            TimeUtils.initDatePicker(getActivity(), mDatePicker, time, PAST_DAYS, FUTURE_DAYS);
            mDatePicker.setValue(Math.abs(PAST_DAYS));
        }
        mTimePicker = ViewHelper.get(getView(), R.id.start_time_picker);
        if (mTimePicker != null) {
            ThemeHelper.setPickerColor(getActivity(), mTimePicker, ThemeHelper.getColor(getActivity(), R.attr.white), ThemeHelper
                .getColor(getActivity(), R.attr.sap_yellow_1));
            mTimePicker.setOnTimeChangedListener(this);
            mTimePicker.setIs24HourView(true);
            int hours = time.get(Calendar.HOUR_OF_DAY);
            int minutes = time.get(Calendar.MINUTE);
            if (getArguments() != null && getArguments().getInt(START_MODE, MODE_SETUP) != MODE_TIME_PANEL && mRaceId == null
                && mStartTimeOffset == null) {
                // In 10 minutes from now, but always a 5-minute-mark.
                time.add(Calendar.MINUTE, 10);
                hours = time.get(Calendar.HOUR_OF_DAY);
                minutes = time.get(Calendar.MINUTE);
                minutes = (int) (Math.ceil((minutes / 5.0)) * 5.0);
                if (minutes >= 60) {
                    hours++;
                }
            }
            mTimePicker.setCurrentHour(hours);
            mTimePicker.setCurrentMinute(minutes);
            mTimePicker.setTag(time.get(Calendar.SECOND));
        }
    }

    @Override
    public void notifyTick(TimePoint now) {
        super.notifyTick(now);

        if (mAbsolute.getVisibility() == View.VISIBLE) {
            int resId;
            TimePoint timePoint;
            String time;
            String timeLeft;
            String timeRight;
            if (mStartTime == null) {
                mStartTime = getPickerTime();
            }
            if (mStartTime.after(now)) {
                resId = R.string.race_start_time_in;
                timePoint = mStartTime.minus(now.asMillis());
                setButtonTime(timePoint, false);
                time = TimeUtils.formatDurationUntil(timePoint.asMillis());
                timeLeft = TimeUtils.formatDurationUntil(mTimeLeft.getTimeInMillis());
                timeRight = TimeUtils.formatDurationUntil(mTimeRight.getTimeInMillis());
                if (timeRight.equals(time)) {
                    mTimeRight.add(Calendar.MINUTE, 1);
                    timeRight = TimeUtils.formatDurationUntil(mTimeRight.getTimeInMillis());
                }
            } else {
                resId = R.string.race_start_time_ago;
                timePoint = now.minus(mStartTime.asMillis());
                setButtonTime(timePoint, true);
                time = TimeUtils.formatDurationSince(timePoint.asMillis());
                timeLeft = TimeUtils.formatDurationSince(mTimeLeft.getTimeInMillis());
                timeRight = TimeUtils.formatDurationSince(mTimeRight.getTimeInMillis());
                if (timeRight.equals(time)) {
                    mTimeRight.add(Calendar.MINUTE, -1);
                    timeRight = TimeUtils.formatDurationSince(mTimeRight.getTimeInMillis());
                }
            }

            if (mCountdown != null) {
                String countdown = getString(resId, time);
                mCountdown.setText(countdown);
                mCountdown.setTag(resId);
            }

            if (mMinuteDec != null) {
                String countdown = getString(resId, timeLeft);
                if (ZERO_TIME.equals(countdown)) {
                    countdown = countdown.substring(1);
                }
                mMinuteDec.setText(countdown);
            }

            if (mMinuteInc != null) {
                String countdown = getString(resId, timeRight);
                if (ZERO_TIME.equals(countdown)) {
                    countdown = countdown.substring(1);
                }
                mMinuteInc.setText(countdown);
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

        int id;
        if (AppConstants.LIGHT_THEME.equals(AppPreferences.on(getActivity()).getTheme())) {
            id = R.drawable.nav_drawer_tab_button_light;
        } else {
            id = R.drawable.nav_drawer_tab_button_dark;
        }
        Drawable drawable = ContextCompat.getDrawable(getActivity(), id);
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

    private void setButtonTime(TimePoint timePoint, boolean reverse) {
        mCalendar.setTime(timePoint.asDate());
        mTimeLeft = getNewTime(mCalendar, (reverse) ? 1 : 0);
        mTimeRight = getNewTime(mCalendar, (reverse) ? 0 : 1);
    }

    private Calendar getNewTime(Calendar calendar, int upDown) {
        Calendar newCalendar = (Calendar) calendar.clone();
        newCalendar.add(Calendar.MINUTE, upDown);
        newCalendar.set(Calendar.SECOND, 0);
        newCalendar.set(Calendar.MILLISECOND, 0);
        return newCalendar;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.minute_inc:
            case R.id.minute_dec:
                mCalendar = Calendar.getInstance();
                int msec = mCalendar.get(Calendar.MILLISECOND);
                mCalendar.set(Calendar.MILLISECOND, msec);
                String button = ((TextView) view).getText().toString();
                boolean down = button.substring(0, 1).equals("-");
                String[] values = button.split(":");
                int hour = Integer.parseInt(values[0]);
                int min = Integer.parseInt(values[1]);
                if (view.getId() == R.id.minute_dec) { // button right
                    if (!down) { // time is positive
                        mCalendar.add(Calendar.HOUR_OF_DAY, -1 * hour);
                        mCalendar.add(Calendar.MINUTE, -1 * min);
                    } else {
                        mCalendar.add(Calendar.HOUR_OF_DAY, -1 * hour);
                        mCalendar.add(Calendar.MINUTE, min);
                    }
                } else { // button left
                    if (!down) { // time is positive
                        mCalendar.add(Calendar.HOUR_OF_DAY, -1 * hour);
                        mCalendar.add(Calendar.MINUTE, -1 * min);
                    } else {
                        mCalendar.add(Calendar.HOUR_OF_DAY, -1 * hour);
                        mCalendar.add(Calendar.MINUTE, min);
                    }
                }

                mStartTime = new MillisecondsTimePoint(mCalendar.getTimeInMillis());
                mListenerIgnore = true;
                setPickerTime();
                break;

            case R.id.set_start_time_absolute:
                changeFragment(mStartTime);
                break;

            case R.id.set_start_time_relative:
                changeFragment(MillisecondsTimePoint.now(), new MillisecondsDurationImpl(mTimeOffset.getValue() * 60 * 1000), identifier);
                break;

            case R.id.header_text:
                changeFragment();
                break;

            default:
                break;
        }
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        if (!mListenerIgnore) {
            mStartTime = new MillisecondsTimePoint(getPickerTime().asMillis());
        }
        mListenerIgnore = false;
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        if (!mListenerIgnore) {
            mStartTime = new MillisecondsTimePoint(getPickerTime().asMillis());
        }
        mListenerIgnore = false;
    }

    private void setPickerTime() {
        Calendar today = Calendar.getInstance();
        Calendar newTime = (Calendar) today.clone();
        newTime.setTime(mStartTime.asDate());

        int days = TimeUtils.daysBetween(today, newTime) + Math.abs(PAST_DAYS);

        if (mDatePicker != null) {
            mDatePicker.setValue(days);
        }
        if (mTimePicker != null) {
            mTimePicker.setCurrentHour(newTime.get(Calendar.HOUR_OF_DAY));
            mTimePicker.setCurrentMinute(newTime.get(Calendar.MINUTE));
        }
    }

    private TimePoint getPickerTime() {
        Calendar calendar = Calendar.getInstance();
        if (mDatePicker != null) {
            calendar.add(Calendar.DAY_OF_MONTH, mDatePicker.getValue() + PAST_DAYS);
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
            if (getArguments().getInt(START_MODE, MODE_SETUP) != MODE_SETUP) {
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
            args.putSerializable(MainScheduleFragment.START_TIME, startTime);
            args.putSerializable(MainScheduleFragment.START_TIME_DIFF, startTimeDiff);
            args.putSerializable(MainScheduleFragment.DEPENDENT_RACE, identifier);
        }
        fragment.setArguments(args);
        transaction.replace(viewId, fragment);
        transaction.commit();
        Intent intent = new Intent(AppConstants.INTENT_ACTION_CLEAR_TOGGLE);
        BroadcastManager.getInstance(getActivity()).addIntent(intent);
    }
}
