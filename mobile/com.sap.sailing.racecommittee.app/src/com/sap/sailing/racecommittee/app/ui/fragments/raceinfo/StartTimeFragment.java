package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.state.RaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.InMemoryDataStore;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.ui.adapters.DependentRaceSpinnerAdapter;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.io.Serializable;
import java.util.Calendar;

public class StartTimeFragment extends BaseFragment
    implements View.OnClickListener, NumberPicker.OnValueChangeListener, TimePicker.OnTimeChangedListener {

    public static final int MODE_SETUP = 0;
    public static final int MODE_1 = 1;
    public static final int MODE_TIME_PANEL = 2;

    private static final String START_MODE = "startMode";
    private static final int FUTURE_DAYS = 25;
    private static final int PAST_DAYS = -3;
    private static final int MAX_DIFF_MIN = 60;

    private View mRelative;
    private View mAbsolute;
    private ToggleButton mToggle;

    private NumberPicker mDatePicker;
    private NumberPicker mTimeOffset;
    private Spinner mDependentRace;
    private TimePicker mTimePicker;
    private TextView mCountdown;
    private TextView mDebugTime;
    private Button mMinuteInc;
    private Button mMinuteDec;
    private TimePoint mStartTime;
    private Calendar mTimeLeft;
    private Calendar mTimeRight;
    private Calendar mCalendar;
    private boolean mListenerIgnore = true;

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

    public static StartTimeFragment newInstance(Serializable timePoint) {
        StartTimeFragment fragment = newInstance(MODE_SETUP);
        if (timePoint != null) {
            Bundle args = fragment.getArguments();
            args.putSerializable(MainScheduleFragment.START_TIME, timePoint);
        }
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_schedule_start_time, container, false);

        mRelative = ViewHolder.get(layout, R.id.time_relative);
        mAbsolute = ViewHolder.get(layout, R.id.time_absolute);

        if (preferences.isDependentRacesAllowed()) {
            View select = ViewHolder.get(layout, R.id.time_select);
            if (select != null) {
                select.setVisibility(View.VISIBLE);
            }
        }

        mCountdown = ViewHolder.get(layout, R.id.start_countdown);
        mMinuteInc = ViewHolder.get(layout, R.id.minute_inc);
        if (mMinuteInc != null) {
            mMinuteInc.setOnClickListener(this);
        }

        mMinuteDec = ViewHolder.get(layout, R.id.minute_dec);
        if (mMinuteDec != null) {
            mMinuteDec.setOnClickListener(this);
        }

        View setStart = ViewHolder.get(layout, R.id.set_start_time_absolute);
        if (setStart != null) {
            setStart.setOnClickListener(this);
        }

        mToggle = ViewHolder.get(layout, R.id.switch_start_time);
        if (mToggle != null) {
            mToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    showInputView(isChecked);
                }
            });
        }

        mDebugTime = ViewHolder.get(layout, R.id.debug_time);
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
                    View header = ViewHolder.get(getView(), R.id.header_text);
                    if (header != null) {
                        header.setOnClickListener(this);
                    }
                    View back = ViewHolder.get(getView(), R.id.header_back);
                    if (back != null) {
                        back.setVisibility(View.VISIBLE);
                    }
                    break;

                case MODE_TIME_PANEL:
                    if (getRace() != null && getRaceState() != null) {
                        mStartTime = getRaceState().getStartTime();
                        time.setTime(mStartTime.asDate());
                    }
                    View frame = ViewHolder.get(getView(), R.id.header);
                    if (frame != null) {
                        frame.setVisibility(View.GONE);
                    }
                    break;

                default: // MODE_SETUP
                    mStartTime = (TimePoint) getArguments().getSerializable(MainScheduleFragment.START_TIME);
                    if (mStartTime != null) {
                        time.setTime(mStartTime.asDate());
                    }
                    break;
                }
            }

            mDatePicker = ViewHolder.get(getView(), R.id.start_date_picker);
            if (mDatePicker != null) {
                ThemeHelper.setPickerTextColor(getActivity(), mDatePicker, ThemeHelper.getColor(getActivity(), R.attr.white));
                mDatePicker.setOnValueChangedListener(this);
                TimeUtils.initDatePicker(getActivity(), mDatePicker, time, PAST_DAYS, FUTURE_DAYS);
                mDatePicker.setValue(Math.abs(PAST_DAYS));
            }
            mTimePicker = ViewHolder.get(getView(), R.id.start_time_picker);
            if (mTimePicker != null) {
                ThemeHelper.setPickerTextColor(getActivity(), mTimePicker, ThemeHelper.getColor(getActivity(), R.attr.white));
                mTimePicker.setOnTimeChangedListener(this);
                mTimePicker.setIs24HourView(true);
                int hours = time.get(Calendar.HOUR_OF_DAY);
                int minutes = time.get(Calendar.MINUTE);
                if (getArguments() != null && getArguments().getInt(START_MODE, MODE_SETUP) != MODE_TIME_PANEL
                    && getArguments().getSerializable(MainScheduleFragment.START_TIME) == null) {
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

            mTimeOffset = ViewHolder.get(getView(), R.id.time_offset);
            if (mTimeOffset != null) {
                ThemeHelper.setPickerTextColor(getActivity(), mTimeOffset, ThemeHelper.getColor(getActivity(), R.attr.white));
                mTimeOffset.setMinValue(0);
                mTimeOffset.setMaxValue(MAX_DIFF_MIN);
                mTimeOffset.setWrapSelectorWheel(false);
                mTimeOffset.setValue(preferences.getDependentRacesOffset());
            }

            mDependentRace = ViewHolder.get(getView(), R.id.dependent_race);
            if (mDependentRace != null) {
                DependentRaceSpinnerAdapter adapter = new DependentRaceSpinnerAdapter(getActivity(), R.layout.dependent_race_item);
                for (int i = 0; i < 100; i++) {
                    adapter.add("Position: " + i);
                }
                mDependentRace.setAdapter(adapter);
            }
        }

        showInputView(mToggle.isChecked());
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
                if (countdown.equals("-00:00:00")) {
                    countdown = "00:00:00";
                }
                mMinuteDec.setText(countdown);
            }

            if (mMinuteInc != null) {
                String countdown = getString(resId, timeRight);
                if (countdown.equals("-00:00:00")) {
                    countdown = "00:00:00";
                }
                mMinuteInc.setText(countdown);
            }

            if (mDebugTime != null) {
                mDebugTime.setText(mStartTime.asDate().toString());
            }
        }
    }

    private void showInputView(boolean isChecked) {
        if (isChecked) {
            mAbsolute.setVisibility(View.GONE);
            mRelative.setVisibility(View.VISIBLE);
        } else {
            mAbsolute.setVisibility(View.VISIBLE);
            mRelative.setVisibility(View.GONE);
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

    private void changeFragment(TimePoint startTime, Duration startTimeDiff, SimpleRaceLogIdentifier dependentRace) {
        int viewId = R.id.racing_view_container;
        TimePoint now = MillisecondsTimePoint.now();
        RaceFragment fragment = MainScheduleFragment.newInstance();
        Bundle args = getRecentArguments();
        RacingProcedureType procedureType = getRaceState().getTypedRacingProcedure().getType();
        getRaceState().setRacingProcedure(now, procedureType);
        if (getArguments() != null && startTime != null) {
            if (getArguments().getInt(START_MODE, MODE_SETUP) != MODE_SETUP) {
                if (startTimeDiff == null && dependentRace == null) {
                    // absolute start time
                    getRaceState().forceNewStartTime(now, startTime);
                } else {
                    // relative start time
                    getRaceState().forceNewDependentStartTime(now, startTimeDiff, dependentRace);
                }
                fragment = RaceFlagViewerFragment.newInstance();
                viewId = R.id.race_frame;
            }
            args.putAll(getArguments());
            args.putSerializable(MainScheduleFragment.START_TIME, startTime);
        }
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(viewId, fragment).commit();
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(AppConstants.INTENT_ACTION_CLEAR_TOGGLE));
    }
}
