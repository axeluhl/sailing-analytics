package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.sap.sailing.domain.abstractlog.race.state.RaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class StartTimeFragment extends RaceFragment implements View.OnClickListener {
    private static final String STARTMODE = "startMode";
    private static final int MAX_DAYS = 30;

    private NumberPicker mDatePicker;
    private TimePicker mTimePicker;
    private TextView mCountdown;
    private TimePoint startTime;
    
    /**
     * Listens for start time changes
     */
    private RaceStateChangedListener raceStateChangedListener;

    public static StartTimeFragment newInstance(int startMode) {
        StartTimeFragment fragment = new StartTimeFragment();
        Bundle args = new Bundle();
        args.putInt(STARTMODE, startMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_schedule_start_time, container, false);
        if (getArguments() != null) {
            switch (getArguments().getInt(STARTMODE, 0)) {
                case 1:
                    View header = layout.findViewById(R.id.header_text);
                    if (header != null) {
                        header.setOnClickListener(this);
                    }
                    View back = layout.findViewById(R.id.header_back);
                    if (back != null) {
                        back.setVisibility(View.VISIBLE);
                    }
                    break;
                case 2:
                    layout.findViewById(R.id.race_header).setVisibility(View.VISIBLE);
                    View frame = layout.findViewById(R.id.header);
                    if (frame != null) {
                        frame.setVisibility(View.GONE);
                    }
                    break;
                default:
                    break;
            }
        }
        mCountdown = (TextView) layout.findViewById(R.id.start_countdown);
        Button min5 = (Button) layout.findViewById(R.id.start_min_five);
        if (min5 != null) {
            min5.setOnClickListener(this);
        }
        Button min1 = (Button) layout.findViewById(R.id.start_min_one);
        if (min1 != null) {
            min1.setOnClickListener(this);
        }
        Button setStart = (Button) layout.findViewById(R.id.set_start_time);
        if (setStart != null) {
            setStart.setOnClickListener(this);
        }
        return layout;
    }

    @Override
    public void onDestroy() {
        getRaceState().removeChangedListener(raceStateChangedListener);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        raceStateChangedListener = new BaseRaceStateChangedListener() {
            @Override
            public void onStartTimeChanged(ReadonlyRaceState state) {
                startTime = state.getStartTime();
                notifyTick();
            }
        };
        getRaceState().addChangedListener(raceStateChangedListener);
        Calendar time = Calendar.getInstance();
        if (getArguments() != null && getArguments().getInt(STARTMODE, 0) == 2) {
            if (getRace() != null && getRaceState() != null) {
                time.setTime(getRaceState().getStartTime().asDate());
            }
        }
        if (getView() != null) {
            mDatePicker = (NumberPicker) getView().findViewById(R.id.start_date_picker);
            if (mDatePicker != null) {
                initDatePicker();
            }
            mTimePicker = (TimePicker) getView().findViewById(R.id.start_time_picker);
            if (mTimePicker != null) {
                mTimePicker.setIs24HourView(true);
                int hours = time.get(Calendar.HOUR_OF_DAY);
                int minutes = time.get(Calendar.MINUTE);
                if (getArguments() != null && getArguments().getInt(STARTMODE, 0) != 2) {
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

        MillisecondsTimePoint now = MillisecondsTimePoint.now();
        int resId;
        String time;

        if (startTime.after(now)) {
            resId = R.string.race_start_time_in;
            time = TimeUtils.formatDurationUntil(startTime.minus(now.asMillis()).asMillis());
        } else {
            resId = R.string.race_start_time_ago;
            time = TimeUtils.formatDurationSince(now.minus(startTime.asMillis()).asMillis());
        }
        if (mCountdown != null) {
            mCountdown.setText(getString(resId).replace("#TIME#", time));
        }
    }

    private TimePoint getPickerTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, mDatePicker.getValue());
        calendar.set(Calendar.HOUR_OF_DAY, mTimePicker.getCurrentHour());
        calendar.set(Calendar.MINUTE, mTimePicker.getCurrentMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_min_five:
            case R.id.start_min_one:
                Integer minutes;
                try {
                    minutes = Integer.valueOf("" + view.getTag());
                } catch (Exception ex) {
                    minutes = 0;
                }
                minutes++;
                Calendar newStartTime = Calendar.getInstance();
                newStartTime.add(Calendar.MINUTE, minutes);
                getRaceState().forceNewStartTime(MillisecondsTimePoint.now(), new MillisecondsTimePoint(newStartTime.getTime()));
                break;
            case R.id.set_start_time:
                changeFragment(getPickerTime());
                break;
            case R.id.header_text:
                changeFragment();
                break;
            default:
                break;
        }
    }

    private void changeFragment() {
       changeFragment(null);
    }

    private void changeFragment(TimePoint startTime) {
        int viewId = R.id.racing_view_container;
        RaceFragment fragment = MainScheduleFragment.newInstance();
        Bundle args = getRecentArguments();
        if (getArguments() != null && startTime != null) {
            if (getArguments().getInt(STARTMODE, 0) != 0) {
                getRaceState().forceNewStartTime(MillisecondsTimePoint.now(), startTime);
                fragment = RaceFlagViewerFragment.newInstance();
                viewId = R.id.race_frame;
            }
            args.putAll(getArguments());
            args.putSerializable(MainScheduleFragment.STARTTIME, startTime);
        }
        fragment.setArguments(args);
        getFragmentManager()
                .beginTransaction()
                .replace(viewId, fragment)
                .commit();
        sendIntent(R.string.intent_uncheck_all);
    }
}
