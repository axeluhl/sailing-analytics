package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.domain.abstractlog.race.state.RaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class StartTimeFragment extends BaseFragment
    implements View.OnClickListener, NumberPicker.OnValueChangeListener, TimePicker.OnTimeChangedListener {
    private static final String START_MODE = "startMode";
    private static final int MAX_DAYS = 30;

    private NumberPicker mDatePicker;
    private TimePicker mTimePicker;
    private TextView mCountdown;
    private TimePoint mStartTime;
    private boolean mListenerIgnore = true;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_schedule_start_time, container, false);

        mCountdown = ViewHolder.get(layout, R.id.start_countdown);
        ImageButton min_inc = ViewHolder.get(layout, R.id.minute_inc);
        if (min_inc != null) {
            min_inc.setOnClickListener(this);
        }

        ImageButton min_dec = ViewHolder.get(layout, R.id.minute_dec);
        if (min_dec != null) {
            min_dec.setOnClickListener(this);
        }

        Button setStart = ViewHolder.get(layout, R.id.set_start_time);
        if (setStart != null) {
            setStart.setOnClickListener(this);
        }

        return layout;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getRaceState().removeChangedListener(raceStateChangedListener);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        raceStateChangedListener = new BaseRaceStateChangedListener() {
            @Override
            public void onStartTimeChanged(ReadonlyRaceState state) {
                mStartTime = state.getStartTime();
                notifyTick();
            }
        };
        getRaceState().addChangedListener(raceStateChangedListener);
        Calendar time = Calendar.getInstance();
        if (getArguments() != null && getArguments().getInt(START_MODE, 0) == 2) {
            if (getRace() != null && getRaceState() != null) {
                time.setTime(getRaceState().getStartTime().asDate());
            }
        }
        if (getView() != null) {
            if (getArguments() != null) {
                switch (getArguments().getInt(START_MODE, 0)) {
                case 1:
                    View header = ViewHolder.get(getView(), R.id.header_text);
                    if (header != null) {
                        header.setOnClickListener(this);
                    }
                    View back = ViewHolder.get(getView(), R.id.header_back);
                    if (back != null) {
                        back.setVisibility(View.VISIBLE);
                    }
                    break;

                case 2:
                    View frame = ViewHolder.get(getView(), R.id.header);
                    if (frame != null) {
                        frame.setVisibility(View.GONE);
                    }
                    break;

                default:
                    break;
                }
            }

            mDatePicker = ViewHolder.get(getView(), R.id.start_date_picker);
            if (mDatePicker != null) {
                ThemeHelper.setPickerTextColor(getActivity(), mDatePicker,
                    ThemeHelper.getColor(getActivity(), R.attr.white));
                mDatePicker.setOnValueChangedListener(this);
                initDatePicker();
            }
            mTimePicker = ViewHolder.get(getView(), R.id.start_time_picker);
            if (mTimePicker != null) {
                ThemeHelper.setPickerTextColor(getActivity(), mTimePicker,
                    ThemeHelper.getColor(getActivity(), R.attr.white));
                mTimePicker.setOnTimeChangedListener(this);
                mTimePicker.setIs24HourView(true);
                int hours = time.get(Calendar.HOUR_OF_DAY);
                int minutes = time.get(Calendar.MINUTE);
                if (getArguments() != null && getArguments().getInt(START_MODE, 0) != 2) {
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
    }

    @Override
    public void notifyTick() {
        super.notifyTick();

        int resId;
        String time;
        MillisecondsTimePoint now = MillisecondsTimePoint.now();
        if (mStartTime == null) {
            mStartTime = getPickerTime(true);
        }
        resId = R.string.race_start_time_ago;
        if (mStartTime.after(now)) {
            //            resId = R.string.race_start_time_in;
            time = TimeUtils.formatDurationUntil(mStartTime.minus(now.asMillis()).asMillis());
        } else {
            //            resId = R.string.race_start_time_ago;
            time = TimeUtils.formatDurationSince(now.minus(mStartTime.asMillis()).asMillis());
        }
        if (mCountdown != null) {
            String countdown = getString(resId).replace("#TIME#", time);
            mCountdown.setText(countdown);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.minute_inc:
        case R.id.minute_dec:
            String operator = String.valueOf(view.getTag());
            if (!operator.equals("null")) {
                Calendar newStart = Calendar.getInstance();
                newStart.setTime(mStartTime.asDate());
                String secondsCountdown = mCountdown.getText().toString();
                secondsCountdown = secondsCountdown.substring(secondsCountdown.length() - 2);
                int seconds = Integer.parseInt(secondsCountdown);
                if (operator.equals("-")) {
                    seconds = -1 * seconds;
                } else {
                    seconds = 60 - seconds;
                }
                newStart.add(Calendar.SECOND, seconds);
                mStartTime = new MillisecondsTimePoint(newStart.getTimeInMillis());
                mListenerIgnore = true;
                setPickerTime();
            }
            break;

        case R.id.set_start_time:
            changeFragment(mStartTime);
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
            mStartTime = new MillisecondsTimePoint(getPickerTime(true).asMillis());
        }
        mListenerIgnore = false;
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        if (!mListenerIgnore) {
            mStartTime = new MillisecondsTimePoint(getPickerTime(true).asMillis());
        }
        mListenerIgnore = false;
    }

    private void setPickerTime() {
        Calendar today = Calendar.getInstance();
        Calendar newTime = (Calendar) today.clone();
        newTime.setTime(mStartTime.asDate());

        int days = TimeUtils.daysBetween(today, newTime);

        mDatePicker.setValue(days);
        mTimePicker.setCurrentHour(newTime.get(Calendar.HOUR_OF_DAY));
        mTimePicker.setCurrentMinute(newTime.get(Calendar.MINUTE));
    }

    private TimePoint getPickerTime(boolean zeroSeconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, mDatePicker.getValue());
        calendar.set(Calendar.HOUR_OF_DAY, mTimePicker.getCurrentHour());
        calendar.set(Calendar.MINUTE, mTimePicker.getCurrentMinute());
        if (zeroSeconds) {
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }
        return new MillisecondsTimePoint(calendar.getTime());
    }

    private void initDatePicker() {
        DateFormat dateFormat = DateFormat.getDateInstance();
        ArrayList<String> dates = new ArrayList<>();
        dates.add(getString(R.string.today));
        dates.add(getString(R.string.tomorrow));
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        for (int i = 3; i <= MAX_DAYS; i++) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            dates.add(dateFormat.format(calendar.getTime()));
        }
        mDatePicker.setDisplayedValues(dates.toArray(new String[dates.size()]));
        mDatePicker.setMinValue(0);
        mDatePicker.setMaxValue(dates.size() - 1);
        mDatePicker.setWrapSelectorWheel(false);
    }

    private void changeFragment() {
        changeFragment(null);
    }

    private void changeFragment(TimePoint startTime) {
        int viewId = R.id.racing_view_container;
        RaceFragment fragment = MainScheduleFragment.newInstance();
        Bundle args = getRecentArguments();
        if (getArguments() != null && startTime != null) {
            if (getArguments().getInt(START_MODE, 0) != 0) {
                getRaceState().forceNewStartTime(MillisecondsTimePoint.now(), startTime);
                fragment = RaceFlagViewerFragment.newInstance();
                viewId = R.id.race_frame;
            }
            args.putAll(getArguments());
            args.putSerializable(MainScheduleFragment.STARTTIME, startTime);
        }
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(viewId, fragment).commit();
        LocalBroadcastManager.getInstance(getActivity())
            .sendBroadcast(new Intent(AppConstants.INTENT_ACTION_CLEAR_TOGGLE));
    }
}
